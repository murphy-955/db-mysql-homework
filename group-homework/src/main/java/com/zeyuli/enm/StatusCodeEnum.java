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
    GET_USER_FAILED(433, "获取用户信息失败"),
    MANAGE_USER_FAILED(434, "管理用户失败"),
    USER_TOKEN_EXPIRED(435, "用户令牌错误"),
    INSERT_BILL_FAILED(436, "新增账单失败"),
    GET_DATA_FAILED(437, "获取数据失败"),
    THE_VALUE_OF_THE_VISIT_DOES_NOT_EXIST(438, "访问值不存在"),
    DELETE_BILL_FAILED(439, "删除账单失败"),
    SEARCH_TYPE_NOT_ALLOWED(440, "查找方式不被允许"),
    INSUFFICIENT_BALANCE(441, "余额不足"),
    INIT_ACCOUNT_INFO_FAILED(442, "初始化账户信息失败"),
    BALANCE_NOT_ENOUGH(443, "余额不足");


    private final Integer statusCode;
    private final String message;
}
