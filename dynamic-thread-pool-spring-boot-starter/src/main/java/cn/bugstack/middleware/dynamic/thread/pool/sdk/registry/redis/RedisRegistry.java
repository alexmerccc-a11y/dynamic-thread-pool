package cn.bugstack.middleware.dynamic.thread.pool.sdk.registry.redis;

import cn.bugstack.middleware.dynamic.thread.pool.sdk.domain.model.entity.ThreadPoolConfigEntity;
import cn.bugstack.middleware.dynamic.thread.pool.sdk.domain.model.valobj.RegistryEnumVO;
import cn.bugstack.middleware.dynamic.thread.pool.sdk.registry.IRegistry;
import org.redisson.api.RBucket;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.util.List;

/**
 * @description Redis 注册中心
 *
 * <p>这里把 IRegistry 接口落到了 Redis 上：</p>
 * <ul>
 *     <li>Redis List 存线程池列表；</li>
 *     <li>Redis Bucket 存单个线程池配置；</li>
 *     <li>Redis Topic 的订阅和发布在自动配置类、管理端 Controller 中完成。</li>
 * </ul>
 * @create 2024-05-12 16:22
 */
public class RedisRegistry implements IRegistry {

    /** Redisson 客户端，负责真正访问 Redis。 */
    private final RedissonClient redissonClient;

    public RedisRegistry(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public void reportThreadPool(List<ThreadPoolConfigEntity> threadPoolEntities) {
        // THREAD_POOL_CONFIG_LIST_KEY 是管理端查询列表时读取的 Redis List。
        RList<ThreadPoolConfigEntity> list = redissonClient.getList(RegistryEnumVO.THREAD_POOL_CONFIG_LIST_KEY.getKey());

        // 本项目每次上报都先清空再写入，表示 Redis 中只保留“最近一次上报”的列表快照。
        list.delete();
        list.addAll(threadPoolEntities);
    }

    @Override
    public void reportThreadPoolConfigParameter(ThreadPoolConfigEntity threadPoolConfigEntity) {
        // 单个线程池配置的 key 会带上应用名和线程池名，避免不同应用、不同线程池互相覆盖。
        // 示例：THREAD_POOL_CONFIG_PARAMETER_LIST_KEY_dynamic-thread-pool-test-app_threadPoolExecutor01
        String cacheKey = RegistryEnumVO.THREAD_POOL_CONFIG_PARAMETER_LIST_KEY.getKey() + "_" + threadPoolConfigEntity.getAppName() + "_" + threadPoolConfigEntity.getThreadPoolName();
        RBucket<ThreadPoolConfigEntity> bucket = redissonClient.getBucket(cacheKey);

        // 保存 30 天：如果业务应用重启，可以读取这份配置并恢复到上次管理端调整后的参数。
        bucket.set(threadPoolConfigEntity, Duration.ofDays(30));
    }

}
