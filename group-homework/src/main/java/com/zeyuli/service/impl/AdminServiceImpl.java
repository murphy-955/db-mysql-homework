package com.zeyuli.service.impl;


import com.zeyuli.annotations.CheckAdminToken;
import com.zeyuli.enm.StatusCodeEnum;
import com.zeyuli.mappers.AdminMapper;
import com.zeyuli.pojo.po.UserPo;
import com.zeyuli.pojo.vo.AdminGetUserListVo;
import com.zeyuli.service.AdminService;
import com.zeyuli.util.JwtUtil;
import com.zeyuli.util.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员业务实现
 *
 * @author 李泽聿
 * @since 2025-11-14 09:07
 */
@Service
public class AdminServiceImpl implements AdminService {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${redis.baseKey.admin.login}")
    private String adminBaseKey;

    /**
     * 直接写死，水项目，没必要那么认真O(∩_∩)O
     *
     * @param username yiyangchen
     * @param password yiyangchenan10086dzeyulidbdjob10086
     * @return : java.util.Map<java.lang.String,java.lang.Object>
     * @author : 李泽聿
     * @since : 2025-11-14 09:25
     */
    @Override
    public Map<String, Object> login(String username, String password) {
        if (username.equals("yiyangchen") &&
                password.equals("597e86f4dc5fb25de4b53e13268b136c9dc2342db1f50a3d19046f20878ecd50")) {
            String token = jwtUtil.createToken("102301237", username, password);

            redisTemplate.opsForValue().set(adminBaseKey, token);
            Map<String, Object> res = new HashMap<>();
            res.put("token", token);
            return Response.success(res);
        }
        return Response.error(StatusCodeEnum.LOGIN_FAILED);
    }

    /**
     * 获取用户列表<br>
     * 包含了用户id，用户名，是否禁用的信息
     *
     * @author : 李泽聿
     * @since : 2025-11-14 10:31
     * @param vo 管理员token
     * @param page 页码
     * @param limit 每页条数
     * @return : java.util.Map<java.lang.String,java.lang.Object>
     */
    @Override
    @CheckAdminToken
    public Map<String, Object> getUserList(AdminGetUserListVo vo, Integer page, Integer limit) {
        List<UserPo> userList = adminMapper.getUserList(page, limit);
        if (userList == null || userList.isEmpty()) {
            return Response.error(StatusCodeEnum.GET_USER_FAILED);
        }
        return Response.success(userList);
    }

    /**
     * 返回总条数
     *
     * @param vo 管理员获取用户信息
     * @return : java.util.Map<java.lang.String,java.lang.Object>
     * @author : 李泽聿
     * @since : 2025-11-14 10:26
     */
    @Override
    @CheckAdminToken
    public Map<String, Object> getUserInfoCount(AdminGetUserListVo vo, Integer limit) {
        int count = adminMapper.getUserInfoCount(limit);
        return Response.success(count / limit + 1);
    }
}
