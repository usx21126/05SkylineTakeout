package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService {
    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    void addShoppingCart(ShoppingCartDTO shoppingCartDTO);

    /**
     * 查看购物车
     * @return
     */
    List<ShoppingCart> getShoppingCartList();

    /**
     * 清空购物车
     */
    void deleteShoppingCart();

    /**
     * 根据信息删除购物车的一个商品
     * @param shoppingCartDTO
     */
    void deleteShoppingCartByShoppingCartInfo(ShoppingCartDTO shoppingCartDTO);
}
