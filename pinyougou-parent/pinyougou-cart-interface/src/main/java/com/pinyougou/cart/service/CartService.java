package com.pinyougou.cart.service;

import entity.Cart;

import java.util.List;

public interface CartService {
    /**
     * 添加商品到购物车集合
     * @param cartList//购物车集合
     * @param itemId//每个商品sku的id
     * @param num//要添加的数量
     * @return
     */
    public List<Cart> addGoodsToCartList(List<Cart> cartList,Long itemId,int num);

    public List<Cart> findCartListInRedis(String username);

    /**
     * 合并到redis中
     * @return
     */
    public void saveCartListCookieToCartListRedis(String username , List<Cart> cartList);

    /**
     * 添加到redis中
     * @param username
     * @param cartList
     */
    void addCartListToRedis(String username, List<Cart> cartList);
}
