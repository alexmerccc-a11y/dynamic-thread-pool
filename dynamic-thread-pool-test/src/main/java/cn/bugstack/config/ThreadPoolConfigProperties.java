package cn.bugstack.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "thread.pool.executor.config", ignoreInvalidFields = true)
public class ThreadPoolConfigProperties {

    /**
     * 核心线程数。
     *
     * <p>对应 yml 中的 thread.pool.executor.config.core-pool-size。
     * Spring Boot 会把短横线命名 core-pool-size 自动绑定到 Java 驼峰命名 corePoolSize。</p>
     */
    private Integer corePoolSize = 20;

    /** 最大线程数，对应 yml 中的 max-pool-size。 */
    private Integer maxPoolSize = 200;

    /**
     * 空闲线程存活时间。
     *
     * <p>ThreadPoolConfig 创建线程池时使用 TimeUnit.SECONDS，
     * 所以这里的数字最终会按“秒”理解。</p>
     */
    private Long keepAliveTime = 10L;

    /** 阻塞队列容量，对应 LinkedBlockingQueue 的最大可排队任务数。 */
    private Integer blockQueueSize = 5000;

    /*
     * AbortPolicy：丢弃任务并抛出RejectedExecutionException异常。
     * DiscardPolicy：直接丢弃任务，但是不会抛出异常
     * DiscardOldestPolicy：将最早进入队列的任务删除，之后再尝试加入队列的任务被拒绝
     * CallerRunsPolicy：如果任务添加线程池失败，那么主线程自己执行该任务
     * */
    /** 拒绝策略名称。ThreadPoolConfig 会根据这个字符串创建具体的拒绝策略对象。 */
    private String policy = "AbortPolicy";

}
