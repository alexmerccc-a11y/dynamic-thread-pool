package cn.bugstack.middleware.dynamic.thread.pool.sdk.trigger.listener;

import cn.bugstack.middleware.dynamic.thread.pool.sdk.domain.IDynamicThreadPoolService;
import cn.bugstack.middleware.dynamic.thread.pool.sdk.domain.model.entity.ThreadPoolConfigEntity;
import cn.bugstack.middleware.dynamic.thread.pool.sdk.registry.IRegistry;
import com.alibaba.fastjson.JSON;
import org.redisson.api.listener.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @description 动态线程池变更监听
 *
 * <p>管理端调用修改接口时，会往 Redis Topic 发布一条 ThreadPoolConfigEntity 消息。
 * 当前类就是业务应用侧的订阅者：收到消息后，修改本地线程池配置，并把最新状态再写回 Redis。</p>
 * @create 2024-05-12 16:46
 */
public class ThreadPoolConfigAdjustListener implements MessageListener<ThreadPoolConfigEntity> {

    private Logger logger = LoggerFactory.getLogger(ThreadPoolConfigAdjustListener.class);

    /** 负责真正修改本地 ThreadPoolExecutor。 */
    private final IDynamicThreadPoolService dynamicThreadPoolService;

    /** 修改完成后，用它把最新状态重新上报到 Redis。 */
    private final IRegistry registry;

    public ThreadPoolConfigAdjustListener(IDynamicThreadPoolService dynamicThreadPoolService, IRegistry registry) {
        this.dynamicThreadPoolService = dynamicThreadPoolService;
        this.registry = registry;
    }

    @Override
    public void onMessage(CharSequence charSequence, ThreadPoolConfigEntity threadPoolConfigEntity) {
        // charSequence 是 Redis Topic 名称；threadPoolConfigEntity 是管理端发布的消息体。
        logger.info("动态线程池，调整线程池配置。线程池名称:{} 核心线程数:{} 最大线程数:{}", threadPoolConfigEntity.getThreadPoolName(), threadPoolConfigEntity.getPoolSize(), threadPoolConfigEntity.getMaximumPoolSize());

        // 1. 修改当前 JVM 中对应线程池的 corePoolSize / maximumPoolSize。
        dynamicThreadPoolService.updateThreadPoolConfig(threadPoolConfigEntity);

        // 2. 修改后立刻上报最新列表数据，让管理端页面尽快看到新状态。
        List<ThreadPoolConfigEntity> threadPoolConfigEntities = dynamicThreadPoolService.queryThreadPoolList();
        registry.reportThreadPool(threadPoolConfigEntities);

        // 3. 再单独上报被修改的线程池详情，用于详情查询和下次应用启动时恢复配置。
        ThreadPoolConfigEntity threadPoolConfigEntityCurrent = dynamicThreadPoolService.queryThreadPoolConfigByName(threadPoolConfigEntity.getThreadPoolName());
        registry.reportThreadPoolConfigParameter(threadPoolConfigEntityCurrent);
        logger.info("动态线程池，上报线程池配置：{}", JSON.toJSONString(threadPoolConfigEntity));
    }

}
