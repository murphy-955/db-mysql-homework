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
 * 根据关键字查询
 *
 * @author 李泽聿
 * @since 2025-11-19 09:01
 */
@Component
@RequiredArgsConstructor
public class QueryBillOrderByKeywordStrategyImpl implements BillQueryStrategy {
    private final JwtUtil jwtUtil;

    private final QueryBillMapper queryBillMapper;

    private final CacheUtil cacheUtil;

    @Value("${cache.baseKey.billList}")
    private String baseCacheBillListKey;

    /**
     * 根据关键字查询账单列表
     *
     * @param vo 包含了信息的类，见{@link GetBillListOrderBySpecificMethodVo}
     * @return : java.util.List<com.zeyuli.pojo.bo.GetBillListBo>
     * @author : 李泽聿
     * @since : 2025-11-19 09:02
     */
    @Override
    @CheckUserToken
    public List<GetBillListBo> queryBillList(GetBillListOrderBySpecificMethodVo vo) {
        String type = getSearchType();
        String userId = jwtUtil.getUserInfo(vo.getToken())[0];
        // 缓存key
        // 格式：bill:list:id（前16位）:DATE_RANGE:startDate:endDate
        String key = String.format(baseCacheBillListKey,
                userId.substring(0, 16),
                type,
                vo.getEndDate().replace("-", ""),
                vo.getStartDate().replace("-", ""),
                vo.getPage(),
                vo.getLimit()
        );
        // 1,2. 命中一级,二级缓存
        List<GetBillListBo> billList;
        billList = cacheUtil.getBillListOrderBySpecialMethod(key);
        if (billList != null) {
            return billList;
        }

        // 3. 查数据库
        billList = queryBillMapper.queryBillListOrderByKeyword(vo,userId);
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
        return QueryBillListTypeEnum.KEYWORD.name();
    }
}
