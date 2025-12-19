package com.zeyuli.strategy.impl;


import com.zeyuli.annotations.CheckUserToken;
import com.zeyuli.enm.QueryBillListTypeEnum;
import com.zeyuli.mappers.QueryBillMapper;
import com.zeyuli.pojo.bo.GetBillListBo;
import com.zeyuli.pojo.vo.GetBillListOrderBySpecificMethodVo;
import com.zeyuli.strategy.BillQueryStrategy;
import com.zeyuli.util.CacheUtil;
import com.zeyuli.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 根据金额范围查询账单列表
 *
 * @author 李泽聿
 * @since 2025-11-19 07:57
 */
@Component
@RequiredArgsConstructor
public class QueryBillByAmountRangeStrategyImpl implements BillQueryStrategy {
    private final CacheUtil cacheUtil;

    private final QueryBillMapper queryBillMapper;

    private final JwtUtil jwtUtil;

    @Value("${cache.baseKey.billList}")
    private String billListCacheKey;

    /**
     * 根据金额范围查询账单列表
     *
     * @author : 李泽聿
     * @since : 2025-11-19 08:02
     * @param vo 查询账单列表参数
     * @return : java.util.List<com.zeyuli.pojo.bo.GetBillListBo>
     */
    @Override
    @CheckUserToken
    public List<GetBillListBo> queryBillList(GetBillListOrderBySpecificMethodVo vo) {
        String userId = jwtUtil.getUserInfo(vo.getToken())[0];
        String type = getSearchType();
        String key = String.format("%s:%s:%s:%s-%s:%s-%s",billListCacheKey,
                userId.substring(0,16),
                type,
                vo.getMinAmount().toString(),
                vo.getMaxAmount().toString(),
                vo.getPage(),
                vo.getLimit()
        );
        // 从缓存中获取账单列表
        List<GetBillListBo> billList = cacheUtil.getBillListOrderBySpecialMethod(key);
        if (!billList.isEmpty()) {
            return billList;
        }

        // 从数据库查询列表
         billList = queryBillMapper.queryBillListOrderByAmountRange(vo, userId);
        if (billList != null) {
            cacheUtil.asyncCacheBillListOrderBySpecificMethod(key, billList);
            return billList;
        }
        cacheUtil.asyncCacheNullKey(key);
        return null;
    }

    @Override
    public String getSearchType() {
        return QueryBillListTypeEnum.AMOUNT_RANGE.name();
    }
}
