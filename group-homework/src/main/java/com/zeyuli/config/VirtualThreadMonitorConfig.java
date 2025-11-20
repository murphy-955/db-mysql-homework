package com.zeyuli.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 *
 *
 * @author 李泽聿
 * @since 2025-11-19 11:02
 */
@Configuration
@Slf4j
public class VirtualThreadMonitorConfig {
    @Scheduled(fixedRate = 1000)
    public void monitorVirtualThreads() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

        // 获取所有线程
        Thread[] threads = new Thread[Thread.activeCount()];
        Thread.enumerate(threads);

        long virtualThreadCount = 0;
        long blockedThreadCount = 0;

        for (Thread thread : threads) {
            if (thread != null && thread.isVirtual()) {
                virtualThreadCount++;
                if (thread.getState() == Thread.State.BLOCKED) {
                    blockedThreadCount++;
                    log.warn("检测到阻塞的虚拟线程: {} - {}", thread.getName(), thread.getState());
                }
            }
        }

        log.info("虚拟线程监控 - 总数: {}, 阻塞: {}, 平台线程: {}",
                virtualThreadCount, blockedThreadCount, threadMXBean.getThreadCount());
    }
}
