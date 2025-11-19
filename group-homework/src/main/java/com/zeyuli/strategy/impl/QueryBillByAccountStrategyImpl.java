package com.zeyuli.strategy.impl;


import com.zeyuli.annotations.CheckUserToken;
import com.zeyuli.enm.QueryBillListTypeEnum;
import com.zeyuli.mappers.QueryBillMapper;
import com.zeyuli.pojo.bo.GetBillListBo;
import com.zeyuli.pojo.vo.GetBillListOrderBySpecificMethodVo;
import com.zeyuli.strategy.BillQueryStrategy;
import com.zeyuli.util.CacheUtil;
import com.zeyuli.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 根据账户查询流式策略实现
 *
 * @author 李泽聿
 * @since 2025-11-18 14:11
 */
@Component
public class QueryBillByAccountStrategyImpl implements BillQueryStrategy {
    @Autowired
    private CacheUtil cacheUtil;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private QueryBillMapper queryBillMapper;

    @Value("${cache.baseKey.billList}")
    private String billListCacheKey;

    /**
     * 根据账户查询账单列表
     *
     * @author : 李泽聿
     * @since : 2025-11-19 07:56
     * @param vo 账单查询参数
     * @return : java.util.List<com.zeyuli.pojo.bo.GetBillListBo>
     */
    @Override
    @CheckUserToken
    public List<GetBillListBo> queryBillList(GetBillListOrderBySpecificMethodVo vo) {
        String type = getSearchType();
        String userId = jwtUtil.getUserInfo(vo.getToken())[0];
        // 缓存key
        // 格式：bill:list:id（前16位）:ACCOUNT:startDate:endDate
        String key = String.format(billListCacheKey,
                userId.substring(0,16),
                type,
                vo.getEndDate().replace("-",""),
                vo.getStartDate().replace("-",""),
                vo.getPage(),
                vo.getLimit()
        );
        List<GetBillListBo> billList = cacheUtil.getBillListOrderBySpecialMethod(key);
        if (billList != null) {
            return billList;
        }
        billList = queryBillMapper.queryBillListOrderByAccount(vo, userId);
        if (!billList.isEmpty()) {
            cacheUtil.asyncCacheBillListOrderBySpecificMethod(key, billList);
            return billList;
        }
        // 4. 缓存空值
        cacheUtil.asyncCacheNullKey(key);
        return null;
    }

    @Override
    public String getSearchType() {
        return QueryBillListTypeEnum.ACCOUNT.name();
    }
}
