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
 * 根据<b>日期范围</b>查询账单信息的实现类
 *
 * @author 李泽聿
 * @since 2025-11-18 08:55
 */
@Component
public class QueryBillByDateStrategyImpl implements BillQueryStrategy {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CacheUtil cacheUtil;

    @Autowired
    private QueryBillMapper queryBillMapper;


    @Value("${cache.baseKey.billList}")
    private String baseCacheBillListKey;

    /**
     * 先读缓存，缓存中没有再查询数据库，如果数据库中也没有数据，则返回空列表，并缓存空值
     *
     * @param vo 包含了查询的<b>日期范围</b>，<b>token</b>等，见 {@link GetBillListOrderBySpecificMethodVo}
     * @return : java.util.List<com.zeyuli.pojo.bo.GetBillListBo>
     * @author : 李泽聿
     * @since : 2025-11-18 10:48
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
        if (!billList.isEmpty()) {
            return billList;
        }

        // 3. 查数据库
        billList = queryBillMapper.queryBillListOrderByDate(vo, userId);
        if (!billList.isEmpty()) {
            cacheUtil.asyncCacheBillListOrderBySpecificMethod(key, billList);
            return billList;
        }
        // 4. 缓存空值
        cacheUtil.asyncCacheNullKey(key);
        return null;
    }

    /**
     * 获取查询策略的类型<br>
     * 该策略实现类用于根据<b>日期范围</b>查询账单信息
     *
     * @return : java.lang.String
     * @author : 李泽聿
     * @since : 2025-11-18 10:45
     */
    @Override
    public String getSearchType() {
        return QueryBillListTypeEnum.DATE_RANGE.name();
    }
}
