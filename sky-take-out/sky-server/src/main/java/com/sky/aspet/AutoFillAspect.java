package com.sky.aspet;


import com.sky.anno.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import com.sky.mapper.EmployeeMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Slf4j
@Component
@Aspect
public class AutoFillAspect {

    @Before("@annotation(com.sky.anno.AutoFill)")
    public void autoFill(JoinPoint joinPoint) throws Throwable {
        //  1.获取目标方法上的注解值
        MethodSignature signature = (MethodSignature) joinPoint.getSignature(); //方法签名
        Method method = signature.getMethod();  //方法对象
        AutoFill autoFill = method.getAnnotation(AutoFill.class);
        OperationType operationType = autoFill.value();
        //  2.获取目标方法的参数对象
        Object[] args = joinPoint.getArgs();
        if(args==null || args.length==0){
            return;
        }
        Object entity = args[0];
        //  3.补充字段
        if(operationType == OperationType.INSERT){
            Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
            Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
            Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
            setCreateTime.invoke(entity, LocalDateTime.now());
            setUpdateTime.invoke(entity, LocalDateTime.now());
            setCreateUser.invoke(entity, BaseContext.getCurrentId());
            setUpdateUser.invoke(entity, BaseContext.getCurrentId());
        }else if(operationType == OperationType.UPDATE){
            Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
            Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
            setUpdateTime.invoke(entity, LocalDateTime.now());
            setUpdateUser.invoke(entity, BaseContext.getCurrentId());
        }
    }
}
