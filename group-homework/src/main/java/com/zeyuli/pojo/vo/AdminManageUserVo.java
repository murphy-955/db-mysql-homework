package com.zeyuli.pojo.vo;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 管理员管理用户，包含token以及用户id信息
 *
 * @author 李泽聿
 * @since 2025-11-14 14:11
 */
@Data
public class AdminManageUserVo {
    @NotNull(message = "token不能为空")
    private String token;
    @NotNull(message = "userId不能为空")
    private Long userId;
}
