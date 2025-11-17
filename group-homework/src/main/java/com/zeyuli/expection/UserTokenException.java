package com.zeyuli.expection;


/**
 * 用户token校验异常
 *
 * @author 李泽聿
 * @since 2025-11-15 14:19
 */

public class UserTokenException extends RuntimeException {
    public UserTokenException(String message) {
        super(message);
    }
}
