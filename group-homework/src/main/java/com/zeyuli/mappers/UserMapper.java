package com.zeyuli.mappers;


import com.zeyuli.pojo.po.UserPo;
import jakarta.validation.constraints.NotNull;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 用户持久层接口
 *
 * @author 李泽聿
 * @since 2025-11-14 08:18
 */
@Mapper
public interface UserMapper {
    int register(String id,
                 @NotNull(message = "密码不能为空") String password,
                 @NotNull(message = "用户名不能为空") String username);

    UserPo login(@NotNull(message = "用户名不能为空") String username,
                 @NotNull(message = "密码不能为空") String password);

    UserPo selectUser(String userId);

    int initAccountInfo(String userId,
                        @NotNull(message = "账户不能为空") String account,
                        @NotNull(message = "初始余额不能为空") Double initBalance,
                        @NotNull(message = "账单记录类型不能为空") String description);

    List<UserPo> selectAllUsers();
}
