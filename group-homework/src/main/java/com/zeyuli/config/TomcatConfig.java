package com.zeyuli.config;


import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * 让tomcat使用虚拟线程处理每一个请求
 *
 * @author 李泽聿
 * @since 2025-11-19 10:43
 */
@Configuration
public class TomcatConfig {
    @Bean
    public TomcatProtocolHandlerCustomizer<?> protocolHandlerCustomizer(){
        return protocolHandler -> {
            protocolHandler.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        };
    }
}
