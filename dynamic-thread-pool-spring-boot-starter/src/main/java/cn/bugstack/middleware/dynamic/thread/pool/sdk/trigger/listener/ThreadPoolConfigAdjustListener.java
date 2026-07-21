package cn.bugstack.middleware.dynamic.thread.pool.sdk.trigger.listener;

import cn.bugstack.middleware.dynamic.thread.pool.sdk.domain.IDynamicThreadPoolService;
import cn.bugstack.middleware.dynamic.thread.pool.sdk.trigger.event.ThreadPoolConfigEntityEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @description 动态线程池变更监听
 *
 * <p>管理端调用修改接口时，会往 Redis Topic 发布一条 ThreadPoolConfigEntity 消息。
 * 当前类就是业务应用侧的订阅者：收到消息后，修改本地线程池配置，并把最新状态再写回 Redis。</p>
 * @create 2024-05-12 16:46
 */
@Slf4j
public class ThreadPoolConfigAdjustListener {
    public ThreadPoolConfigAdjustListener(IDynamicThreadPoolService dynamicThreadPoolService) {
        this.dynamicThreadPoolService = dynamicThreadPoolService;
    }

    private  IDynamicThreadPoolService dynamicThreadPoolService;

    @EventListener
    public void onMessage(ThreadPoolConfigEntityEvent event) {
        // 1. 修改当前 JVM 中对应线程池的 corePoolSize / maximumPoolSize。
        dynamicThreadPoolService.updateThreadPoolConfig(event.getThreadPoolConfigEntity());

    }

}
