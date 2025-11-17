package com.zeyuli.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记校验管理员token的注解<br>
 * 注意接受的横切的第一个参数必须是包含了token的类
 *
 * @author 李泽聿
 * @since 2025-11-14 09:31
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckAdminToken {
}
