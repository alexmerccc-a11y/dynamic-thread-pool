package cn.bugstack.middleware.dynamic.thread.pool.sdk.domain.model.valobj;

/**
 * @description 注册中心枚举值对象 key
 *
 * <p>这里集中管理 Redis key / Topic 前缀，避免字符串散落在各个类里。
 * 学习时可以从这三个 key 反推数据流向。</p>
 * @create 2024-05-12 16:26
 */
public enum RegistryEnumVO {

    /** Redis List：保存所有应用上报的线程池列表，用于管理端首页展示。 */
    THREAD_POOL_CONFIG_LIST_KEY("THREAD_POOL_CONFIG_LIST_KEY", "池化配置列表"),

    /** Redis Bucket：保存某个应用某个线程池的详细配置，key 后面会拼 appName 和 threadPoolName。 */
    THREAD_POOL_CONFIG_PARAMETER_LIST_KEY("THREAD_POOL_CONFIG_PARAMETER_LIST_KEY", "池化配置参数"),

    /** Redis Topic：管理端发布配置变更消息，业务应用订阅后实时调整本地线程池。 */
    DYNAMIC_THREAD_POOL_REDIS_TOPIC("DYNAMIC_THREAD_POOL_REDIS_TOPIC", "动态线程池监听主题配置");

    /** 实际写入 Redis 时使用的 key 或 Topic 前缀。 */
    private final String key;

    /** 给开发者看的中文说明，不参与业务逻辑。 */
    private final String desc;

    RegistryEnumVO(String key, String desc) {
        this.key = key;
        this.desc = desc;
    }

    public String getKey() {
        return key;
    }

    public String getDesc() {
        return desc;
    }


}
