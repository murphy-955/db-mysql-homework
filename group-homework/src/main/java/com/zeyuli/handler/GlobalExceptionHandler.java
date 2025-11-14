package com.zeyuli.handler;

import com.zeyuli.enm.StatusCodeEnum;
import com.zeyuli.expection.*;
import com.zeyuli.util.Response;
import com.zeyuli.expection.AdminTokenException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.naming.CommunicationException;
import java.util.Map;

/**
 * 全局异常处理器，用来处理Token过期异常
 *
 * @author 李泽聿
 * @since 2025-11-14 10:50:00
 */

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AdminTokenException.class)
    @ResponseBody
    public Map<String, Object> handleAdminTokenException(AdminTokenException e) {
        return Response.error(StatusCodeEnum.ADMIN_TOKEN_EXPIRED, e.getMessage());
    }

}
