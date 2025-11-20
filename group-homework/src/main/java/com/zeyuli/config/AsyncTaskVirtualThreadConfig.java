package com.zeyuli.config;


import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.task.ThreadPoolTaskExecutorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 虚拟线程配置
 *
 * @author 李泽聿
 * @since 2025-11-19 10:34
 */
@Configuration
@EnableAsync
public class AsyncTaskVirtualThreadConfig implements AsyncConfigurer {
    /**
     * 异步任务执行器
     *
     * @return : java.util.concurrent.Executor 异步任务执行器
     */
    @Override
    public Executor getAsyncExecutor() {
        try {
            return virtualThreadExecutor();
        } catch (Exception e) {
            return commonThreadTaskExecutor();
        }
    }

    /**
     * 虚拟线程执行器
     *
     * @return : java.util.concurrent.Executor 虚拟线程执行器
     */
    @Bean
    @ConditionalOnProperty(name = "spring.threads.virtual.enabled", havingValue = "true")
    public Executor virtualThreadExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * 配置传统线程执行器
     *
     * @return : org.springframework.core.task.AsyncTaskExecutor
     * @author : 李泽聿
     * @since : 2025-11-19 10:39
     */
    @Bean
    @ConditionalOnProperty(name = "spring.threads.virtual.enabled", havingValue = "false", matchIfMissing = true)
    public AsyncTaskExecutor commonThreadTaskExecutor() {
        return new ThreadPoolTaskExecutorBuilder()
                .corePoolSize(10)
                .maxPoolSize(20)
                .queueCapacity(100)
                .threadNamePrefix("Async-")
                .build();
    }
}
