package cn.bugstack.middleware.dynamic.thread.pool.sdk.domain;

import cn.bugstack.middleware.dynamic.thread.pool.sdk.domain.model.entity.ThreadPoolConfigEntity;

import java.util.List;

/**
 * @description 动态线程池领域服务接口
 *
 * <p>接口只描述“能做什么”，不关心“怎么做”。本项目目前只有
 * {@link DynamicThreadPoolService} 一个实现类。</p>
 * @create 2024-05-12 16:04
 */
public interface IDynamicThreadPoolService {

    /**
     * 查询当前应用内所有被 Spring 管理的线程池状态。
     *
     * @return 每个线程池的一份配置/运行状态快照
     */
    List<ThreadPoolConfigEntity> queryThreadPoolList();

    /**
     * 按线程池 Bean 名称查询一个线程池。
     *
     * @param threadPoolName 线程池在 Spring 容器里的 Bean 名称，例如 threadPoolExecutor01
     * @return 线程池快照；如果找不到线程池，返回只包含 appName/threadPoolName 的空快照
     */
    ThreadPoolConfigEntity queryThreadPoolConfigByName(String threadPoolName);

    /**
     * 根据管理端下发的配置修改本地线程池参数。
     *
     * @param threadPoolConfigEntity 管理端发来的目标配置
     */
    void updateThreadPoolConfig(ThreadPoolConfigEntity threadPoolConfigEntity);

}
