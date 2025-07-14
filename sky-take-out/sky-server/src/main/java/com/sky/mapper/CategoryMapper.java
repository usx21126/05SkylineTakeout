package com.sky.mapper;

import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
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
    List<Category> getCategoryList(String name, Integer type ,Integer begin, Integer pageSize);

    /**
     * 修改分类
     * @param category
     */
    void updateCategory(Category category);

    /**
     * 新增分类
     * @param category
     */
    void addCategory(Category category);

    void deleteCategoryById(Long id);
}
