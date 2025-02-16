package com.uluckyxh.airmonitorplatform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class ThreadPoolConfig {

    /**
     * 新任务提交
     * ↓
     * 是否有空闲的核心线程？
     * ├── 有 → 直接执行
     * └── 没有
     *     ↓
     *     队列是否已满？
     *     ├── 未满 → 加入队列等待
     *     └── 已满
     *         ↓
     *         是否达到最大线程数？
     *         ├── 未达到 → 创建新线程执行
     *         └── 已达到 → 触发拒绝策略
     *             ├── AbortPolicy：直接抛出异常
     *             ├── CallerRunsPolicy：在调用者线程中执行任务
     *             ├── DiscardOldestPolicy：丢弃队列中最旧的任务
     *             └── DiscardPolicy：直接丢弃任务
     *
     */
    @Bean("MyAsyncExecutor")
    public ThreadPoolTaskExecutor asyncExecutor() {
        // 创建ThreadPoolTaskExecutor对象，这是Spring提供的线程池执行器
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数：线程池创建时候初始化的线程数
        // 这些线程会一直存活，即使它们处于空闲状态
        executor.setCorePoolSize(5);

        // 最大线程数：线程池最大的线程数，只有在工作队列满了之后才会创建超出核心线程数的线程
        // 当任务数增加时，线程池会创建新线程处理任务，直到达到maxPoolSize
        executor.setMaxPoolSize(10);

        // 队列容量：用于缓存任务的阻塞队列的大小
        // 当核心线程都在工作时，新任务会被放到队列中等待
        // 只有当队列满了后，才会创建新的线程（但不超过最大线程数）
        executor.setQueueCapacity(25);

        // 线程名前缀：设置线程名的前缀，方便查看日志时区分不同线程池
        executor.setThreadNamePrefix("Async-");

        // 拒绝策略：当线程池和队列都满了时的处理策略
        // CALLER_RUNS：在调用者线程中执行任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 等待所有任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // 等待任务完成的最长时间
        // 如果超过这个时间，会强制销毁线程池
        executor.setAwaitTerminationSeconds(60);

        // 初始化线程池
        executor.initialize();

        return executor;
    }


}
