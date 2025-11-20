package com.zeyuli.pojo.vo;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 初始化用户账户信息
 *
 * @author 李泽聿
 * @since 2025-11-20 08:57
 */
@Data
public class InitAccountInfoVo {
    @NotNull(message = "用户token不能为空")
    private String token;
    @NotNull(message = "用户账户不能为空")
    private String account;
    @NotNull(message = "账户余额不能为空")
    private Double balance;
    @Size(max = 50, message = "账户描述不能超过50个字符")
    private String description;
}
