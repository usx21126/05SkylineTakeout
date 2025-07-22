package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {
    /**
     * 根据条件查询购物车
     * @param shoppingCart
     * @return
     */
    ShoppingCart getByShoppingCartInfo(ShoppingCart shoppingCart);

    /**
     * 添加购物车数据
     * @param shoppingCart
     */
    void addShoppingCart(ShoppingCart shoppingCart);

    /**
     * 更新购物车状态
     * @param cart
     */
    void updateShoppingCart(ShoppingCart cart);

    /**
     * 根据userId查询购物车
     * @param userId
     * @return
     */
    List<ShoppingCart> getShoppingCartListByUserId(Long userId);

    /**
     * 根据userId清空购物车
     * @param userId
     */
    void deleteShoppingCartByUserId(Long userId);
}
