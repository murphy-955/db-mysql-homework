package com.zeyuli.pojo.vo;


import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.lang.NonNull;

/**
 * 用户注册Vo
 *
 * @author 李泽聿
 * @since 2025-11-14 08:10
 */
@Data
public class UserRegisterVo {
    @NotNull(message = "用户名不能为空")
    private String username;
    @NotNull(message = "密码不能为空")
    private String password;
}
