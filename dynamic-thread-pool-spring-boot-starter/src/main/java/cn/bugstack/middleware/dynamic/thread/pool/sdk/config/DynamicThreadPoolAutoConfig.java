package cn.bugstack.middleware.dynamic.thread.pool.sdk.config;


import cn.bugstack.middleware.dynamic.thread.pool.sdk.domain.DynamicThreadPoolService;
import cn.bugstack.middleware.dynamic.thread.pool.sdk.trigger.listener.ThreadPoolConfigAdjustListener;
import org.apache.commons.lang.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

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

    private String applicationName;


    /**
     * 创建动态线程池领域服务。
     *
     * <p>参数 {@code Map<String, ThreadPoolExecutor> threadPoolExecutorMap} 是 Spring 的一个常用能力：
     * 当容器里有多个同类型 Bean 时，可以用 Map 注入，key 是 Bean 名称，value 是 Bean 对象。
     * 因此业务项目只要声明 {@link ThreadPoolExecutor} Bean，就会被这个 starter 自动发现。</p>
     */
    @Bean("dynamicThreadPollService")
    public DynamicThreadPoolService dynamicThreadPollService(ApplicationContext applicationContext, Map<String, ThreadPoolExecutor> threadPoolExecutorMap) {
        applicationName = applicationContext.getEnvironment().getProperty("spring.application.name");

        if (StringUtils.isBlank(applicationName)) {
            applicationName = "缺省的";
            logger.warn("动态线程池，启动提示。SpringBoot 应用未配置 spring.application.name 无法获取到应用名称！");
        }

        return new DynamicThreadPoolService(applicationName, threadPoolExecutorMap);
    }
    @Bean("threadPoolConfigAdjustListener")
    public ThreadPoolConfigAdjustListener threadPoolConfigAdjustListener(DynamicThreadPoolService dynamicThreadPollService) {
        return new ThreadPoolConfigAdjustListener(dynamicThreadPollService);
    }

}
