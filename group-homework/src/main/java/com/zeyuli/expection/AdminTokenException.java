package com.zeyuli.expection;


/**
 * 处理管理员横切类的异常
 *
 * @author 李泽聿
 * @since 2025-11-14 09:40
 */

public class AdminTokenException extends RuntimeException {
    public AdminTokenException(String message) {
        super(message);
    }
}
