package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DishFlavorMapper {
    /**
     * 添加口味
     * @param flavors
     */
    void addFlavors(List<DishFlavor> flavors);

    /**
     * 根据id列表删除菜品口味
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    /**
     * 根据dishId查询菜品口味
     * @param id
     * @return
     */
    List<DishFlavor> getByDishId(Long id);

    /**
     * 根据菜品id删除菜品口味
     * @param id
     */
    void deleteDishFlavorByDishId(Long id);
}
