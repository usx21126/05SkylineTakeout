package com.sky.mapper;

import com.sky.anno.AutoFill;
import com.sky.context.BaseContext;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CategoryMapper {
    /**
     * 分类分页查询返回总记录条数
     * @param categoryPageQueryDTO
     * @return
     */
    Integer getCount(CategoryPageQueryDTO categoryPageQueryDTO);

    /**
     * 分类分页查询返回分类列表
     * @param name
     * @param type
     * @param begin
     * @param pageSize
     * @return
     */
    List<Category> getCategoryPageList(String name, Integer type ,Integer begin, Integer pageSize);

    /**
     * 修改分类
     * @param category
     */
    @AutoFill(OperationType.UPDATE)
    void updateCategory(Category category);

    /**
     * 新增分类
     * @param category
     */
    @AutoFill(OperationType.INSERT)
    void addCategory(Category category);

    /**
     * 根据id删除分类
     * @param id
     */
    void deleteCategoryById(Long id);

    /**
     * 根据类型查询分类
     * @param type
     * @return
     */
    List<Category> getCategoryList(Integer type);
}
