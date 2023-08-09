package com.ie.pdf2.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class SyncConfiguration {


    @Value("${task.core.pool-size}")
    int CorePoolSize = 2;
    @Value("${task.max.pool-size}")
    int MaxPoolSize = 5;
    @Value("${task.queue.capacity}")
    int QueueCapacity = 20;
    @Value("${task.keep.alive.seconds}")
    int KeepAliveSeconds = 200;


    @Bean(name = "ieTaskExecutor")
    public Executor taskExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //核心线程数
        executor.setCorePoolSize(CorePoolSize);
        //最大线程数
        executor.setMaxPoolSize(MaxPoolSize);
        //队列中允许的最大空闲任务数量
        executor.setQueueCapacity(QueueCapacity);
        //任务被处理后，线程应 trounce出队列，直到任务执行结束为止
        executor.setKeepAliveSeconds(KeepAliveSeconds);

        //名称空间
        executor.setThreadNamePrefix("ieTask-");

        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

}
