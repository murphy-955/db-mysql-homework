package com.zeyuli.strategy.impl;

import com.zeyuli.enm.SearchTypeEnum;
import com.zeyuli.enm.UsageEnum;
import com.zeyuli.pojo.bo.GetBillListBo;
import com.zeyuli.pojo.vo.GetBillListOrderByDateVo;
import com.zeyuli.strategy.BillQueryStrategy;

import java.util.List;

/**
 * 根据{@link SearchTypeEnum#USAGE_TYPE}查询流水
 *
 * @author 李泽聿
 * @since 2025-11-18 14:44
 */

public class QueryBillByUsageTypeStrategyImpl implements BillQueryStrategy {


    @Override
    public List<GetBillListBo> queryBillList(GetBillListOrderByDateVo vo) {
        return List.of();
    }

    @Override
    public String getSearchType() {
        return SearchTypeEnum.USAGE_TYPE.name();
    }
}
