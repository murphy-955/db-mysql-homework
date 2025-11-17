package com.zeyuli.expection;


/**
 * 缓存穿透异常
 *
 * @author 李泽聿
 * @since 2025-11-17 10:37
 */

public class CachePenetrationExpectation extends RuntimeException {
    public CachePenetrationExpectation(String message) {
        super(message);
    }
}
