package com.zeyuli.pojo.vo;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 批量上传账单vo
 *
 * @author 李泽聿
 * @since 2025-11-17 20:52
 */
@Data
public class AddBillListVo {
    @NotNull(message = "token不能为空")
    private String token;
    @NotNull(message = "账单列表不能为空")
    private List<AddBillVo> billList;
}
