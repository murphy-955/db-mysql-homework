package com.zeyuli.pojo.po;

import com.zeyuli.enm.RecordEnum;
import com.zeyuli.enm.UsageEnum;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 账单映射类
 *
 * @author 李泽聿
 * @since 2025-11-15 15:35
 */
@Data
public class BillPo {
    // 自增主键
    private Long id;
    // 用户id
    private String userId;
    // 花销方式枚举
    private RecordEnum recordEnum;
    // 花销金额
    private BigDecimal amount;
    // 账户
    private String account;
    // 用途枚举
    private UsageEnum type;
    // 日期
    private LocalDate date;
    // 备注
    private String remarks;
    // 是否删除
    private boolean deleted;
}
