package com.zeyuli.service;


import com.zeyuli.enm.SearchTypeEnum;
import com.zeyuli.pojo.vo.GetBillListOrderByDateVo;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 查询接口
 *
 * @author 李泽聿
 * @since 2025-11-17 21:30
 */
public interface QueryService {

    Map<String, Object> getSearchType();

    Map<String, Object> getBillList(GetBillListOrderByDateVo vo, SearchTypeEnum searchType);
}



