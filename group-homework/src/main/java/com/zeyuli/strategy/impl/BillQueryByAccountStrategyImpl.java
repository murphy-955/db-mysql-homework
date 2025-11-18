package com.zeyuli.strategy.impl;


import com.zeyuli.annotations.CheckUserToken;
import com.zeyuli.enm.SearchTypeEnum;
import com.zeyuli.mappers.QueryBillMapper;
import com.zeyuli.pojo.bo.GetBillListBo;
import com.zeyuli.pojo.vo.GetBillListOrderByDateVo;
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
public class BillQueryByAccountStrategyImpl implements BillQueryStrategy {
    @Autowired
    private CacheUtil cacheUtil;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private QueryBillMapper queryBillMapper;

    @Value("${cache.baseKey.billList}")
    private String billListCacheKey;

    @Override
    @CheckUserToken
    public List<GetBillListBo> queryBillList(GetBillListOrderByDateVo vo) {
        String type = vo.getSearchType().name();
        String userId = jwtUtil.getUserInfo(vo.getToken())[0];
        // 缓存key
        // 格式：bill:list:id（前16位）:DATE_RANGE:startDate:endDate
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
        billList = queryBillMapper.queryBillListOrderByAccount(vo);
        if (!billList.isEmpty()) {
            cacheUtil.asyncCacheHotBillListOrderByDate(key, billList);
            return billList;
        }
        // 4. 缓存空值
        cacheUtil.asyncCacheNullKey(key);
        return null;
    }

    @Override
    public String getSearchType() {
        return SearchTypeEnum.ACCOUNT.name();
    }
}
