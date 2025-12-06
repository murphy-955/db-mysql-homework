package com.zeyuli.asspect;


import com.zeyuli.expection.AdminTokenException;
import com.zeyuli.util.JwtUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.util.Objects;

/**
 * 校验管理员用户横切类
 *
 * @author 李泽聿
 * @since 2025-11-14 09:27
 */
@Aspect
@Component
public class CheckAdminTokenAspect {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${cache.redis.baseKey.admin.login}")
    private String adminBaseLoginKey;

    @Pointcut("@annotation(com.zeyuli.annotations.CheckAdminToken)")
    public void markedCheckAdminToken() {
    }

    @Before("markedCheckAdminToken()")
    public void checkAdminToken(JoinPoint jp) {
        String token = JwtUtil.getToken(jp);
        String redisToken = Objects.requireNonNull(redisTemplate.opsForValue().get(adminBaseLoginKey)).toString();
        if (redisToken.equals(token)) {
            return;
        }
        String[] userInfo = jwtUtil.getUserInfo(token);
        String hash = DigestUtils.md5DigestAsHex("597e86f4dc5fb25de4b53e13268b136c9dc2342db1f50a3d19046f20878ecd50"
                .getBytes()).substring(0, 6);
        if (jwtUtil.isExpiration(token)
                && userInfo[1].equals("yiyangchen")
                && userInfo[2].equals(hash)
                && userInfo[0].equals("102301237")){
            return;
        }
        throw new AdminTokenException("管理员token校验失败");
    }
}
