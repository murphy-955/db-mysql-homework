package com.zeyuli.pojo.po;


import lombok.Data;

/**
 * 用户数据表映射
 *
 * @author 李泽聿
 * @since 2025-11-14 08:56
 */
@Data
public class UserPo {
    private Integer id;
    private String userId;
    private String username;
    private String password;
}
