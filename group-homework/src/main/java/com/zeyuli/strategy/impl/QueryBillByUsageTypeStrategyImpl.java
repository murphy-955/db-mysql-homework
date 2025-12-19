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
 * 根据{@link QueryBillListTypeEnum#USAGE_TYPE}查询流水
 *
 * @author 李泽聿
 * @since 2025-11-18 14:44
 */
@Component
@RequiredArgsConstructor
public class QueryBillByUsageTypeStrategyImpl implements BillQueryStrategy {
    private final CacheUtil cacheUtil;

    private final QueryBillMapper queryBillMapper;

    private final JwtUtil jwtUtil;

    @Value("${cache.baseKey.billList}")
    private String billListCacheKey;

    /**
     * 根据用途类型查询流水
     *
     * @author : 李泽聿
     * @since : 2025-11-19 08:49
     * @param vo 包含了查询的用途类型，见 {@link com.zeyuli.pojo.vo.GetBillListOrderBySpecificMethodVo}
     * @return : java.util.List<com.zeyuli.pojo.bo.GetBillListBo>
     */
    @Override
    @CheckUserToken
    public List<GetBillListBo> queryBillList(GetBillListOrderBySpecificMethodVo vo) {
        String userId = jwtUtil.getUserInfo(vo.getToken())[0];
        String type = getSearchType();
        String key = String.format("%s:%s:%s:%s:%s-%s",billListCacheKey,
                userId.substring(0,16),
                type,
                vo.getUsageEnum().name(),
                vo.getPage(),
                vo.getLimit()
        );
        List<GetBillListBo> billList = cacheUtil.getBillListOrderBySpecialMethod(key);
        if(!billList.isEmpty()){
            return billList;
        }
        billList = queryBillMapper.queryBillListOrderByUsageType(vo, userId);
        if (!billList.isEmpty()){
            cacheUtil.asyncCacheBillListOrderBySpecificMethod(key,billList);
        }
        cacheUtil.asyncCacheNullKey(key);
        return billList;
    }

    @Override
    public String getSearchType() {
        return QueryBillListTypeEnum.USAGE_TYPE.name();
    }
}
