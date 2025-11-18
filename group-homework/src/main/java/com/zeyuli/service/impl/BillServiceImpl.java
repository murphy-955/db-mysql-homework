package com.zeyuli.service.impl;


import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
     * 删除账单<br>
     * 是假删除，只是将状态改为删除，并不删除数据库中的数据<br>
     * 由于业务条件，插入和读取较多，而修改和删除操作较少，为保障强一致性，所以采用延时双删，延时100ms
     *
     * @param vo {@link UserOperateBillDetailVo}
     * @return : java.util.Map<java.lang.String,java.lang.Object>
     * @author : 李泽聿
     * @since : 2025-11-17 15:03
     */
    @Override
    @CheckUserToken
    public Map<String, Object> deleteBill(UserOperateBillDetailVo vo, int page, int limit) throws InterruptedException {
        String userId = jwtUtil.getUserInfo(String.valueOf(vo.getToken()))[0];
        cacheUtil.clearAllCache(userId, vo.getId(), page, limit);
        int res = billMapper.deleteBill(vo.getId(), userId);
        if (res > 0) {
            Thread.sleep(100);
            cacheUtil.clearAllCache(userId, vo.getId(), page, limit);
            return Response.success(null);
        }
        cacheUtil.clearAllCache(userId, vo.getId(), page, limit);
        return Response.error(StatusCodeEnum.DELETE_BILL_FAILED);
    }

    /**
     * 获取删除账单列表<br>
     * 只需要放二级缓存中即可，不用放一级缓存<br>
     *
     * @param vo    {@link UserQueryBillVo}
     * @param page  页码
     * @param limit 每页条数
     * @return : java.util.Map<java.lang.String,java.lang.Object>
     * @author : 李泽聿
     * @since : 2025-11-17 19:29
     */
    @Override
    @CheckUserToken
    public Map<String, Object> getDeleteBillList(UserQueryBillVo vo, int page, int limit) throws JsonProcessingException {
        // 1. 从redis中获取账单列表
        String userId = jwtUtil.getUserInfo(vo.getToken())[0];
        List<GetBillListBo> redisBillList = cacheUtil.getDeleteBillListFromRedis(userId, page, limit);
        if (redisBillList != null) {
            return Response.success(StatusCodeEnum.SUCCESS, redisBillList);
        }
        // 2. 从数据库中获取账单列表
        List<GetBillListBo> billList = billMapper.getDeleteBillList(page, limit, userId);
        if (billList == null) {
            cacheUtil.cacheNullKey(userId, page, limit);
            return Response.error(StatusCodeEnum.GET_DATA_FAILED);
        }
        // 3.异步写入缓存
        if (page == 1 && !billList.isEmpty()) {
            cacheUtil.asyncWriteRedisCache(userId, page, limit, billList);
        }
        cacheUtil.asyncWriteRedisCache(userId, page, limit, billList);
        return Response.success(StatusCodeEnum.SUCCESS, billList);
    }

    /**
     * 恢复账单<br>
     * 先从数据库中查询，如果查询不到，缓存空值。然后更新状态为正常，并更新缓存<br>
     *
     * @param vo {@link UserOperateBillDetailVo}
     * @return : java.util.Map<java.lang.String,java.lang.Object>
     * @author : 李泽聿
     * @since : 2025-11-17 20:00
     */
    @Override
    @CheckUserToken
    public Map<String, Object> recoverBill(UserOperateBillDetailVo vo) {
        String userId = jwtUtil.getUserInfo(String.valueOf(vo.getToken()))[0];
        BillPo billPo = billMapper.recoverBill(vo.getId(), userId);
        if (billPo != null) {
            if (billPo.getDate().isBefore(LocalDate.now().plusDays(7))) {
                // 异步写入本地缓存和redis缓存，并设置随机过期时间
                cacheUtil.asyncCacheHotBillToLocalCache(billPo);
            }
            // 异步写入redis缓存
            cacheUtil.asyncCacheBillToRedis(billPo);
            return Response.success(StatusCodeEnum.SUCCESS, null);
        }
        // 未找到账单,缓存空值
        cacheUtil.checkNullKey(userId, vo.getId());
        return Response.error(StatusCodeEnum.GET_DATA_FAILED);
    }

    /**
     * 修改账单<br>
     * 还是使用延时双删
     *
     * @param vo {@link ModifyBillVo}
     * @return : java.util.Map<java.lang.String,java.lang.Object>
     * @author : 李泽聿
     * @since : 2025-11-17 20:16
     */
    @Override
    @CheckUserToken
    public Map<String, Object> modifyBill(ModifyBillVo vo, int page, int limit) throws InterruptedException {
        String userId = jwtUtil.getUserInfo(vo.getToken())[0];
        // 1.先删缓存
        cacheUtil.clearAllCache(userId, vo.getId(), page, limit);

        // 2. 更新数据库
        BillPo billPo = billMapper.modifyBill(vo, userId);

        // 3. 分别为账单id和分页缓存空值，防止缓存穿透
        if (billPo == null) {
            cacheUtil.cacheNullKey(userId,vo.getId());
            cacheUtil.cacheNullKey(userId, page, limit);
            return Response.error(StatusCodeEnum.GET_DATA_FAILED);
        }

        // 4. 延时双删
        Thread.sleep(100);
        cacheUtil.clearAllCache(userId, vo.getId(), page, limit);

        // 5. 异步写入缓存
        if (billPo.getDate().isBefore(LocalDate.now().plusDays(7))) {
            // 异步写入本地缓存和redis缓存，并设置随机过期时间
            cacheUtil.asyncCacheHotBillToLocalCache(billPo);
        }

        // 6.异步写入redis缓存
        cacheUtil.asyncCacheBillToRedis(billPo);
        return Response.success(StatusCodeEnum.SUCCESS, null);
    }

    /**
     * 添加账单列表
     *
     * @author : 李泽聿
     * @since : 2025-11-17 20:56
     * @param vo {@link AddBillListVo}
     * @return : java.util.Map<java.lang.String,java.lang.Object>
     */
    @Override
    @CheckUserToken
    public Map<String, Object> addBillList(AddBillListVo vo) {
        String userId = jwtUtil.getUserInfo(vo.getToken())[0];
        List<BillPo> res = billMapper.addBillList(vo, userId);
        if (res == null) {
            return Response.error(StatusCodeEnum.GET_DATA_FAILED);
        }
        for (BillPo i : res) {
            if (i.getDate().isBefore(LocalDate.now().plusDays(7))) {
                // 异步写入本地缓存和redis缓存，并设置随机过期时间
                cacheUtil.asyncCacheHotBillToLocalCache(i);
            }
                // 异步写入redis缓存
                cacheUtil.asyncCacheBillToRedis(i);
        }
        return Response.success(StatusCodeEnum.SUCCESS, null);
    }

    @Override
    public Map<String, Object> getRecordType() {
        Map<String,Object> res = new HashMap<>();
        for (RecordEnum recordEnum : RecordEnum.values()) {
            res.put(recordEnum.name(), recordEnum.getRecordDescription());
        }
        return Response.success(StatusCodeEnum.SUCCESS, res);
    }
}