package com.zeyuli.service;


import com.zeyuli.enm.QueryBillListTypeEnum;
import com.zeyuli.pojo.vo.GetBillListOrderBySpecificMethodVo;
import com.zeyuli.pojo.vo.GetReportVo;

import java.util.Map;

/**
 * 查询接口
 *
 * @author 李泽聿
 * @since 2025-11-17 21:30
 */
public interface QueryService {

    Map<String, Object> getSearchType();

    Map<String, Object> getBillList(GetBillListOrderBySpecificMethodVo vo, QueryBillListTypeEnum searchType);

    Map<String, Object> getReport(GetReportVo vo);
}



