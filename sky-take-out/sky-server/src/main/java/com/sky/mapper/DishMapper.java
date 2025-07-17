package com.sky.mapper;

import com.sky.anno.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    /**
     * 新增菜品
     * @param dish
     */
    @AutoFill(OperationType.INSERT)
    void addDish(Dish dish);

    /**
     * 菜品分页查询返回菜品列表
     * @param categoryId
     * @param name
     * @param status
     * @param begin
     * @param pageSize
     * @return
     */
    List<DishVO> getDishPageList(Integer categoryId, String name, Integer status, Integer begin, Integer pageSize);

    /**
     * 菜品分页查询返回总记录条数
     * @return
     */
    Integer getCount(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    Dish getById(Long id);

    /**
     * 根据id列表删除菜品
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    /**
     * 更新菜品信息
     * @param dish
     */
    @AutoFill(OperationType.UPDATE)
    void updateDish(Dish dish);

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    List<Dish> getDishesByCategoryId(Long categoryId);

    Integer getStatusCount(List<Long> dishIds);
}
