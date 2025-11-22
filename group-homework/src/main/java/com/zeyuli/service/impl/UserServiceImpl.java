package com.zeyuli.service.impl;


import com.zeyuli.annotations.CheckUserToken;
import com.zeyuli.enm.StatusCodeEnum;
import com.zeyuli.mappers.UserMapper;
import com.zeyuli.pojo.po.UserPo;
import com.zeyuli.pojo.vo.InitAccountInfoVo;
import com.zeyuli.pojo.vo.UserLoginVo;
import com.zeyuli.pojo.vo.UserRegisterVo;
import com.zeyuli.service.UserService;
import com.zeyuli.util.CacheUtil;
import com.zeyuli.util.JwtUtil;
import com.zeyuli.util.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 用户业务层
 *
 * @author 李泽聿
 * @since 2025-11-14 08:17
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${jwt.token.tokenExpiration}")
    private long tokenExpiration;

    @Value("${cache.redis.baseKey.user.login}")
    private String userBaseLoginKey;
    @Autowired
    private CacheUtil cacheUtil;

    /**
     * 用户注册
     *
     * @param vo {@link UserRegisterVo}
     * @return : java.util.Map<java.lang.String,java.lang.Object>
     * @author : 李泽聿
     * @since : 2025-11-14 08:53
     */
    @Override
    public Map<String, Object> register(UserRegisterVo vo) {
        // 获取年月日
        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String id = UUID.randomUUID().toString().replaceAll("-", "");
        id = date.concat(id);
        int res = userMapper.register(id, vo.getPassword(), vo.getUsername());
        if (res > 0) {
            String token = jwtUtil.createToken(id, vo.getUsername(), vo.getPassword());
            String key = id
                    .concat(":")
                    .concat(userBaseLoginKey);
            // 设置token为===>>murphy_955:user:token:返回的token
            // 设置token的过期时间为24小时
            redisTemplate.opsForValue().set(key, token, tokenExpiration, TimeUnit.HOURS);

            return Response.success(token);
        }
        return Response.error(StatusCodeEnum.REGISTER_FAILED, null);
    }

    /**
     * 用户登录<br>
     * 返回token，并将toke存入redis中
     *
     * @param vo {@link UserLoginVo}
     * @return : java.util.Map<java.lang.String,java.lang.Object>
     * @author : 李泽聿
     * @since : 2025-11-14 08:54
     */
    @Override
    public Map<String, Object> login(UserLoginVo vo) {
        UserPo res = userMapper.login(vo.getUsername(), vo.getPassword());
        if (res != null) {
            String token = jwtUtil.createToken(res.getUserId(), res.getUsername(), res.getPassword());
            String key = res.getUserId()
                    .concat(":")
                    .concat(userBaseLoginKey);
            // 设置token为===>>murphy_955:user:token:返回的token
            // 设置token的过期时间为24小时
            redisTemplate.opsForValue().set(key, token, tokenExpiration, TimeUnit.HOURS);

            return Response.success(token);
        }
        return Response.error(StatusCodeEnum.LOGIN_FAILED, null);
    }

    /**
     * 初始化用户账户信息<br>
     * 包括设置初始密码，设置初始余额，设置初始账单记录类型
     *
     * @param vo {@link InitAccountInfoVo}
     * @return : java.util.Map<java.lang.String,java.lang.Object>
     * @author : 李泽聿
     * @since : 2025-11-22 13:44
     */
    @Override
    @CheckUserToken
    public Map<String, Object> initAccountInfo(InitAccountInfoVo vo) {
        String userId = jwtUtil.getUserInfo(vo.getToken())[0];
        int res = userMapper.initAccountInfo(userId, vo.getAccount(), vo.getBalance(), vo.getDescription());
        if (res > 0) {
            // 拼接的key：
            redisTemplate.opsForValue().set("user:" + userId.substring(0,16) + "account:" + vo.getAccount(), vo);
            return Response.success(null);
        }
        return Response.error(StatusCodeEnum.INIT_ACCOUNT_INFO_FAILED);
    }
}
