package com.zeyuli.pojo.vo;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 获取统计报表
 *
 * @author 李泽聿
 * @since 2025-12-09 10:12
 */
@Data
public class GetReportVo {
    @NotNull
    private String token;
    @NotNull
    private LocalDate startDate;
    @NotNull
    private LocalDate endDate;
}
