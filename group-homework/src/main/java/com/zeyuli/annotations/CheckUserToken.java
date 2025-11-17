package com.zeyuli.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 校验用户token<br>
 * 校验用户token的注解,同样。方法的第一个参数必须是携带了token信息的对象
 *
 * @author 李泽聿
 * @since 2025-11-14 14:45
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckUserToken {
}
