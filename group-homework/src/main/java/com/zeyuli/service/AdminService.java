package com.zeyuli.service;


import com.zeyuli.pojo.vo.AdminGetUserListVo;

import java.util.Map;

/**
 * 管理员服务层接口
 *
 * @author 李泽聿
 * @since 2025-11-14 09:06
 */

public interface AdminService {
    Map<String, Object> login(String username, String password);

    Map<String, Object> getUserList(AdminGetUserListVo vo, Integer page, Integer limit);

    Map<String, Object> getUserInfoCount(AdminGetUserListVo vo, Integer limit);
}
