package com.zeyuli.pojo.po;


import lombok.Data;

/**
 * 用户账单
 *
 * @author 李泽聿
 * @since 2025-12-11 09:27
 */
@Data
public class UserAccountPo {
    private String id;
    private String userId;
    private String account;
    private String balance;
    private String description;
}
