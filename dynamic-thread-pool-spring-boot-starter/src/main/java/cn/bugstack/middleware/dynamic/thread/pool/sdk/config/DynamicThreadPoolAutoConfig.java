package cn.bugstack.middleware.dynamic.thread.pool.sdk.config;

import cn.bugstack.middleware.dynamic.thread.pool.sdk.trigger.listener.ThreadPoolConfigAdjustListener;
import cn.bugstack.middleware.dynamic.thread.pool.sdk.domain.DynamicThreadPoolService;
import cn.bugstack.middleware.dynamic.thread.pool.sdk.domain.IDynamicThreadPoolService;
import cn.bugstack.middleware.dynamic.thread.pool.sdk.domain.model.entity.ThreadPoolConfigEntity;
import cn.bugstack.middleware.dynamic.thread.pool.sdk.domain.model.valobj.RegistryEnumVO;
import cn.bugstack.middleware.dynamic.thread.pool.sdk.registry.IRegistry;
import cn.bugstack.middleware.dynamic.thread.pool.sdk.registry.redis.RedisRegistry;
import cn.bugstack.middleware.dynamic.thread.pool.sdk.trigger.job.ThreadPoolDataReportJob;
import org.apache.commons.lang.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @description 动态线程池 starter 的自动配置入口
 *
 * <p>这类代码通常不会被业务项目直接调用，而是在业务项目引入 starter 依赖后，
 * 由 Spring Boot 根据 {@code META-INF/spring.factories} 自动加载。</p>
 *
 * <p>本类完成 4 件核心事情：</p>
 * <ol>
 *     <li>读取 {@code dynamic.thread.pool.config.*} 配置并创建 Redisson 客户端；</li>
 *     <li>扫描 Spring 容器中的所有 {@link ThreadPoolExecutor} Bean；</li>
 *     <li>启动定时任务，把线程池运行状态上报到 Redis；</li>
 *     <li>订阅 Redis Topic，收到管理端调整消息后修改本地线程池参数。</li>
 * </ol>
 * @create 2024-05-12 15:37
 */
@Configuration
// 启用 DynamicThreadPoolAutoProperties，让 Spring 能把 yml/properties 中的配置绑定到这个对象。
@EnableConfigurationProperties(DynamicThreadPoolAutoProperties.class)
// 启用 @Scheduled 定时任务，否则 ThreadPoolDataReportJob 上的 @Scheduled 不会生效。
@EnableScheduling
public class DynamicThreadPoolAutoConfig {

    private final Logger logger = LoggerFactory.getLogger(DynamicThreadPoolAutoConfig.class);

    /**
     * 当前业务应用名称，来自 spring.application.name。
     *
     * <p>这个值会参与 Redis key 和 Redis Topic 的拼接，用来区分不同应用的线程池数据。
     * 例如：DYNAMIC_THREAD_POOL_REDIS_TOPIC_dynamic-thread-pool-test-app。</p>
     */
    private String applicationName;

    /**
     * 创建专门给动态线程池 starter 使用的 Redisson 客户端。
     *
     * <p>Redisson 是 Redis 的 Java 客户端。本项目用它操作 Redis List、Bucket 和 Topic。
     * 这里的 Bean 名称是 dynamicThreadRedissonClient，避免和业务系统自己的 RedissonClient 冲突。</p>
     */
    @Bean("dynamicThreadRedissonClient")
    public RedissonClient redissonClient(DynamicThreadPoolAutoProperties properties) {
        Config config = new Config();
        // 根据需要可以设定编解码器；https://github.com/redisson/redisson/wiki/4.-%E6%95%B0%E6%8D%AE%E5%BA%8F%E5%88%97%E5%8C%96
        config.setCodec(JsonJacksonCodec.INSTANCE);

        // 当前项目只配置了单机 Redis。如果要支持 Redis 集群或哨兵，需要换成 Redisson 对应的配置方式。
        config.useSingleServer()
                .setAddress("redis://" + properties.getHost() + ":" + properties.getPort())
                .setPassword(properties.getPassword())
                .setConnectionPoolSize(properties.getPoolSize())
                .setConnectionMinimumIdleSize(properties.getMinIdleSize())
                .setIdleConnectionTimeout(properties.getIdleTimeout())
                .setConnectTimeout(properties.getConnectTimeout())
                .setRetryAttempts(properties.getRetryAttempts())
                .setRetryInterval(properties.getRetryInterval())
                .setPingConnectionInterval(properties.getPingInterval())
                .setKeepAlive(properties.isKeepAlive())
        ;

        RedissonClient redissonClient = Redisson.create(config);

        logger.info("动态线程池，注册器（redis）链接初始化完成。{} {} {}", properties.getHost(), properties.getPoolSize(), !redissonClient.isShutdown());

        return redissonClient;
    }

    /**
     * 注册中心抽象的 Redis 实现。
     *
     * <p>业务层只依赖 IRegistry 接口，不直接依赖 RedisRegistry。
     * 这样以后如果把注册中心从 Redis 换成 Nacos、ZooKeeper，理论上只需要换实现类。</p>
     */
    @Bean
    public IRegistry redisRegistry(RedissonClient dynamicThreadRedissonClient) {
        return new RedisRegistry(dynamicThreadRedissonClient);
    }

    /**
     * 创建动态线程池领域服务。
     *
     * <p>参数 {@code Map<String, ThreadPoolExecutor> threadPoolExecutorMap} 是 Spring 的一个常用能力：
     * 当容器里有多个同类型 Bean 时，可以用 Map 注入，key 是 Bean 名称，value 是 Bean 对象。
     * 因此业务项目只要声明 {@link ThreadPoolExecutor} Bean，就会被这个 starter 自动发现。</p>
     */
    @Bean("dynamicThreadPollService")
    public DynamicThreadPoolService dynamicThreadPollService(ApplicationContext applicationContext, Map<String, ThreadPoolExecutor> threadPoolExecutorMap, RedissonClient redissonClient) {
        applicationName = applicationContext.getEnvironment().getProperty("spring.application.name");

        if (StringUtils.isBlank(applicationName)) {
            applicationName = "缺省的";
            logger.warn("动态线程池，启动提示。SpringBoot 应用未配置 spring.application.name 无法获取到应用名称！");
        }

        // 启动时先读取 Redis 中保存过的线程池配置，并覆盖本地线程池的 corePoolSize / maximumPoolSize。
        // 这样应用重启后，仍然可以沿用管理端之前调整过的配置。
        Set<String> threadPoolKeys = threadPoolExecutorMap.keySet();
        for (String threadPoolKey : threadPoolKeys) {
            ThreadPoolConfigEntity threadPoolConfigEntity = redissonClient.<ThreadPoolConfigEntity>getBucket(RegistryEnumVO.THREAD_POOL_CONFIG_PARAMETER_LIST_KEY.getKey() + "_" + applicationName + "_" + threadPoolKey).get();
            if (null == threadPoolConfigEntity) continue;
            ThreadPoolExecutor threadPoolExecutor = threadPoolExecutorMap.get(threadPoolKey);
            threadPoolExecutor.setCorePoolSize(threadPoolConfigEntity.getCorePoolSize());
            threadPoolExecutor.setMaximumPoolSize(threadPoolConfigEntity.getMaximumPoolSize());
        }

        return new DynamicThreadPoolService(applicationName, threadPoolExecutorMap);
    }

    /**
     * 创建定时上报任务。任务内部会周期性读取本地线程池状态，并写入 Redis。
     */
    @Bean
    public ThreadPoolDataReportJob threadPoolDataReportJob(IDynamicThreadPoolService dynamicThreadPoolService, IRegistry registry) {
        return new ThreadPoolDataReportJob(dynamicThreadPoolService, registry);
    }

    /**
     * 创建线程池配置变更监听器。它本身只是监听器对象，还需要注册到 Redis Topic 才能收到消息。
     */
    @Bean
    public ThreadPoolConfigAdjustListener threadPoolConfigAdjustListener(IDynamicThreadPoolService dynamicThreadPoolService, IRegistry registry) {
        return new ThreadPoolConfigAdjustListener(dynamicThreadPoolService, registry);
    }

    /**
     * 订阅管理端发布的 Redis Topic。
     *
     * <p>管理端发布到 DYNAMIC_THREAD_POOL_REDIS_TOPIC_{appName}，
     * 当前业务应用订阅同一个 Topic，收到消息后由 ThreadPoolConfigAdjustListener 处理。</p>
     */
    @Bean(name = "dynamicThreadPoolRedisTopic")
    public RTopic threadPoolConfigAdjustListener(RedissonClient redissonClient, ThreadPoolConfigAdjustListener threadPoolConfigAdjustListener) {
        RTopic topic = redissonClient.getTopic(RegistryEnumVO.DYNAMIC_THREAD_POOL_REDIS_TOPIC.getKey() + "_" + applicationName);
        topic.addListener(ThreadPoolConfigEntity.class, threadPoolConfigAdjustListener);
        return topic;
    }

}
