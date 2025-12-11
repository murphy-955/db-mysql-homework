package com.zeyuli.strategy.impl;


import com.zeyuli.annotations.CheckUserToken;
import com.zeyuli.enm.QueryBillListTypeEnum;
import com.zeyuli.mappers.QueryBillMapper;
import com.zeyuli.pojo.bo.GetBillListBo;
import com.zeyuli.pojo.vo.GetBillListOrderBySpecificMethodVo;
import com.zeyuli.strategy.BillQueryStrategy;
import com.zeyuli.util.CacheUtil;
import com.zeyuli.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

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

    private static final Logger log = LoggerFactory.getLogger(QueryBillByDateStrategyImpl.class);

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
        long start = System.nanoTime();
        String type = getSearchType();
        String userId = jwtUtil.getUserInfo(vo.getToken())[0];
        // 缓存key
        // 格式：bill:list:id（前16位）:DATE_RANGE:startDate:endDate
        String key = String.format("%s:%s:%s:%s:%s:%d:%d",
                baseCacheBillListKey,
                userId.substring(0, 16),
                type,
                vo.getEndDate().replace("-", ""),
                vo.getStartDate().replace("-", ""),
                vo.getPage(),
                vo.getLimit());// 1,2. 命中一级,二级缓存
        List<GetBillListBo> billList;
        billList = cacheUtil.getBillListOrderBySpecialMethod(key);
        if (!billList.isEmpty()) {
            long cacheTime = System.nanoTime() - start;
            log.info("命中缓存，耗时：{}ms", TimeUnit.NANOSECONDS.toMillis(cacheTime));
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
        System.out.println("未命中缓存，从数据库中查询");
        long dbTime = System.nanoTime() - start;
        log.info("未命中缓存，从数据库中查询，耗时：{}ms", TimeUnit.NANOSECONDS.toMillis(dbTime));
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
