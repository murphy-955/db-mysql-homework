package com.zeyuli.controller;


import com.zeyuli.pojo.vo.AdminGetUserListVo;
import com.zeyuli.pojo.vo.AdminLoginVo;
import com.zeyuli.pojo.vo.AdminManageUserVo;
import com.zeyuli.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理员控制层
 *
 * @author 李泽聿
 * @since 2025-11-14 08:07
 */
@RestController
@RequestMapping("api/")
public class AdminController {
    @Autowired
    private AdminService adminService;

    @PostMapping("admin/login")
    public Map<String, Object> login(@RequestBody AdminLoginVo vo) {
        return adminService.login(vo.getUsername(), vo.getPassword());
    }

    @PostMapping("admin/getUserList")
    public Map<String, Object> getUserList(@RequestParam("page") Integer page,
                                           @RequestParam("limit") Integer limit,
                                           @RequestBody AdminGetUserListVo vo) {
        return adminService.getUserList(vo, page, limit);
    }

    @PostMapping("admin/getUserInfoCount")
    public Map<String, Object> getUserInfoCount(@RequestParam("limit") Integer limit,
                                                @RequestBody AdminGetUserListVo vo) {
        return adminService.getUserInfoCount(vo, limit);
    }

    @PostMapping("admin/disableUser")
    public Map<String, Object> disableUser(@RequestBody AdminManageUserVo vo) {
        return adminService.disableUser(vo);
    }

    @PostMapping("admin/enableUser")
    public Map<String, Object> enableUser(@RequestBody AdminManageUserVo vo) {
        return adminService.enableUser(vo);
    }

    @PostMapping("admin/deleteUser")
    public Map<String, Object> deleteUser(@RequestBody AdminManageUserVo vo) {
        return adminService.deleteUser(vo);
    }
}
