package com.zeyuli.pojo.vo;

import com.zeyuli.enm.UsageEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 查询依据日期vo
 *
 * @author 李泽聿
 * @since 2025-11-18 08:26
 */
@Data
public class GetBillListOrderBySpecificMethodVo {
    @NotNull(message ="token不能为空")
    private String token;

    @NotNull(message = "页码不能为空")
    private int page ;
    @NotNull(message = "每页数量不能为空")
    private int limit;

    private String startDate;
    private String endDate;

    // 最小金额
    private BigDecimal minAmount;
    // 最大金额
    private BigDecimal maxAmount;

    // 用途
    private UsageEnum usageEnum;

    // 账户id
    private String accountId;
    // 关键字
    private String keyword;
}
