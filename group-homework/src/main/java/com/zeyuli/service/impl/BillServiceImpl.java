package com.zeyuli.service.impl;


import com.zeyuli.annotations.CheckUserToken;
import com.zeyuli.enm.RecordEnum;
import com.zeyuli.enm.StatusCodeEnum;
import com.zeyuli.mappers.BillMapper;
import com.zeyuli.pojo.bo.GetBillListBo;
import com.zeyuli.pojo.po.BillPo;
import com.zeyuli.pojo.vo.*;
import com.zeyuli.service.BillService;
import com.zeyuli.util.CacheUtil;
import com.zeyuli.util.JwtUtil;
import com.zeyuli.util.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
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
     * 添加账单主方法<br>
     * 按照UML流程图实现的完整添加账单流程：<br>
     * 1. 参数校验和token验证（通过@CheckUserToken注解）<br>
     * 2. 检查Redis中用户余额是否足够（仅针对支出类型）<br>
     * 3. 在Redis中预扣减余额<br>
     * 4. 生成账单ID并添加到数据库<br>
     * 5. 将账单加入Redis待刷盘队列<br>
     * 6. 更新Redis中的账单缓存<br>
     * 7. 异步预热热点数据到本地缓存<br>
     * 8. 异步执行批量刷盘检查<br>
     * 9. 立即返回操作成功
     *
     * @param vo {@link AddBillVo} 账单信息对象
     * @return 操作结果Map
     * @author : 李泽聿
     * @since : 2025-11-16 14:29
     */
    @Override
    @CheckUserToken
    @Transactional(rollbackFor = Exception.class,
            propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ)
    public Map<String, Object> addBill(AddBillVo vo) {
        String userId = jwtUtil.getUserInfo(vo.getToken())[0];
        String key = "user:" + userId.substring(0, 16) + "account:" + vo.getAccount();
        // 异步执行批量刷盘检查
        asynchronousBrushing(vo, userId);
        // 更新Redis中的余额
        cacheUtil.updateBalance(key, vo);
        return Response.success(new ArrayList<>());
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
            System.out.println("命中一级缓存");
            return Response.success(localCache);
        }

        // 2. 从redis中获取账单列表
        localCache = cacheUtil.getBillListFromRedis(userId, page, limit);
        if (localCache != null) {
            System.out.println("命中二级缓存");
            return Response.success(localCache);
        }

        // 3. 从数据库中获取账单列表
        Long offset = (long) (page - 1) * limit;
        List<GetBillListBo> billList = billMapper.getBillList(limit, offset, userId, lastId, lastDate);
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
        System.out.println("未命中缓存，从数据库中查询");
        return Response.success(billList);
    }

    /**
     * 获取账单详情<br>
     * 1. 拿<b>一级缓存（caffeine）</b>，返回<br>
     * 2. 未命中，拿<b>二级缓存（redis）</b>，返回<br>
     * 3. 未命中，从<b>数据库</b>中查询，返回<br>
     * 4. 未命中，返回错误信息，并在<b>二级缓存（redis）</b>中设置空值，详情见 {@link CacheUtil#cacheNullKey(String, int, int)}<br>
     *
     * @param vo {@link UserOperateBillDetailVo}
     * @return : java.util.Map<java.lang.String,java.lang.Object>
     * @author : 李泽聿
     * @since : 2025-11-17 09:50
     */
    @Override
    @CheckUserToken
    public Map<String, Object> getBillDetail(UserOperateBillDetailVo vo) {
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

    /**
     * 添加账单列表
     *
     * @param vo {@link AddBillListVo}
     * @return : java.util.Map<java.lang.String,java.lang.Object>
     * @author : 李泽聿
     * @since : 2025-11-17 20:56
     */
    @Override
    @CheckUserToken
    public Map<String, Object> addBillList(AddBillListVo vo) {
        String userId = jwtUtil.getUserInfo(vo.getToken())[0];
        int res = billMapper.addBillList(vo, userId);
        if (res == 0) {
            return Response.error(StatusCodeEnum.GET_DATA_FAILED);
        }
        return Response.success(StatusCodeEnum.SUCCESS, null);
    }

    @Override
    public Map<String, Object> getRecordType() {
        Map<String, Object> res = new HashMap<>();
        for (RecordEnum recordEnum : RecordEnum.values()) {
            res.put(recordEnum.name(), recordEnum.getRecordDescription());
        }
        return Response.success(StatusCodeEnum.SUCCESS, res);
    }

    /**
     * 异步刷写账单<br>
     * 当用户进行支出操作时，需要检查用户余额是否足够。如果足够，就异步刷写账单到数据库。<br>
     * 异步刷写账单的目的是为了提高系统的吞吐量，避免阻塞用户操作。<br>
     *
     * @param vo     {@link AddBillVo}
     * @param userId {@link String}
     * @author : 李泽聿
     * @since : 2025-11-22 14:12
     *
     */
    // todo synchronized 会导致addBill方法的事务失效
    @Async
    protected synchronized void asynchronousBrushing(AddBillVo vo, String userId) {
        Long id = billMapper.addBill(vo, userId);
        BillPo res = billMapper.getBillDetail(id, userId);
        int insertRes = 0;
        if (vo.getRecordEnum() == RecordEnum.INCOME) {
            insertRes = billMapper.addAccountBalance(userId, vo.getAmount());
        }
        if (vo.getRecordEnum() == RecordEnum.EXPENDITURE) {
            insertRes = billMapper.modifyAccountBalance(userId, vo.getAmount());
        }
        if (insertRes == 0) {
            throw new RuntimeException("更新用户余额失败，用户ID：" + userId + "，账单ID：" + id);
        }
        if (res != null) {
            // 我们认为离当前天数7天内的账单是热点数据，需要进行预热
            LocalDate date = vo.getDate();
            if (date.isBefore(LocalDate.now().plusDays(7))) {
                // 缓存预热代码
                cacheUtil.asyncCacheHotBillToLocalCache(res);
            }
        }
    }
}