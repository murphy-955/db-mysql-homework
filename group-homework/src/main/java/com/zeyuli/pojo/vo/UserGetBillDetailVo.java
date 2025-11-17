package com.zeyuli.pojo.vo;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 用户获取详情账单<br>
 * <b>请求地址：`/api/bill/getBillDetail`</b>
 *
 * @author 李泽聿
 * @since 2025-11-17 09:46
 */
@Data
public class UserGetBillDetailVo {
    @NotNull(message = "token")
    private Long token;
    @NotNull(message = "账单ID不能为空")
    private Long id;
    @NotNull(message = "日期不能为空")
    private LocalDate date;
}
