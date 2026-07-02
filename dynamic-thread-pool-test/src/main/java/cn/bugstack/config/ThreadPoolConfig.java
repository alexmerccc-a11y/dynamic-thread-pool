package cn.bugstack.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.*;

@Slf4j
@EnableAsync
@Configuration
@EnableConfigurationProperties(ThreadPoolConfigProperties.class)
public class ThreadPoolConfig {

    /**
     * 第一个业务线程池。
     *
     * <p>方法名上的 @Bean("threadPoolExecutor01") 指定了 Bean 名称。
     * starter 注入 Map<String, ThreadPoolExecutor> 时，这个名称会成为 Map 的 key，
     * 也就是管理端页面看到的 threadPoolName。</p>
     */
    @Bean("threadPoolExecutor01")
    public ThreadPoolExecutor threadPoolExecutor01(ThreadPoolConfigProperties properties) {
        // 实例化拒绝策略。当线程池和队列都满了，新任务进来时就会触发这里选中的策略。
        RejectedExecutionHandler handler;
        switch (properties.getPolicy()){
            case "AbortPolicy":
                handler = new ThreadPoolExecutor.AbortPolicy();
                break;
            case "DiscardPolicy":
                handler = new ThreadPoolExecutor.DiscardPolicy();
                break;
            case "DiscardOldestPolicy":
                handler = new ThreadPoolExecutor.DiscardOldestPolicy();
                break;
            case "CallerRunsPolicy":
                handler = new ThreadPoolExecutor.CallerRunsPolicy();
                break;
            default:
                handler = new ThreadPoolExecutor.AbortPolicy();
                break;
        }

        // 创建线程池。starter 动态调整的就是 corePoolSize 和 maximumPoolSize 这两个参数。
        return new ThreadPoolExecutor(properties.getCorePoolSize(),
                properties.getMaxPoolSize(),
                properties.getKeepAliveTime(),
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(properties.getBlockQueueSize()),
                Executors.defaultThreadFactory(),
                handler);
    }

    /**
     * 第二个业务线程池。
     *
     * <p>它和 threadPoolExecutor01 使用同一组配置，只是 Bean 名称不同。
     * 这样可以演示一个应用里存在多个线程池时，starter 会全部扫描并上报。</p>
     */
    @Bean("threadPoolExecutor02")
    public ThreadPoolExecutor threadPoolExecutor02(ThreadPoolConfigProperties properties) {
        // 实例化拒绝策略。当线程池和队列都满了，新任务进来时就会触发这里选中的策略。
        RejectedExecutionHandler handler;
        switch (properties.getPolicy()){
            case "AbortPolicy":
                handler = new ThreadPoolExecutor.AbortPolicy();
                break;
            case "DiscardPolicy":
                handler = new ThreadPoolExecutor.DiscardPolicy();
                break;
            case "DiscardOldestPolicy":
                handler = new ThreadPoolExecutor.DiscardOldestPolicy();
                break;
            case "CallerRunsPolicy":
                handler = new ThreadPoolExecutor.CallerRunsPolicy();
                break;
            default:
                handler = new ThreadPoolExecutor.AbortPolicy();
                break;
        }

        // 创建线程池。LinkedBlockingQueue 的容量来自 blockQueueSize。
        return new ThreadPoolExecutor(properties.getCorePoolSize(),
                properties.getMaxPoolSize(),
                properties.getKeepAliveTime(),
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(properties.getBlockQueueSize()),
                Executors.defaultThreadFactory(),
                handler);
    }

}
