package com.zeyuli.pojo.bo;


import com.zeyuli.enm.RecordEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 缓存数据以及db数据分页的映射
 *
 * @author 李泽聿
 * @since 2025-11-17 08:17
 */
@Data
public class GetBillListBo {
    private String id;
    private RecordEnum recordEnum;
    private BigDecimal amount;
    private LocalDate date;
}
