package com.zeyuli.enm;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 查询方式枚举
 *
 * @author 李泽聿
 * @since 2025-11-17 21:18
 */
@AllArgsConstructor
@Getter
public enum QueryBillListTypeEnum {
    DATE_RANGE( "日期范围查询"),
    AMOUNT_RANGE( "金额范围查询"),
    USAGE_TYPE( "用途类型查询"),
    // TODO 待实现
    KEYWORD( "关键词查询"),
    ACCOUNT( "账户查询");
    private final String description;
}

