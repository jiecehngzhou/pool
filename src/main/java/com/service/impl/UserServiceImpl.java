package com.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pojo.User;
import com.service.UserService;
import com.mapper.UserMapper;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Arrays;


@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {



}




