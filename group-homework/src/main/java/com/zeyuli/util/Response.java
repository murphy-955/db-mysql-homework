package com.zeyuli.util;


import com.zeyuli.enm.StatusCodeEnum;

import java.util.HashMap;
import java.util.Map;

/**
 * 响应体
 *
 * @author 李泽聿
 * @since 2025-11-14 07:58
 */

public class Response {
    public static Map<String, Object> success() {
        HashMap<String, Object> res = new HashMap<>();
        res.put("statusCode", 200);
        res.put("message", "成功");
        return res;
    }

    public static Map<String, Object> success(Object data) {
        HashMap<String, Object> res = (HashMap<String, Object>) success();
        res.put("data", data);
        return res;
    }

    public static Map<String, Object> success(StatusCodeEnum statusCode, Object data){
        HashMap<String, Object> res = new HashMap<>();
        res.put("statusCode", statusCode.getStatusCode());
        res.put("message", statusCode.getMessage());
        res.put("data", data);
        return res;
    }

    public static Map<String, Object> error(StatusCodeEnum statusCode){
        HashMap<String, Object> res = new HashMap<>();
        res.put("statusCode", statusCode.getStatusCode());
        res.put("message", statusCode.getMessage());
        return res;
    }

    public static Map<String, Object> error(StatusCodeEnum statusCode, Object data){
        HashMap<String, Object> res = (HashMap<String, Object>) error(statusCode);
        res.put("data", data);
        return res;
    }
}
