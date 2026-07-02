package cn.bugstack.middleware.dynamic.thread.pool.sdk.registry;

import cn.bugstack.middleware.dynamic.thread.pool.sdk.domain.model.entity.ThreadPoolConfigEntity;

import java.util.List;

/**
 * @description 注册中心接口
 *
 * <p>注册中心负责保存和广播线程池数据。本项目用 Redis 实现，
 * 但业务层只依赖这个接口，降低了和 Redis 的耦合。</p>
 * @create 2024-05-12 16:21
 */
public interface IRegistry {

    /**
     * 上报当前应用内所有线程池的列表数据。
     *
     * @param threadPoolEntities 所有线程池的当前状态快照
     */
    void reportThreadPool(List<ThreadPoolConfigEntity> threadPoolEntities);

    /**
     * 上报某一个线程池的详细配置。
     *
     * @param threadPoolConfigEntity 单个线程池的当前状态快照
     */
    void reportThreadPoolConfigParameter(ThreadPoolConfigEntity threadPoolConfigEntity);

}
