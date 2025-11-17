package com.zeyuli.enm;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 账目用途枚举
 *
 * @author 李泽聿
 * @since 2025-11-14 14:34
 */
@AllArgsConstructor
@Getter
public enum UsageEnum {
    FOOD("食物"),
    SHOPPING("购物"),
    TRANSPORTATION("交通"),
    HOUSING("住房"),
    HEALTH("建康"),
    FINANCE("金融"),
    INSURANCE("保险"),
    CLOTHING("服装"),
    ELECTRONIC("电子"),
    FURNITURE("家具"),
    HOUSEHOLD("家庭"),
    OTHER("其他");

    private final String description;
}
