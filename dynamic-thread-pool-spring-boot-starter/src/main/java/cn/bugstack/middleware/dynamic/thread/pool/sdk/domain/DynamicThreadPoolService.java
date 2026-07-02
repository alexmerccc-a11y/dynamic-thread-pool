package cn.bugstack.middleware.dynamic.thread.pool.sdk.domain;

import cn.bugstack.middleware.dynamic.thread.pool.sdk.domain.model.entity.ThreadPoolConfigEntity;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @description 动态线程池领域服务实现
 *
 * <p>这个类持有业务系统中所有 {@link ThreadPoolExecutor} Bean 的引用。
 * “动态调整线程池”的本质，就是运行时调用这些 ThreadPoolExecutor 对象的 setter 方法，
 * 修改 corePoolSize 和 maximumPoolSize。</p>
 * @create 2024-05-12 16:08
 */
public class DynamicThreadPoolService implements IDynamicThreadPoolService {

    private final Logger logger = LoggerFactory.getLogger(DynamicThreadPoolService.class);

    /** 当前业务应用名称，用来标识这些线程池属于哪个应用。 */
    private final String applicationName;

    /**
     * Spring 注入进来的线程池集合。
     *
     * <p>key 是线程池 Bean 名称，例如 threadPoolExecutor01；
     * value 是真正工作的 ThreadPoolExecutor 对象。</p>
     */
    private final Map<String, ThreadPoolExecutor> threadPoolExecutorMap;

    public DynamicThreadPoolService(String applicationName, Map<String, ThreadPoolExecutor> threadPoolExecutorMap) {
        this.applicationName = applicationName;
        this.threadPoolExecutorMap = threadPoolExecutorMap;
    }

    @Override
    public List<ThreadPoolConfigEntity> queryThreadPoolList() {
        Set<String> threadPoolBeanNames = threadPoolExecutorMap.keySet();
        List<ThreadPoolConfigEntity> threadPoolVOS = new ArrayList<>(threadPoolBeanNames.size());
        for (String beanName : threadPoolBeanNames) {
            ThreadPoolExecutor threadPoolExecutor = threadPoolExecutorMap.get(beanName);

            // ThreadPoolConfigEntity 是一个“快照对象”，把线程池当前状态复制出来，
            // 方便后续写入 Redis、返回给管理端页面展示。
            ThreadPoolConfigEntity threadPoolConfigVO = new ThreadPoolConfigEntity(applicationName, beanName);
            threadPoolConfigVO.setCorePoolSize(threadPoolExecutor.getCorePoolSize());
            threadPoolConfigVO.setMaximumPoolSize(threadPoolExecutor.getMaximumPoolSize());
            threadPoolConfigVO.setActiveCount(threadPoolExecutor.getActiveCount());
            threadPoolConfigVO.setPoolSize(threadPoolExecutor.getPoolSize());
            threadPoolConfigVO.setQueueType(threadPoolExecutor.getQueue().getClass().getSimpleName());
            threadPoolConfigVO.setQueueSize(threadPoolExecutor.getQueue().size());
            threadPoolConfigVO.setRemainingCapacity(threadPoolExecutor.getQueue().remainingCapacity());
            threadPoolVOS.add(threadPoolConfigVO);
        }
        return threadPoolVOS;
    }

    @Override
    public ThreadPoolConfigEntity queryThreadPoolConfigByName(String threadPoolName) {
        ThreadPoolExecutor threadPoolExecutor = threadPoolExecutorMap.get(threadPoolName);

        // 找不到线程池时不抛异常，而是返回一个空快照，调用方可以据此判断数据是否完整。
        if (null == threadPoolExecutor) return new ThreadPoolConfigEntity(applicationName, threadPoolName);

        // 线程池配置数据。这里读取的是“当前时刻”的状态，不是启动时配置文件里的原始值。
        ThreadPoolConfigEntity threadPoolConfigVO = new ThreadPoolConfigEntity(applicationName, threadPoolName);
        threadPoolConfigVO.setCorePoolSize(threadPoolExecutor.getCorePoolSize());
        threadPoolConfigVO.setMaximumPoolSize(threadPoolExecutor.getMaximumPoolSize());
        threadPoolConfigVO.setActiveCount(threadPoolExecutor.getActiveCount());
        threadPoolConfigVO.setPoolSize(threadPoolExecutor.getPoolSize());
        threadPoolConfigVO.setQueueType(threadPoolExecutor.getQueue().getClass().getSimpleName());
        threadPoolConfigVO.setQueueSize(threadPoolExecutor.getQueue().size());
        threadPoolConfigVO.setRemainingCapacity(threadPoolExecutor.getQueue().remainingCapacity());

        if (logger.isDebugEnabled()) {
            logger.info("动态线程池，配置查询 应用名:{} 线程名:{} 池化配置:{}", applicationName, threadPoolName, JSON.toJSONString(threadPoolConfigVO));
        }

        return threadPoolConfigVO;
    }

    @Override
    public void updateThreadPoolConfig(ThreadPoolConfigEntity threadPoolConfigEntity) {
        // 防御性判断：空消息、发给其他应用的消息，都不应该影响当前应用。
        if (null == threadPoolConfigEntity || !applicationName.equals(threadPoolConfigEntity.getAppName())) return;
        ThreadPoolExecutor threadPoolExecutor = threadPoolExecutorMap.get(threadPoolConfigEntity.getThreadPoolName());
        if (null == threadPoolExecutor) return;

        // 设置参数：「调整核心线程数和最大线程数」。
        // 这两个方法是 ThreadPoolExecutor 原生提供的运行时调整能力。
        threadPoolExecutor.setCorePoolSize(threadPoolConfigEntity.getCorePoolSize());
        threadPoolExecutor.setMaximumPoolSize(threadPoolConfigEntity.getMaximumPoolSize());
    }

}
