package com.zeyuli.mappers;


import com.zeyuli.pojo.bo.GetBillListBo;
import com.zeyuli.pojo.vo.GetBillListOrderBySpecificMethodVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 查询持久层
 *
 * @author 李泽聿
 * @since 2025-11-18 11:29
 */
@Mapper
public interface QueryBillMapper {
    List<GetBillListBo> queryBillListOrderByDate(
            @Param("vo") GetBillListOrderBySpecificMethodVo vo,
            @Param("userId") String userId);

    List<GetBillListBo> queryBillListOrderByAccount(
            @Param("vo") GetBillListOrderBySpecificMethodVo vo,
            @Param("userId") String userId);

    List<GetBillListBo> queryBillListOrderByAmountRange(
            @Param("vo") GetBillListOrderBySpecificMethodVo vo,
            @Param("userId") String userId);

    List<GetBillListBo> queryBillListOrderByUsageType(
            @Param("vo") GetBillListOrderBySpecificMethodVo vo,
            @Param("userId") String userId);

    List<GetBillListBo> queryBillListOrderByKeyword(
            @Param("vo") GetBillListOrderBySpecificMethodVo vo,
            @Param("userId") String userId);
}
