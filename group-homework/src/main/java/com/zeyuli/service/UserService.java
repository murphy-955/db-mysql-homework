package com.zeyuli.service;


import com.zeyuli.pojo.vo.InitAccountInfoVo;
import com.zeyuli.pojo.vo.QueryUserAccountInfoVo;
import com.zeyuli.pojo.vo.UserLoginVo;
import com.zeyuli.pojo.vo.UserRegisterVo;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 用户业务层
 *
 * @author 李泽聿
 * @since 2025-11-14 08:15
 */
@Service
public interface UserService {
    Map<String, Object> register(UserRegisterVo vo);

    Map<String, Object> login(UserLoginVo vo);

    Map<String, Object> initAccountInfo(InitAccountInfoVo vo);

    Map<String, Object> getUserAccountInfo(QueryUserAccountInfoVo vo);
}
