package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.utils.CookieUtil;
import entity.Cart;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;
    @Reference(timeout = 6000)
    private CartService cartService;
    @RequestMapping("/add")
    @CrossOrigin(origins="http://localhost:8080",allowCredentials = "true")
    public Result addGoodsToCartList(Long itemId, int num){
        System.out.println(num);
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("username:"+username);
        List<Cart> cartList = findCartList();
        cartList = cartService.addGoodsToCartList(cartList, itemId, num);
        try {
            if ("anonymousUser".equals(username)){
                //用户没有登录在cookie中操作
                //更新cookie
                CookieUtil.setCookie(request,response , "cartList",JSON.toJSONString(cartList),3600*24,"UTF-8" );
            }else {
                //用户登录了在redis中操作

                cartService.addCartListToRedis(username, cartList);
            }

            return new Result(true,"修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"修改失敗");
        }



    }
    @RequestMapping("findAll")
    public List<Cart> findCartList(){
        //购物车主线,所有方法都会经过
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("username:"+username);
        if ("anonymousUser".equals(username)){
            String cartList = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
            if (cartList==null||cartList.length()==0){
                cartList = "[]";
            }
            List<Cart> carts = JSON.parseArray(cartList, Cart.class);
            return carts;
        } else {//用户登录了如果用户是第一次刚登陆获取本地cookie合并到redis中并且删除cookie中的值
            //在redis中查找
            String cartList = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
            if (cartList!=null&&cartList.length()!=0&&!"".equals(cartList)){//cookie中有值
                //合并本地购物车和服务端购物车
                List<Cart> carts_cookie = JSON.parseArray(cartList, Cart.class);
                cartService.saveCartListCookieToCartListRedis(username,carts_cookie);
                //同步完成后删除cookie
                CookieUtil.deleteCookie(request,response ,"cartList" );
            }

            List<Cart> cartListInRedis = cartService.findCartListInRedis(username);
            return cartListInRedis;


        }



    }
}
