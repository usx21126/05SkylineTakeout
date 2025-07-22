package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
@Slf4j
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private UserMapper userMapper;
    private static final String WX_URL = "https://api.weixin.qq.com/sns/jscode2session";
    @Override
    public User login(UserLoginDTO userLoginDTO) {
        // 1.HttpClient 发送登录验证请求
        Map<String, String> params = new HashMap<String, String>();
        params.put("appid",weChatProperties.getAppid());
        params.put("secret",weChatProperties.getSecret());
        params.put("js_code",userLoginDTO.getCode());
        params.put("gran_type","authorization_code");
        String res = HttpClientUtil.doGet(WX_URL,params);
        // 2.解析响应结果
        log.info("res={}",res);
        JSONObject jsonObject = JSONObject.parseObject(res);
        String openid = jsonObject.getString("openid");
        if(openid==null){
            throw new LoginFailedException(MessageConstant.USER_NOT_LOGIN);
        }
        // 3.判断是否为新用户
        User user = userMapper.getUserByOpenid(openid);
        if(user==null){
            user = User.builder().openid(openid).createTime(LocalDateTime.now()).build();
            userMapper.addUser(user);
        }
        return user;
    }
}
