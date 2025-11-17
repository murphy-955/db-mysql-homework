package com.zeyuli.service.impl;


import com.zeyuli.annotations.CheckUserToken;
import com.zeyuli.enm.StatusCodeEnum;
import com.zeyuli.mappers.BillMapper;
import com.zeyuli.pojo.bo.GetBillListBo;
import com.zeyuli.pojo.po.BillPo;
import com.zeyuli.pojo.vo.AddBillVo;
import com.zeyuli.pojo.vo.UserGetBillDetailVo;
import com.zeyuli.pojo.vo.UserQueryBillVo;
import com.zeyuli.service.BillService;
import com.zeyuli.util.CacheUtil;
import com.zeyuli.util.JwtUtil;
import com.zeyuli.util.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 账单接口业务实现
 *
 * @author 李泽聿
 * @since 2025-11-14 14:26
 */
@Service
public class BillServiceImpl implements BillService {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private BillMapper billMapper;

    @Autowired
    private CacheUtil cacheUtil;

    /**
     * 将账单信息添加到数据库中<br>
     * 将热点数据缓存到一级缓存（caffeine）中
     *
     * @param vo {@link AddBillVo}
     * @return : java.util.Map<java.lang.String,java.lang.Object>
     * @author : 李泽聿
     * @since : 2025-11-16 14:29
     */
    @Override
    @CheckUserToken
    public Map<String, Object> addBill(AddBillVo vo) {
        String userId = jwtUtil.getUserInfo(vo.getToken())[0];
        BillPo res = billMapper.addBill(vo, userId);
        if (res != null) {
            // TODO 防止缓存雪崩
            // 我们认为离当前天数7天内的账单是热点数据，需要进行预热
            LocalDate date = vo.getDate();
            if (date.isBefore(LocalDate.now().plusDays(7))) {
                // 缓存预热代码
                cacheUtil.asyncCacheHotBillToLocalCache(res);
            }
            return Response.success(null);
        }
        return Response.error(StatusCodeEnum.INSERT_BILL_FAILED);
    }


    /**
     * 获取账单列表<br>
     * 1. 将第一条数据缓存到<b>一级缓存（caffeine）</b>中<br>
     * 2. 将频繁访问的数据缓存到<b>二级缓存（redis）</b>中<br>
     *
     * @param page  页码
     * @param limit 每页条数
     * @param vo    {@link UserQueryBillVo}
     * @return : java.util.Map<java.lang.String,java.lang.Object>
     * @author : 李泽聿
     * @since : 2025-11-17 08:09
     */
    @Override
    @CheckUserToken
    public Map<String, Object> getBillList(UserQueryBillVo vo, int page, int limit, Long lastId, LocalDate lastDate) {
        List<GetBillListBo> localCache;
        String userId = jwtUtil.getUserInfo(vo.getToken())[0];
        // 1. 尝试从一级缓存中获取第一条数据
        localCache = cacheUtil.getBillListFromLocalCache(userId, limit);
        if (localCache != null && !localCache.isEmpty()) {
            return Response.success(localCache);
        }

        // 2. 从redis中获取账单列表
        localCache = cacheUtil.getBillListFromRedis(userId, page, limit);
        if (localCache != null) {
            return Response.success(localCache);
        }

        // 3. 从数据库中获取账单列表
        List<GetBillListBo> billList = billMapper.getBillList(page, limit, userId, lastId, lastDate);
        // 防止缓存击穿
        if (billList == null) {
            cacheUtil.cacheNullKey(userId, page, limit);
            return Response.error(StatusCodeEnum.GET_DATA_FAILED);
        }
        // 4.异步写入缓存
        if (page == 1 && !billList.isEmpty()) {
            cacheUtil.asyncWriteLocalCache(userId, page, limit, billList);
        }
        cacheUtil.asyncWriteRedisCache(userId, page, limit, billList);
        return Response.success(billList);
    }

    /**
     * 获取账单详情<br>
     * 1. 拿<b>一级缓存（caffeine）</b>，返回<br>
     * 2. 未命中，拿<b>二级缓存（redis）</b>，返回<br>
     * 3. 未命中，从<b>数据库</b>中查询，返回<br>
     * 4. 未命中，返回错误信息，并在<b>二级缓存（redis）</b>中设置空值，详情见 {@link CacheUtil#cacheNullKey(String, int, int)}<br>
     *
     * @param vo {@link UserGetBillDetailVo}
     * @return : java.util.Map<java.lang.String,java.lang.Object>
     * @author : 李泽聿
     * @since : 2025-11-17 09:50
     */
    @Override
    @CheckUserToken
    public Map<String, Object> getBillDetail(UserGetBillDetailVo vo) {
        String userId = jwtUtil.getUserInfo(String.valueOf(vo.getToken()))[0];
        // 1.检查是否为有效
        boolean isNullKey = cacheUtil.checkNullKey(userId, vo.getId());
        if (isNullKey) {
            return Response.error(StatusCodeEnum.GET_DATA_FAILED);
        }
        // 2.尝试命中一级缓存
        BillPo billFromLocalCache = cacheUtil.getBillFromLocalCache(userId, vo.getId());
        if (billFromLocalCache != null) {
            return Response.success(billFromLocalCache);
        }

        // 3.尝试命中二级缓存
        BillPo billFromRedis = cacheUtil.getBillFromRedis(userId, vo.getId());
        if (billFromRedis != null) {
            return Response.success(billFromRedis);
        }

        // 4.从数据库中查询
        BillPo billFromDb = billMapper.getBillDetail(vo.getId(), userId);
        if (billFromDb != null) {
            if (billFromDb.getDate().isBefore(LocalDate.now().plusDays(7))) {
                // 5. 异步写入本地缓存和redis缓存，并设置随机过期时间
                cacheUtil.asyncCacheHotBillToLocalCache(billFromDb);
                cacheUtil.asyncCacheHotBillToRedis(billFromDb);
            }
            // 6.异步写入redis缓存
            cacheUtil.asyncCacheBillToRedis(billFromDb);
            return Response.success(billFromDb);
        }
        return Response.error(StatusCodeEnum.GET_DATA_FAILED);
    }


}
