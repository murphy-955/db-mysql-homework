package com.zeyuli.pojo.vo;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户登录Vo
 *
 * @author 李泽聿
 * @since 2025-11-14 08:52
 */
@Data
public class UserLoginVo {
    @NotNull(message = "用户名不能为空")
    private String username;
    @NotNull(message = "密码不能为空")
    private String password;
}
