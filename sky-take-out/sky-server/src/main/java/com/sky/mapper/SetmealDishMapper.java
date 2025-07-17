package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
    /**
     * 查询套餐表中包含菜品id记录数量
     * @param ids
     * @return
     */
    Integer getCountByDishId(List<Long> ids);

    /**
     * 添加套餐菜品列表
     * @param setmealDishes
     */
    void addSetmealDishes(List<SetmealDish> setmealDishes);

    /**
     * 根据id查询套餐
     * @param id
     * @return
     */
    List<Long> getSetmealIdsByDishId(Long id);

    /**
     * 根据setmealId 查询 菜品
     * @param setmealId
     * @return
     */
    List<SetmealDish> getSetmealDishBySetmealId(Long setmealId);

    /**
     * 根据套餐id删除 套餐-菜品表记录
     * @param setmealId
     */
    void deleteSetmealDishesBySetmealId(Long setmealId);

    List<Long> getDishIdsBySetmealId(Long setmealId);
}
