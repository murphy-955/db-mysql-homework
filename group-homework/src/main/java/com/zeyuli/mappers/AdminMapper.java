package com.zeyuli.mappers;


import com.zeyuli.pojo.po.UserPo;
import jakarta.validation.constraints.NotNull;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 管理员mapper
 *
 * @author 李泽聿
 * @since 2025-11-13 17:12
 */

@Mapper
public interface AdminMapper {
    List<UserPo> getUserList(Integer page, Integer limit);

    int getUserInfoCount(Integer limit);

    int disableUser(@NotNull(message = "userId不能为空") Long userId);

    int enableUser(@NotNull(message = "userId不能为空") Long userId);

    int deleteUser(@NotNull(message = "userId不能为空") Long userId);
}

