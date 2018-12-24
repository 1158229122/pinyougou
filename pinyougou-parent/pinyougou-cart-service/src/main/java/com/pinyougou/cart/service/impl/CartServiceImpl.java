package com.pinyougou.cart.service.impl;


import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;

import com.pinyougou.pojo.TbOrderItem;
import entity.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private TbItemMapper itemMapper;
    /**
     * 添加商品sku到购物车集合
     * @param cartList//购物车集合
     * @param itemId//每个商品sku的id
     * @param num//要添加的数量
     * @return
     */
    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, int num) {
        //1根据商品的sku列表查询商品的详细信息
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        if (item==null){
            throw  new RuntimeException("item没有这个id");
        }
        //2获取商家id
        String sellerId = item.getSellerId();
        //3根据商家id判断购物车对象是否存在该商家
        Cart cart = isSellerIdInCartList(cartList, sellerId);
        if (cart==null){
            //4.1如果不存在创建该商家的购物车对象
            cart = new Cart();
            cart.setSellerId(sellerId);
            cart.setSellerName(item.getSeller());
            List<TbOrderItem> orderItemList = new ArrayList<TbOrderItem>();
            TbOrderItem orderItem = createOrderItem(num, item);
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);
            cartList.add(cart);
        }else
       {
            //5如果存在该商家的购物车
            TbOrderItem tbOrderItem = isItemIdInOrderItemList(cart.getOrderItemList(),itemId);
            if (tbOrderItem==null){
                //5.2如果添加的商品不存在....
                TbOrderItem orderItem1 = createOrderItem(num, item);
                cart.getOrderItemList().add(orderItem1);
            }else {
                //5.1如果添加的商品已经有了....
                tbOrderItem.setNum(tbOrderItem.getNum()+num);
                tbOrderItem.setTotalFee(new BigDecimal(tbOrderItem.getNum()*tbOrderItem.getPrice().doubleValue()));
                if (tbOrderItem.getNum()<=0){
                    cart.getOrderItemList().remove(tbOrderItem);
                }
                if (cart.getOrderItemList()==null||cart.getOrderItemList().size()<=0){
                    cartList.remove(cart);
                }
            }



        }


        return cartList;
    }
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<Cart> findCartListInRedis(String username) {
        List<Cart> cartLis = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
        if (cartLis==null||cartLis.size()==0){
            cartLis = new ArrayList<Cart>();
        }
        return cartLis;
    }

    /**
     * 保存本地到服务端同步
     * @param username
     * @param cartList
     */
    @Override
    public void saveCartListCookieToCartListRedis(String username, List<Cart> cartList) {
        System.out.println(cartList);
        if (cartList.size()==0||cartList==null){
            return;
        }

        List<Cart> cartListInRedis = findCartListInRedis(username);
        for (Cart cart : cartList) {
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                cartListInRedis = addGoodsToCartList(cartListInRedis, orderItem.getItemId(), orderItem.getNum());

            }
        }

        redisTemplate.boundHashOps("cartList").put(username, cartListInRedis);

    }

    /**
     * 保存购物车到redis
     * @param username
     *
     *
     *
     *
     * @param cartList
     */
    @Override
    public void addCartListToRedis(String username, List<Cart> cartList) {
        redisTemplate.boundHashOps("cartList").put(username,cartList );
    }


    private TbOrderItem isItemIdInOrderItemList(List<TbOrderItem> orderItemList,Long itemId){
        for (TbOrderItem orderItem : orderItemList) {
            if (orderItem.getItemId().longValue()==itemId.longValue()){
                return orderItem;
            }
        }
        return null;
    }

    private TbOrderItem createOrderItem(int num, TbItem item) {
        if (num<0){
            throw new RuntimeException("数量非法");
        }
        if (!"2".equals(item.getStatus())){
            throw new RuntimeException("商品状态非法");
        }
        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setItemId(item.getId());
        orderItem.setNum(num);
        orderItem.setPicPath(item.getImage());
        orderItem.setPrice(item.getPrice());
        orderItem.setSellerId(item.getSellerId());
        orderItem.setTitle(item.getTitle());
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*num));
        System.out.println(num);
        return orderItem;
    }

    //3根据商家id判断购物车对象是否存在该商家
    private Cart isSellerIdInCartList(List<Cart> cartList,String sellerId){
        if (cartList!=null||cartList.size()>0){
            for (Cart cart : cartList) {
                if (cart.getSellerId().equals(sellerId)){
                    //有这个商家的
                    return cart;
                }
            }
        }
        return null;
    }
}
