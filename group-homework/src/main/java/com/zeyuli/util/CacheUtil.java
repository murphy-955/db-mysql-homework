package com.zeyuli.util;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.zeyuli.expection.CachePenetrationExpectation;
import com.zeyuli.pojo.bo.GetBillListBo;
import com.zeyuli.pojo.po.BillPo;
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
    public List<GetBillListBo> queryBillListOrderByDateFromRedisCache(String key) {
        return (List<GetBillListBo>) redisTemplate.opsForValue().get(key);
    }

    /**
     * 异步缓存依据日期的账单列表<br>
     * 缓存热数据，过期时间为{@code  hotDataRandomTTL()}分钟<br>
     * 缓存冷数据，过期时间为{@code basicExpireTime}分钟
     *
     * @author : 李泽聿
     * @since : 2025-11-18 11:47
     * @param key  : 缓存键
     * @param billList  : 账单列表
     */
    @Async
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
     * @author : 李泽聿
     * @since : 2025-11-18 11:46
     * @param key   : 缓存键
     */
    @Async
    public void asyncCacheNullKey(String key) {
        redisTemplate.opsForValue().set(key, nullData, nullCacheTime, TimeUnit.SECONDS);
    }

    /**
     * 从缓存中查询依据账号的账单列表
     *
     * @author : 李泽聿
     * @since : 2025-11-18 14:26
     * @param key  : 缓存键
     * @return : java.util.List<com.zeyuli.pojo.bo.GetBillListBo>
     */
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
}