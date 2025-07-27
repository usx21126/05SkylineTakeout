package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

@Mapper
public interface UserMapper {
    User getUserByOpenid(String openid);

    void addUser(User user);

    /**
     * 根据id查询用户
     * @param id
     * @return
     */
    User getUserById(Long id);

    Integer getTotalUserByDate(Map map);
}
