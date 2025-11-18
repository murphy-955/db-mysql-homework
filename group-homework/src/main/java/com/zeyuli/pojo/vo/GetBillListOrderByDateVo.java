package com.zeyuli.pojo.vo;


import com.zeyuli.enm.SearchTypeEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 查询依据日期vo
 *
 * @author 李泽聿
 * @since 2025-11-18 08:26
 */
@Data
public class GetBillListOrderByDateVo {
    @NotNull(message ="token不能为空")
    private String token;
    @NotNull(message = "查询类型不能为空")
    private SearchTypeEnum searchType;
    @NotNull(message = "起始日期不能为空")
    private String startDate;
    @NotNull(message = "结束日期不能为空")
    private String endDate;

    private int page ;
    private int limit;
}
