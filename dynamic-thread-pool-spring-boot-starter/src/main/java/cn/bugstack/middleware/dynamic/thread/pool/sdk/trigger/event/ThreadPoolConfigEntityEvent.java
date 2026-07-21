package cn.bugstack.middleware.dynamic.thread.pool.sdk.trigger.event;


import cn.bugstack.middleware.dynamic.thread.pool.sdk.domain.model.entity.ThreadPoolConfigEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.ApplicationEvent;

/**
 * @description 线程池配置实体对象
 *
 * <p>它不是线程池本身，而是线程池的一份“数据快照”。</p>
 *
 * <p>这个对象会在三个地方流转：</p>
 * <ol>
 *     <li>starter 从 ThreadPoolExecutor 读取运行状态后，封装成它；</li>
 *     <li>starter 把它写入 Redis，供管理端查询；</li>
 *     <li>管理端修改 corePoolSize / maximumPoolSize 后，再把它发布给业务应用。</li>
 * </ol>
 * @create 2024-05-12 16:05
 */
@Data
public class ThreadPoolConfigEntityEvent extends ApplicationEvent {
    private ThreadPoolConfigEntity threadPoolConfigEntity;

    public ThreadPoolConfigEntityEvent(ThreadPoolConfigEntity threadPoolConfigEntity) {
        super(threadPoolConfigEntity);
        this.threadPoolConfigEntity = threadPoolConfigEntity;
    }
}
