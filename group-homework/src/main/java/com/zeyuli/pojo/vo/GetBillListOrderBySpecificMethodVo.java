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

    private BigDecimal minAmount;
    private BigDecimal maxAmount;

    private UsageEnum usageEnum;
}
