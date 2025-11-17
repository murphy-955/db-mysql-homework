package com.zeyuli.pojo.vo;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户查询账单vo<br>
 * 包含{@code /api/bill/getBillList?page=1&limit=10}，{@code /api/bill/getDeletedBillList?page=1&limit=10} 接口
 *
 * @author 李泽聿
 * @since 2025-11-16 14:26
 */
@Data
public class UserQueryBillVo {
    @NotNull(message = "token不能为空")
    private String token;
}
