package com.sky.mapper;

import com.sky.anno.AutoFill;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    /**
     * 添加员工
     * @param employee
     */
    @AutoFill(OperationType.INSERT)
    void addEmployee(Employee employee);

    /**
     * 分页查询返回记录条数
     * @param employeePageQueryDTO
     * @return
     */
    Integer getCount(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * 分页查询返回员工列表
     * @param name
     * @param begin
     * @param pageSize
     * @return
     */
    List<Employee> getEmployeeList(String name,Integer begin,Integer pageSize);

    /**
     * 更新员工数据
     * @param employee
     */
    @AutoFill(OperationType.UPDATE)
    void updateEmployee(Employee employee);

    /**
     * 编辑员工信息
     * @param id
     * @return
     */
    Employee getEmployeeById(Long id);
}
