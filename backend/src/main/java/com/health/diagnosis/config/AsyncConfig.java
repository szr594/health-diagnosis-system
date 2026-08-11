package com.health.diagnosis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步线程池配置。
 *
 * <p>用于异步调用 Python AI 服务，避免阻塞 Web 主线程
 * （对应提示词中「RestTemplate 异步调用 FastAPI，避免阻塞主线程」的要求）。</p>
 */
@Configuration
public class AsyncConfig {

    @Bean(name = "aiExecutor")
    public Executor aiExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("ai-call-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
