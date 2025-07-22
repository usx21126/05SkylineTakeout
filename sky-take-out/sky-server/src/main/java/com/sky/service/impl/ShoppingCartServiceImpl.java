package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    @Transactional
    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();

        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());
        ShoppingCart cart = shoppingCartMapper.getByShoppingCartInfo(shoppingCart);



        if(cart == null) {
            shoppingCart.setNumber(1);
            if (shoppingCart.getDishId()!=null){
                Dish dish = dishMapper.getById(shoppingCart.getDishId());
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());

            }else if(shoppingCart.getSetmealId() != null){
                Setmeal setmeal = setmealMapper.getSetmealById(shoppingCart.getSetmealId());
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
            }
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.addShoppingCart(shoppingCart);
        }else {
            cart.setNumber(cart.getNumber()+1);
            shoppingCartMapper.updateShoppingCart(cart);
        }
    }

    /**
     * 查看购物车
     * @return
     */
    @Override
    public List<ShoppingCart> getShoppingCartList() {
        return shoppingCartMapper.getShoppingCartListByUserId(BaseContext.getCurrentId());
    }

    /**
     * 清空购物车
     */
    @Override
    public void deleteShoppingCart() {
        shoppingCartMapper.deleteShoppingCartByUserId(BaseContext.getCurrentId());
    }
    @Transactional
    @Override
    public void deleteShoppingCartByShoppingCartInfo(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());
        ShoppingCart cart = shoppingCartMapper.getByShoppingCartInfo(shoppingCart);
        if(cart == null) {throw new RuntimeException();}

        if(cart.getNumber()==1){
            shoppingCartMapper.deleteShoppingCartById(cart.getId());
        }else{
            cart.setNumber(cart.getNumber()-1);
            shoppingCartMapper.updateShoppingCart(cart);
        }
    }
}
