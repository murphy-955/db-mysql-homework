package com.zeyuli.asspect;


import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.lang.annotation.Target;

/**
 * 日志输出横切类
 *
 * @author 李泽聿
 * @since 2025-11-20 08:29
 */
@Aspect
@Slf4j
public class LogOutputAspect {
    @Pointcut("@annotation(com.zeyuli.annotations.LogOutPut)")
    public void logOutputPointcut() {
    }

    /**
     * 日志输出环绕通知<br>
     * 捕获所有可能出现的异常
     *
     * @param joinPoint 连接点
     * @author : 李泽聿
     * @since : 2025-11-20 08:31
     */
    @Around("logOutputPointcut()")
    public Object logOutputAround(ProceedingJoinPoint joinPoint) {
        Object result = null;
        try {
            // 执行连接点方法
            result = joinPoint.proceed();
            // 记录日志
            log.info("方法 {} 执行成功，返回值为 {}", joinPoint.getSignature().getName(), result);
            return result;
        } catch (Throwable throwable) {
            // 记录异常日志
            log.error("方法 {} 执行异常：{}", joinPoint.getSignature().getName(), throwable.getMessage(), throwable);
        }
        return result;
    }
}
