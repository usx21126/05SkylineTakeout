package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.constant.MessageConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.LoginFailedException;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@Api(tags = "员工相关接口")
@RestController
@RequestMapping("/admin/employee")
@Slf4j
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")
    @ApiOperation("员工登录")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        String token = null;
        try {
            Map<String, Object> claims = new HashMap<>();
            claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
            token = JwtUtil.createJWT(
                    jwtProperties.getAdminSecretKey(),
                    jwtProperties.getAdminTtl(),
                    claims);
        } catch (Exception e) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @PostMapping("/logout")
    @ApiOperation("员工退出")
    public Result<String> logout() {
        return Result.success();
    }

    /**
     * 新增员工
     * @param employeeDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增员工")
    public Result<String> addEmployee(@RequestBody EmployeeDTO employeeDTO) {
        log.info("employeeDTO={}", employeeDTO);
        employeeService.addEmployee(employeeDTO);
        return Result.success();
    }

    /**
     * 分页查询员工
     * @param employeePageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("分页查询员工")
    public Result<PageResult> getEmployeePages(EmployeePageQueryDTO employeePageQueryDTO) {
        log.info("员工分页查询={}", employeePageQueryDTO);
        log.info("employeePageQueryDTO={}", employeePageQueryDTO);
        PageResult pageResult= employeeService.getEmployeePages(employeePageQueryDTO);

        return Result.success(pageResult);
    }

    /**
     * 启用、禁用员工账号
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("启用、禁用员工账号")
    public Result<String> updateStatus(@PathVariable Integer status,Long id) {
        log.info("status={},id={}", status,id);
        employeeService.updateStatus(status,id);
        return Result.success();
    }

    /**
     * 根据id查询员工
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询员工")
    public Result<Employee> getEmployeeById(@PathVariable Long id) {
        log.info("id={}", id);
        Employee employee = employeeService.getEmployeeById(id);
        return Result.success(employee);
    }

    /**
     * 编辑员工信息
     * @param employeeDTO
     * @return
     */
    @PutMapping
    @ApiOperation("编辑员工信息")
    public Result<String> updateEmployeeInfo(@RequestBody EmployeeDTO employeeDTO) {
        log.info("employeeDTO={}", employeeDTO);
        employeeService.updateEmployeeInfo(employeeDTO);
        return Result.success();
    }


}
