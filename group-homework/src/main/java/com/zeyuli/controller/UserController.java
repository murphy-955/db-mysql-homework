package com.zeyuli.controller;


import com.zeyuli.pojo.vo.InitAccountInfoVo;
import com.zeyuli.pojo.vo.UserLoginVo;
import com.zeyuli.pojo.vo.UserRegisterVo;
import com.zeyuli.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户控制层
 *
 * @author 李泽聿
 * @since 2025-11-14 08:08
 */
@RestController
@RequestMapping("api/user")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("register")
    public Map<String, Object> register(@RequestBody UserRegisterVo vo) {
        return userService.register(vo);
    }

    @PostMapping("login")
    public Map<String, Object> login(@RequestBody UserLoginVo vo) {
        return userService.login(vo);
    }

    @PostMapping("initAccountInfo")
    public Map<String, Object> initAccountInfo(@RequestBody InitAccountInfoVo vo) {
        return userService.initAccountInfo(vo);
    }
}
