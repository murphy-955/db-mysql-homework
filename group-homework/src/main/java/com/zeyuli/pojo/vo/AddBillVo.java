package com.zeyuli.pojo.vo;


import com.zeyuli.enm.RecordEnum;
import com.zeyuli.enm.UsageEnum;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 添加账单的vo
 *
 * @author 李泽聿
 * @since 2025-11-14 14:27
 */
@Data
public class AddBillVo {
    @NotNull(message = "token不能为空")
    private String token;

    @NotNull(message = "记录类型不能为空")
    private RecordEnum recordEnum;

    @NotNull(message = "金额不能为空")
    private BigDecimal amount;

    @NotNull(message = "账户不能为空")
    private String account;

    @NotNull(message = "类型不能为空")
    private UsageEnum type;

    @NotNull(message = "日期不能为空")
    @PastOrPresent(message = "日期必须是过去或现在的时间")
    private LocalDate date;

    private String remarks;

    private String userId;
}
