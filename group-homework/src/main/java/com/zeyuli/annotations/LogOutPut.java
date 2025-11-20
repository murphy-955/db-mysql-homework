package com.zeyuli.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 捕获当前方法可能出现的异常并记录日志
 *
 * @author 李泽聿
 * @since 2025-11-20 08:27
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LogOutPut {
}
