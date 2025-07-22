package com.itheima.controller;

import com.itheima.entity.User;
import com.itheima.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @CachePut(cacheNames = "user",key = "#result.id")
    @PostMapping
    public User save(@RequestBody User user){
        userMapper.insert(user);
        return user;
    }
    @CacheEvict(cacheNames = "user",key = "#id")
    @DeleteMapping
    public void deleteById(Long id){
        userMapper.deleteById(id);
    }
    @CacheEvict(cacheNames = "user",allEntries = true)
	@DeleteMapping("/delAll")
    public void deleteAll(){
        userMapper.deleteAll();
    }

    @Cacheable(cacheNames = "user",key = "#id")
    @GetMapping
    public User getById(Long id){
        User user = userMapper.getById(id);
        return user;
    }

}
