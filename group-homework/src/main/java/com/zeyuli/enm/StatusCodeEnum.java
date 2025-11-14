package com.zeyuli.enm;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 状态码枚举
 *
 * @author 李泽聿
 * @since 2025-11-14 08:01
 */
@AllArgsConstructor
@Getter
public enum StatusCodeEnum {
    SUCCESS(200, "成功"),
    REGISTER_FAILED(430, "注册失败"),
    LOGIN_FAILED(431, "登录失败"),
    ADMIN_TOKEN_EXPIRED(432, "管理员令牌错误"),
    GET_USER_FAILED(433, "获取用户信息失败");


    private final Integer statusCode;
    private final String message;
}
