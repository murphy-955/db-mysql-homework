package com.zeyuli.asspect;


import com.zeyuli.expection.UserTokenException;
import com.zeyuli.mappers.UserMapper;
import com.zeyuli.pojo.po.UserPo;
import com.zeyuli.util.JwtUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.util.Objects;
import java.util.concurrent.TimeUnit;


/**
 * 校验用户token的横切类
 *
 * @author 李泽聿
 * @since 2025-11-14 14:47
 */
@Aspect
@Component
public class CheckUserTokenAspect {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserMapper userMapper;

    @Value("${cache.redis.baseKey.user.login}")
    private String userBaseKey;

    @Value("${jwt.token.tokenExpiration}")
    private long tokenExpiration;

    private static final Logger log = LoggerFactory.getLogger(CheckUserTokenAspect.class);

    @Pointcut("@annotation(com.zeyuli.annotations.CheckUserToken)")
    public void markedCheckUserToken() {
    }

    @Before("markedCheckUserToken()")
    public void checkUserToken(JoinPoint jp) {
        long start = System.nanoTime();
        String token = JwtUtil.getToken(jp);
        // 校验token是否过期
        if (jwtUtil.isExpiration(token)) {
            throw new UserTokenException("用户token已过期");
        }
        String[] userInfo = jwtUtil.getUserInfo(token);
        String key = userInfo[0]
                .concat(":")
                .concat(userBaseKey);
        String redisToken = (String) redisTemplate.opsForValue().get(key);
        if (redisToken != null && redisToken.equals(token)) {
            log.info("token校验命中缓存，耗时:{}", TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));
            return;
        }
        UserPo res = userMapper.selectUser(userInfo[0]);
        String hash = DigestUtils.md5DigestAsHex(res.getPassword().getBytes()).substring(0, 6);
        if (userInfo[1].equals(res.getUsername())
                && userInfo[2].equals(hash)) {
            redisTemplate.opsForValue().set(key, token, tokenExpiration, TimeUnit.HOURS);
            log.info("token校验，未命中缓存，耗时:{}", TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));
            return;
        }
        throw new UserTokenException("用户token校验失败");
    }


}
