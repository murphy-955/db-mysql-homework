package com.zeyuli.expection;


/**
 * 查找方式不被允许异常
 *
 * @author 李泽聿
 * @since 2025-11-18 09:09
 */

public class SearchTypeNotAllowedExpectation extends RuntimeException {
    public SearchTypeNotAllowedExpectation(String message) {
        super(message);
    }
}
