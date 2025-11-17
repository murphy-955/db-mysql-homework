package com.zeyuli.util;


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
     * @author : 李泽聿
     * @since : 2025-11-17 14:36
     * @param po : 账单对象
     */
    @Async
    public void asyncCacheHotBillToRedis(BillPo po) {
        String key = "bill:" + po.getId() + ":user:" + po.getUserId();
        redisTemplate.opsForValue().set(key, po, hotDataRandomTTL(), TimeUnit.MINUTES);
    }

    /**
     * 异步将热点数据写入本地缓存<br>
     * 本地缓存配置见{@link LocalCacheConfig#billLocalCache()}
     *
     * @author : 李泽聿
     * @since : 2025-11-17 14:38
     * @param po : 账单对象
     */
    @Async
    public void asyncCacheHotBillToLocalCache(BillPo po) {
        String key = "bill:" + po.getId() + ":user:" + po.getUserId();
        localBillCache.put(key, po);
    }

    public BillPo getBillFromLocalCache(String userId, long id) {
        String key = "bill:" + id + ":user:" + userId;
        return localBillCache.getIfPresent(key);
    }

    /**
     * 异步写入redis缓存
     *
     * @author : 李泽聿
     * @since : 2025-11-17 14:22
     * @param po
     * @return : void
     */
    @Async
    public void asyncCacheBillToRedis(BillPo po) {
        String key = "bill:" + po.getId() + ":user:" + po.getUserId();
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
    public List<GetBillListBo> getBillListFromLocalCache(String userId,
                                                         int limit) {
        // 只需要缓存第一页的数据，其他页的数据可以直接从redis或db中获取
        String key = cacheBillListKey + ":" + userId + ":page:1" + ":limit:" + limit;
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
    public List<GetBillListBo> getBillListFromRedis(String userId, int page, int limit) {
        String key = cacheBillListKey + ":" + userId + ":page:" + page + ":limit:" + limit;
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
        String key = "bill:" + id + ":user:" + userId;
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
        String key = cacheBillListKey + ":" + userId + ":page:" + page + ":limit:" + limit;
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
        String key = cacheBillListKey + ":" + userId + ":page:" + page + ":limit:" + limit;
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
        String key = cacheBillListKey + ":" + userId + ":page:" + page + ":limit:" + limit;
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
        String key = "bill:" + id + ":user:" + userId;
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
        String key = cacheBillListKey + ":" + userId + ":page:" + page + ":limit:" + limit;
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
        String key = "bill:" + id + ":user:" + userId;
        Object value = redisTemplate.opsForValue().get(key);
        if (value != null && value.equals(nullData)) {
            // 缓存空值
            cacheNullKey(userId, id);
            return true;
        }
        return false;
    }

}