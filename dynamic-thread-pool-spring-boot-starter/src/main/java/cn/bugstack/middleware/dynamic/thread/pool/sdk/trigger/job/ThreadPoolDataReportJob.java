package cn.bugstack.middleware.dynamic.thread.pool.sdk.trigger.job;

import cn.bugstack.middleware.dynamic.thread.pool.sdk.registry.IRegistry;
import cn.bugstack.middleware.dynamic.thread.pool.sdk.domain.IDynamicThreadPoolService;
import cn.bugstack.middleware.dynamic.thread.pool.sdk.domain.model.entity.ThreadPoolConfigEntity;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

/**
 * @description 线程池数据上报任务
 *
 * <p>这个任务负责把“业务应用本地线程池的运行状态”定期同步到 Redis。
 * 管理端页面查询到的数据，主要来自这里的上报结果。</p>
 * @create 2024-05-12 16:29
 */
public class ThreadPoolDataReportJob {

    private final Logger logger = LoggerFactory.getLogger(ThreadPoolDataReportJob.class);

    /** 负责读取本地 ThreadPoolExecutor 状态。 */
    private final IDynamicThreadPoolService dynamicThreadPoolService;

    /** 负责把读取到的状态写入 Redis。 */
    private final IRegistry registry;

    public ThreadPoolDataReportJob(IDynamicThreadPoolService dynamicThreadPoolService, IRegistry registry) {
        this.dynamicThreadPoolService = dynamicThreadPoolService;
        this.registry = registry;
    }

    /**
     * 每 20 秒执行一次线程池状态上报。
     *
     * <p>cron = "0/20 * * * * ?" 表示从每分钟第 0 秒开始，每隔 20 秒触发一次。</p>
     */
    @Scheduled(cron = "0/20 * * * * ?")
    public void execReportThreadPoolList() {
        // 1. 从本地 JVM 中读取所有线程池的当前状态。
        List<ThreadPoolConfigEntity> threadPoolConfigEntities = dynamicThreadPoolService.queryThreadPoolList();

        // 2. 写入 Redis List，供管理端查询“线程池列表”。
        registry.reportThreadPool(threadPoolConfigEntities);
        logger.info("动态线程池，上报线程池信息：{}", JSON.toJSONString(threadPoolConfigEntities));

        // 3. 再逐个写入 Redis Bucket，供管理端查询“某个线程池详情”。
        for (ThreadPoolConfigEntity threadPoolConfigEntity : threadPoolConfigEntities) {
            registry.reportThreadPoolConfigParameter(threadPoolConfigEntity);
            logger.info("动态线程池，上报线程池配置：{}", JSON.toJSONString(threadPoolConfigEntity));
        }

    }

}
