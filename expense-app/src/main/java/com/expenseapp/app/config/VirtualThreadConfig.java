package com.expenseapp.app.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class VirtualThreadConfig {

    @Bean
    @Qualifier("pipelineExecutor")
    public ExecutorService pipelineExecutor(){
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Bean
    @Qualifier("scheduledExecutor")
    public ScheduledExecutorService retryExecutor() {
        return Executors.newScheduledThreadPool(2);
    }
}
