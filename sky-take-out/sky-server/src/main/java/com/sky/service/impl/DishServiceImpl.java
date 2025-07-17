package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Slf4j
@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 新增菜品
     * @param dishDTO
     */
    @Transactional
    @Override
    public void addDish(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.addDish(dish);
        Long dishId = dish.getId();
        List<DishFlavor> dishFlavors = dishDTO.getFlavors();
        if(dishFlavors!=null && !dishFlavors.isEmpty()){
            dishFlavors.forEach(dishFlavor -> dishFlavor.setDishId(dishId));
            dishFlavorMapper.addFlavors(dishFlavors);
        }
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult getDishPages(DishPageQueryDTO dishPageQueryDTO) {

        Integer total = dishMapper.getCount(dishPageQueryDTO);
        List<DishVO> records = dishMapper.getDishPageList(
                dishPageQueryDTO.getCategoryId(),
                dishPageQueryDTO.getName(),
                dishPageQueryDTO.getStatus(),
                (dishPageQueryDTO.getPage()-1)*dishPageQueryDTO.getPageSize(),
                dishPageQueryDTO.getPageSize()
        );
        return new PageResult(total,records);
    }

    /**
     * 批量删除菜品
     * @param ids
     */
    @Transactional
    @Override
    public void deleteDish(List<Long> ids) {
        // 起售菜品不能删
        ids.forEach(id -> {
            Dish dish = dishMapper.getById(id);
            if(dish.getStatus() == StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        });
        // 在套餐中的菜品不能删
        Integer count = setmealDishMapper.getCountByDishId(ids);
        if(count > 0){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        // 删除菜品，同时删除菜品对应的口味dish_flavor
        dishMapper.deleteBatch(ids);
        dishFlavorMapper.deleteBatch(ids);
    }

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    @Transactional
    @Override
    public DishVO getDishById(Long id) {
        Dish dish = dishMapper.getById(id);
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        List<DishFlavor>  flavorList = dishFlavorMapper.getByDishId(id);
        dishVO.setFlavors(flavorList);
        return dishVO;
    }

    /**
     * 修改菜品
     * @param dishDTO
     */
    @Transactional
    @Override
    public void updateDishInfo(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.updateDish(dish);

        dishFlavorMapper.deleteDishFlavorByDishId(dishDTO.getId());
        List<DishFlavor> dishFlavors = dishDTO.getFlavors();
        if(dishFlavors!=null && !dishFlavors.isEmpty()){
            dishFlavors.forEach(dishFlavor -> dishFlavor.setDishId(dish.getId()));
            dishFlavorMapper.addFlavors(dishDTO.getFlavors());
        }
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @Override
    public List<Dish> getDishesByCategoryId(Long categoryId) {
        return dishMapper.getDishesByCategoryId(categoryId);
    }
    @Transactional
    @Override
    public void updateDishStatus(Integer status, Long id) {
        Dish dish = Dish.builder()
                .id(id)
                .status(status)
                .build();
        dishMapper.updateDish(dish);
        if(status == StatusConstant.DISABLE){
            // 根据 菜品id 在套餐-菜品表中 查询套餐id
            List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishId(id);
            // 根据 套餐id 设置套餐状态为 0
            if(setmealIds != null && !setmealIds.isEmpty()){
                setmealIds.forEach(setmealId -> {
                    Setmeal setmeal = Setmeal.builder()
                            .id(setmealId)
                            .status(StatusConstant.DISABLE)
                            .build();
                    setmealMapper.updateSetmeal(setmeal);
                });
            }

        }
    }
}
