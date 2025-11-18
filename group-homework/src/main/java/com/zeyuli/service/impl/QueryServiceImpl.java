package com.zeyuli.service.impl;


import com.zeyuli.enm.SearchTypeEnum;
import com.zeyuli.enm.StatusCodeEnum;
import com.zeyuli.pojo.bo.GetBillListBo;
import com.zeyuli.pojo.vo.GetBillListOrderByDateVo;
import com.zeyuli.service.QueryService;
import com.zeyuli.strategy.BillQueryStrategy;
import com.zeyuli.strategy.BillQueryStrategyFactory;
import com.zeyuli.util.Response;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 查询接口实现
 *
 * @author 李泽聿
 * @since 2025-11-17 21:32
 */
@Service
public class QueryServiceImpl implements QueryService {
    /**
     * 获取查询方式
     *
     * @author : 李泽聿
     * @since : 2025-11-17 21:33
     * @return : java.util.Map<java.lang.String,java.lang.Object>
     */
    @Override
    public Map<String, Object> getSearchType() {
        Map<String, Object> res = new HashMap<>();
        for (SearchTypeEnum searchTypeEnum : SearchTypeEnum.values()) {
            res.put(String.valueOf(searchTypeEnum), searchTypeEnum.getDescription());
        }
        return Response.success(StatusCodeEnum.SUCCESS, res);
    }

    /**
     * 查询账单列表,包含
     *
     * @author : 李泽聿
     * @since : 2025-11-18 08:29
     * @param vo 包含了查询的日期范围，见 {@link GetBillListOrderByDateVo}
     * @return : java.util.Map<java.lang.String,java.lang.Object>
     */
    @Override
    public Map<String, Object> getBillList(GetBillListOrderByDateVo vo, SearchTypeEnum searchType) {
        BillQueryStrategy billQueryStrategy = BillQueryStrategyFactory.getBillQueryStrategy(searchType.name());
        List<GetBillListBo> getBillListBos = billQueryStrategy.queryBillList(vo);
        if (getBillListBos.isEmpty()) {
            return Response.success(StatusCodeEnum.SUCCESS, getBillListBos);
        }
        return Response.error(StatusCodeEnum.DELETE_BILL_FAILED, null);
    }
}
