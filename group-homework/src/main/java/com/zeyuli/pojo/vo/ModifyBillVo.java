package com.zeyuli.pojo.vo;


import com.zeyuli.enm.RecordEnum;
import com.zeyuli.enm.UsageEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 修改账单的vo
 *
 * @author 李泽聿
 * @since 2025-11-17 20:11
 */
@Data
public class ModifyBillVo {
    @NotNull(message = "账单token不能为空")
    private String token;

    @NotNull(message = "账单id不能为空")
    private Long id;

    @NotNull(message ="账单类型不能为空")
    private RecordEnum recordEnum;

    @NotNull(message = "金额不能为空")
    private Double amount;

    // 账户
    private String account;

    @NotNull(message = "类型不能为空")
    private UsageEnum type;

    @NotNull(message = "日期不能为空")
    private LocalDate date;

    private String remarks;

}
