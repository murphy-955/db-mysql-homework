package com.zeyuli.mappers;


import com.zeyuli.pojo.bo.GetBillListBo;
import com.zeyuli.pojo.vo.GetBillListOrderBySpecificMethodVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 查询持久层
 *
 * @author 李泽聿
 * @since 2025-11-18 11:29
 */
@Mapper
public interface QueryBillMapper {
    List<GetBillListBo> queryBillListOrderByDate(GetBillListOrderBySpecificMethodVo vo, String userId);

    List<GetBillListBo> queryBillListOrderByAccount(GetBillListOrderBySpecificMethodVo vo, String userId);

    List<GetBillListBo> queryBillListOrderByAmountRange(GetBillListOrderBySpecificMethodVo vo, String userId);

    List<GetBillListBo> queryBillListOrderByUsageType(GetBillListOrderBySpecificMethodVo vo, String userId);

    List<GetBillListBo> queryBillListOrderByKeyword(GetBillListOrderBySpecificMethodVo vo, String userId);
}
