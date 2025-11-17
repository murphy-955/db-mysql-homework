package com.zeyuli.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.zeyuli.pojo.bo.GetBillListBo;
import com.zeyuli.pojo.po.BillPo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * jdk本地缓存配置
 *
 * @author 李泽聿
 * @since 2025-11-15 15:50
 */
@Configuration
public class LocalCacheConfig {

    @Bean
    public Cache<String, BillPo> billLocalCache() {
        return Caffeine.newBuilder()
                // 基于容量的淘汰
                .maximumSize(100000) // 根据内存调整，800万数据中缓存10万条热点数据
                // 基于时间的淘汰
                .expireAfterWrite(10, TimeUnit.MINUTES) // 写入后10分钟过期
                .expireAfterAccess(5, TimeUnit.MINUTES)  // 访问后5分钟过期
                // 弱引用，便于GC回收
                .weakKeys()
                .weakValues()
                // 记录命中率统计
                .recordStats()
                .build();
    }

    @Bean
    public Cache<String, List<GetBillListBo>> billListCache() {
        return Caffeine.newBuilder()
                .maximumSize(1000) // 缓存查询结果列表
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .weakKeys()
                .weakValues()
                .recordStats()
                .build();
    }
}