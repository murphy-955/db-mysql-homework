package com.zeyuli.strategy;


import com.zeyuli.pojo.bo.GetBillListBo;
import com.zeyuli.pojo.vo.GetBillListOrderBySpecificMethodVo;

import java.util.List;

/**
 * {@code api/query/getBillList}接口的策略接口
 *
 * @author 李泽聿
 * @since 2025-11-18 08:37
 */

public interface BillQueryStrategy {
    /**
     * 根据日期范围查询账单列表
     *
     * @param vo    包含了查询的日期范围，见 {@link GetBillListOrderBySpecificMethodVo}
     * @return : java.util.List<com.zeyuli.pojo.bo.GetBillListBo>
     * @author : 李泽聿
     * @since : 2025-11-18 08:29
     */
    List<GetBillListBo> queryBillList(GetBillListOrderBySpecificMethodVo vo);

    /**
     * 获取查询策略的类型
     *
     * @return : java.lang.String
     * @author : 李泽聿
     * @since : 2025-11-18 08:37
     */
    String getSearchType();
}
