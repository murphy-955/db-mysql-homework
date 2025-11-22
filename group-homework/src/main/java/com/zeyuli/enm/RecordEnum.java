package com.zeyuli.enm;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 账单记录的枚举<br>
 * 包含了支出，收入2种类型
 *
 * @author 李泽聿
 * @since 2025-11-14 14:28
 */
@Getter
@AllArgsConstructor
public enum RecordEnum {
    INCOME("收入"),
    EXPENDITURE("支出");

    private final String recordDescription;
}
