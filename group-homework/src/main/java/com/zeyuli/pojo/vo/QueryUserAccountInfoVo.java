package com.zeyuli.pojo.vo;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 查询用户关联信息的vo
 *
 * @author 李泽聿
 * @since 2025-12-19 15:53
 */
@Data
public class QueryUserAccountInfoVo {
    @NotNull(message = "用户token不能为空")
    private String token;
}
