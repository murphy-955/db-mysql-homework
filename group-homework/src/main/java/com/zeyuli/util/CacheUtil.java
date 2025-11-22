package com.zeyuli.util;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.zeyuli.annotations.LogOutPut;
import com.zeyuli.enm.RecordEnum;
import com.zeyuli.expection.CachePenetrationExpectation;
import com.zeyuli.pojo.bo.GetBillListBo;
import com.zeyuli.pojo.po.BillPo;
import com.zeyuli.pojo.vo.AddBillVo;
import com.zeyuli.pojo.vo.InitAccountInfoVo;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import com.zeyuli.config.LocalCacheConfig;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 缓存工具类
 *
 * @author 李泽聿
 * @since 2025-11-15 15:27
 */
@Component
public class CacheUtil {
    @Value("${cache.redis.expireTime}")
    private long basicExpireTime;

    @Value("${cache.baseKey.billList}")
    private String cacheBillListKey;

    @Value("${cache.redis.nullCacheTime}")
    private long nullCacheTime;

    @Value("${cache.redis.nullData}")
    private String nullData;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private Cache<String, BillPo> localBillCache;

    @Autowired
    private Cache<String, List<GetBillListBo>> localBillListCache;

    /**
     *
     *
     * @return : long ==>  返回4倍的{@link #basicExpireTime}，加上0到{@link #basicExpireTime}的偏移量，为热点缓存的过期时间
     * @author : 李泽聿
     * @since : 2025-11-15 15:30
     */
    @LogOutPut
    private long hotDataRandomTTL() {
        long randomOffset = (long) (Math.random() * basicExpireTime);
        return basicExpireTime * 4 + randomOffset;
    }

    /**
     * 异步将热点数据写入redis缓存
     *
     * @param po : 账单对象
     * @author : 李泽聿
     * @since : 2025-11-17 14:36
     */
    @Async
    @LogOutPut
    public void asyncCacheHotBillToRedis(BillPo po) {
        String key = "bill:" + po.getId() + ":user:" + po.getUserId().substring(0, 16);
        redisTemplate.opsForValue().set(key, po, hotDataRandomTTL(), TimeUnit.MINUTES);
    }

    /**
     * 异步将热点数据写入本地缓存<br>
     * 本地缓存配置见{@link LocalCacheConfig#billLocalCache()}
     *
     * @param po : 账单对象
     * @author : 李泽聿
     * @since : 2025-11-17 14:38
     */
    @Async
    @LogOutPut
    public void asyncCacheHotBillToLocalCache(BillPo po) {
        String key = "bill:" + po.getId() + ":user:" + po.getUserId().substring(0, 16);
        localBillCache.put(key, po);
    }

    public BillPo getBillFromLocalCache(String userId, long id) {
        String key = "bill:" + id + ":user:" + userId.substring(0, 16);
        return localBillCache.getIfPresent(key);
    }

    /**
     * 异步写入redis缓存
     *
     * @param po : 账单对象
     * @author : 李泽聿
     * @since : 2025-11-17 14:22
     */
    @Async
    @LogOutPut
    public void asyncCacheBillToRedis(BillPo po) {
        String key = "bill:" + po.getId() + ":user:" + po.getUserId().substring(0, 16);
        redisTemplate.opsForValue().set(key, po, basicExpireTime, TimeUnit.MINUTES);
    }

    /**
     * 本地缓存只缓存第一页数据
     *
     * @param userId : 用户id
     * @param limit  : 限制条数
     * @return : java.util.List<com.zeyuli.pojo.bo.GetBillListBo>
     * @author : 李泽聿
     * @since : 2025-11-17 08:43
     */
    @SuppressWarnings("unchecked")
    @LogOutPut
    public List<GetBillListBo> getBillListFromLocalCache(String userId,
                                                         int limit) {
        // 只需要缓存第一页的数据，其他页的数据可以直接从redis或db中获取
        String key = cacheBillListKey + ":" + userId.substring(0, 16) + ":page:1" + ":limit:" + limit;
        return (List<GetBillListBo>) localBillCache.getIfPresent(key);
    }

    /**
     * redis中缓存可能的多页数据
     *
     * @param userId : 用户id
     * @param page   : 页码
     * @param limit  : 限制条数
     * @return : java.util.List<com.zeyuli.pojo.bo.GetBillListBo>
     * @author : 李泽聿
     * @since : 2025-11-17 08:44
     */
    @SuppressWarnings("unchecked")
    @LogOutPut
    public List<GetBillListBo> getBillListFromRedis(String userId, int page, int limit) {
        String key = cacheBillListKey + ":" + userId.substring(0, 16) + ":page:" + page + ":limit:" + limit;
        List<GetBillListBo> billList = (List<GetBillListBo>) redisTemplate.opsForValue().get(key);
        if (billList != null && !billList.isEmpty()) {
            return billList;
        }
        return null;
    }

    /**
     * 从<b>二级缓存（redis）</b>中获取账单
     *
     * @param userId : 用户id
     * @param id     : 账单id
     * @return : com.zeyuli.pojo.po.BillPo
     * @author : 李泽聿
     * @since : 2025-11-17 14:08
     */
    @LogOutPut
    public BillPo getBillFromRedis(String userId, @NotNull(message = "账单ID不能为空") Long id) {
        String key = "bill:" + id + ":user:" + userId.substring(0, 16);
        return (BillPo) redisTemplate.opsForValue().get(key);
    }


    /**
     * 异步将账单列表写入本地缓存<br>
     * 注意：本地缓存的过期时间为 5分钟,详见{@link LocalCacheConfig#billListCache()}
     *
     * @param userId   : 用户id
     * @param page     : 页码
     * @param limit    : 限制条数
     * @param billList : 账单列表
     * @author : 李泽聿
     * @since : 2025-11-17 09:40
     */
    @Async
    @LogOutPut
    public void asyncWriteLocalCache(String userId, int page, int limit, List<GetBillListBo> billList) {
        String key = cacheBillListKey + ":" + userId.substring(0, 16) + ":page:" + page + ":limit:" + limit;
        localBillListCache.put(key, billList);
    }

    /**
     * 异步将账单列表写入redis缓存<br>
     * 注意：redis缓存的过期时间为 30分钟{@link #basicExpireTime}
     *
     * @param userId   : 用户id
     * @param page     : 页码
     * @param limit    : 限制条数
     * @param billList : 账单列表
     * @author : 李泽聿
     * @since : 2025-11-17 09:41
     */
    @Async
    @LogOutPut
    public void asyncWriteRedisCache(String userId, int page, int limit, List<GetBillListBo> billList) {
        String key = cacheBillListKey + ":" + userId.substring(0, 16) + ":page:" + page + ":limit:" + limit;
        redisTemplate.opsForValue().set(key, billList, basicExpireTime, TimeUnit.MINUTES);
    }

    /**
     * <b>缓存空值</b>，设置ttl为15秒，防止缓存穿透
     *
     * @param userId : 用户id
     * @param page   : 页码
     * @param limit  : 限制条数
     * @author : 李泽聿
     * @since : 2025-11-17 10:25
     */
    @LogOutPut
    public void cacheNullKey(String userId, int page, int limit) {
        String key = cacheBillListKey + ":" + userId.substring(0, 16) + ":page:" + page + ":limit:" + limit;
        redisTemplate.opsForValue().set(key, nullData, nullCacheTime, TimeUnit.SECONDS);
    }

    /**
     * 缓存空值，设置ttl为15秒，防止缓存穿透
     *
     * @param userId : 用户id
     * @param id     : 账单id
     * @author : 李泽聿
     * @since : 2025-11-17 13:58
     */
    @LogOutPut
    public void cacheNullKey(String userId, long id) {
        String key = "bill:" + id + ":user:" + userId.substring(0, 16);
        redisTemplate.opsForValue().set(key, nullData, nullCacheTime, TimeUnit.SECONDS);
    }

    /**
     * 检查账单列表空值，防止缓存穿透<br>
     * ttl 15秒
     *
     * @param userId : 用户id
     * @param page   : 页码
     * @param limit  : 限制条数
     * @author : 李泽聿
     * @since : 2025-11-17 10:35
     */
    @LogOutPut
    public void checkNullKey(String userId, int page, int limit) {
        String key = cacheBillListKey + ":" + userId.substring(0, 16) + ":page:" + page + ":limit:" + limit;
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null || value.equals(nullData)) {
            throw new CachePenetrationExpectation("值为空");
        }
    }

    /**
     * 重载方法 检查账单列表空值，防止缓存穿透
     *
     * @param userId : 用户id
     * @param id     : 账单id
     * @return : void
     * @author : 李泽聿
     * @since : 2025-11-17 13:56
     */
    @LogOutPut
    public boolean checkNullKey(String userId, long id) {
        String key = "bill:" + id + ":user:" + userId.substring(0, 16);
        Object value = redisTemplate.opsForValue().get(key);
        if (value != null && value.equals(nullData)) {
            // 缓存空值
            cacheNullKey(userId, id);
            return true;
        }
        return false;
    }

    /**
     * 清除所有缓存，包括列表和详情<br>
     * 包括：<b>一级缓存（caffeine）</b>、<b>二级缓存（redis）</b>、以及<b>可能缓存的空值</b>
     *
     * @param userId : 用户id
     * @param id     : 账单id
     * @param page   : 页码
     * @param limit  : 限制条数
     * @author : 李泽聿
     * @since : 2025-11-17 15:35
     */
    @LogOutPut
    public void clearAllCache(String userId,
                              @NotNull(message = "账单ID不能为空") Long id,
                              int page,
                              int limit) {
        // 删本地账单详情缓存
        String billKey = "bill:" + id + ":user:" + userId.substring(0, 16);
        localBillCache.invalidate(billKey);
        // 删redis详情缓存包括了可能存在的空值
        redisTemplate.delete(billKey);

        // 删本地账单列表缓存
        String listKey = cacheBillListKey + ":" + userId.substring(0, 16) + ":page:" + page + ":limit:" + limit;
        localBillListCache.invalidate(listKey);
        // 删redis列表缓存
        redisTemplate.delete(listKey);
    }

    /**
     * 获取删除账单列表<br>
     *
     * @param userId : 用户id
     * @param page   : 页码
     * @param limit  : 限制条数
     * @return : java.util.List<com.zeyuli.pojo.bo.GetBillListBo>
     * @author : 李泽聿
     * @since : 2025-11-17 19:31
     */
    @LogOutPut
    public List<GetBillListBo> getDeleteBillListFromRedis(String userId, int page, int limit) throws JsonProcessingException {
        String key = cacheBillListKey + ":" + userId.substring(0, 16) + ":page:" + page + ":limit:" + limit;
        String s = (String) redisTemplate.opsForValue().get(key);
        if (s != null) {
            if (s.equals(nullData)) {
                throw new CachePenetrationExpectation("值为空");
            }
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(s, new TypeReference<>() {
            });
        }
        return null;
    }

    /**
     * 从redis缓存中查询依据日期的账单列表
     *
     * @param key : 缓存键
     * @return : java.util.List<com.zeyuli.pojo.bo.GetBillListBo>
     * @author : 李泽聿
     * @since : 2025-11-18 08:55
     */
    @SuppressWarnings("unchecked")
    @LogOutPut
    public List<GetBillListBo> queryBillListOrderByDateFromRedisCache(String key) {
        return (List<GetBillListBo>) redisTemplate.opsForValue().get(key);
    }

    /**
     * 异步缓存依据日期的账单列表<br>
     * 缓存热数据，过期时间为{@code  hotDataRandomTTL()}分钟<br>
     * 缓存冷数据，过期时间为{@code basicExpireTime}分钟
     *
     * @param key      : 缓存键
     * @param billList : 账单列表
     * @author : 李泽聿
     * @since : 2025-11-18 11:47
     */
    @Async
    @LogOutPut
    public void asyncCacheBillListOrderBySpecificMethod(String key, List<GetBillListBo> billList) {
        // 缓存热数据，过期时间为7天
        if (billList.getLast().getDate().isBefore(LocalDate.now().plusDays(7))) {
            localBillListCache.put(key, billList);
            redisTemplate.opsForValue().set(key, billList, hotDataRandomTTL(), TimeUnit.MINUTES);
        }
        // 缓存冷数据，过期时间为
        redisTemplate.opsForValue().set(key, billList, basicExpireTime, TimeUnit.MINUTES);
    }

    /**
     * 异步缓存空值，过期时间为{@code nullCacheTime}秒
     *
     * @param key : 缓存键
     * @author : 李泽聿
     * @since : 2025-11-18 11:46
     */
    @Async
    @LogOutPut
    public void asyncCacheNullKey(String key) {
        redisTemplate.opsForValue().set(key, nullData, nullCacheTime, TimeUnit.SECONDS);
    }

    /**
     * 从缓存中查询依据账号的账单列表
     *
     * @param key : 缓存键
     * @return : java.util.List<com.zeyuli.pojo.bo.GetBillListBo>
     * @author : 李泽聿
     * @since : 2025-11-18 14:26
     */
    @LogOutPut
    public List<GetBillListBo> getBillListOrderBySpecialMethod(String key) {
        // 1. 命中一级缓存
        List<GetBillListBo> billList = localBillListCache.getIfPresent(key);
        if (billList != null) {
            return billList;
        }
        // 2. 命中二级缓存
        billList = queryBillListOrderByDateFromRedisCache(key);
        return billList;
    }

    /**
     * 处理添加账单时的缓存操作，包括：
     * 1. 将账单加入Redis待刷盘队列
     * 2. 更新Redis中的账单缓存
     * 3. 根据日期判断是否需要预热热点数据到本地缓存
     * 4. 异步执行批量刷盘检查
     *
     * @param billPo 账单对象
     * @author : 李泽聿
     * @since : 2025-11-18
     */
    @LogOutPut
    public void handleBillCacheOperations(BillPo billPo) {
        // 1. 将账单加入Redis待刷盘队列
        String billId = String.valueOf(billPo.getId());
        redisTemplate.opsForList().rightPush("bill:batch:flush", billId);

        // 2. 更新Redis中的账单缓存
        asyncCacheBillToRedis(billPo);

        // 3. 根据日期判断是否需要预热热点数据到本地缓存
        if (billPo.getDate().isBefore(LocalDate.now().plusDays(7))) {
            asyncCacheHotBillToLocalCache(billPo);
        }

        // 4. 异步执行批量刷盘检查
        asyncPerformBatchFlushCheck();
    }

    /**
     * 异步执行批量刷盘检查
     * 检查待刷盘队列的大小，如果超过阈值或者距离上次刷盘时间超过阈值，则执行批量刷盘
     *
     * @author : 李泽聿
     * @since : 2025-11-18
     */
    @Async
    @LogOutPut
    public void asyncPerformBatchFlushCheck() {
        // 实现批量刷盘检查逻辑
        // 这里是简化实现，实际项目中可能需要更复杂的逻辑
        String queueKey = "bill:batch:flush";
        String lastFlushTimeKey = "bill:batch:last_flush_time";

        // 检查队列大小
        Long queueSize = redisTemplate.opsForList().size(queueKey);
        if (queueSize != null && queueSize >= 10) { // 队列大小阈值设为10
            performBatchFlush();
            return;
        }

        // 检查距离上次刷盘时间
        Object lastFlushTimeObj = redisTemplate.opsForValue().get(lastFlushTimeKey);
        if (lastFlushTimeObj != null) {
            long lastFlushTime = Long.parseLong(lastFlushTimeObj.toString());
            long currentTime = System.currentTimeMillis();
            // 如果距离上次刷盘时间超过5分钟，则执行批量刷盘
            if (currentTime - lastFlushTime >= 5 * 60 * 1000) {
                performBatchFlush();
            }
        } else {
            // 第一次执行，设置初始时间
            redisTemplate.opsForValue().set(lastFlushTimeKey, String.valueOf(System.currentTimeMillis()));
        }
    }

    /**
     * 执行批量刷盘操作
     * 从队列中取出所有待刷盘的账单ID，并执行相应的数据库更新操作
     *
     * @author : 李泽聿
     * @since : 2025-11-18
     */
    @LogOutPut
    private void performBatchFlush() {
        // 这里是简化实现，实际项目中需要根据具体的业务逻辑进行批量刷盘操作
        String queueKey = "bill:batch:flush";
        String lastFlushTimeKey = "bill:batch:last_flush_time";

        // 更新最后刷盘时间
        redisTemplate.opsForValue().set(lastFlushTimeKey, String.valueOf(System.currentTimeMillis()));

        // 清空队列（实际项目中应该先取出队列中的所有元素，然后执行刷盘操作，最后再清空队列）
        redisTemplate.delete(queueKey);
    }

    /**
     * 检查缓存中的余额是否足够
     *
     * @param key : 缓存键{@code user:userId前17位:account:account}
     * @return : boolean 是否有足够余额
     * @author : 李泽聿
     * @since : 2025-11-22 14:00
     */
    public boolean checkBalance(String key, Double amount) {
        InitAccountInfoVo res = (InitAccountInfoVo) redisTemplate.opsForValue().get(key);
        if (res != null) {
            return res.getBalance() >= amount;
        }
        return false;
    }

    /**
     *
     * @param key : 缓存键{@code user:userId前17位:account}
     * @param vo  : 添加账单VO对象
     * @author : 李泽聿
     * @since : 2025-11-22 14:16
     */
    public void updateBalance(String key, AddBillVo vo) {
        InitAccountInfoVo res = (InitAccountInfoVo) redisTemplate.opsForValue().get(key);
        if (vo.getRecordEnum().equals(RecordEnum.EXPENDITURE)) {
            // 支出，余额减少
            if (res != null) {
                double newBalance = res.getBalance() - vo.getAmount();
                InitAccountInfoVo initAccountInfoVo = new InitAccountInfoVo();
                initAccountInfoVo.setToken(res.getToken());
                initAccountInfoVo.setAccount(res.getAccount());
                initAccountInfoVo.setBalance(newBalance);
                initAccountInfoVo.setDescription(res.getDescription());
                redisTemplate.opsForValue().set(key, initAccountInfoVo);
            }
        }
        if (vo.getRecordEnum().equals(RecordEnum.INCOME)) {
            // 收入，余额增加
            if (res != null) {
                double newBalance = res.getBalance() + vo.getAmount();
                InitAccountInfoVo initAccountInfoVo = new InitAccountInfoVo();
                initAccountInfoVo.setToken(res.getToken());
                initAccountInfoVo.setAccount(res.getAccount());
                initAccountInfoVo.setBalance(newBalance);
                initAccountInfoVo.setDescription(res.getDescription());
                redisTemplate.opsForValue().set(key, initAccountInfoVo);
            }
        }
    }
}