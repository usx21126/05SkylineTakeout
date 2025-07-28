package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
@Slf4j
@RestController
@RequestMapping("/admin/shop")
@Api(tags = "店铺操作接口")
public class ShopController {
    @Autowired
    private RedisTemplate redisTemplate;
    @GetMapping("/status")
    @ApiOperation("获取营业状态")
    public Result<Integer> status() {
        Integer status = (Integer) redisTemplate.opsForValue().get("shop_status");
        log.info("status:{}", status);
        return Result.success(status);
    }

    @PutMapping("/{status}")
    @ApiOperation("设置营业状态")
    public Result<String> updateStatus(@PathVariable Integer status) {
        redisTemplate.opsForValue().set("shop_status", status);
        return Result.success();
    }
}
