package cn.bugstack.middleware.dynamic.thread.pool.sdk.domain.model.entity;

/**
 * 管理端使用的线程池配置传输对象。
 *
 * <p>它和 starter 模块里的 ThreadPoolConfigEntity 字段保持一致，
 * 这样管理端从 Redis 读取数据、向 Redis Topic 发布消息时，双方才能正确序列化和反序列化。</p>
 *
 * <p>更完整的工程中通常会把这类公共 DTO 抽到 common 模块，避免 admin 和 starter 各维护一份。</p>
 */
public class ThreadPoolConfigEntity {

    /**
     * 应用名称，用来找到目标业务应用。
     */
    private String appName;

    /**
     * 线程池 Bean 名称，用来找到目标应用里的具体 ThreadPoolExecutor。
     */
    private String threadPoolName;

    /**
     * 核心线程数，管理端修改时主要写这个字段。
     */
    private int corePoolSize;

    /**
     * 最大线程数，管理端修改时主要写这个字段。
     */
    private int maximumPoolSize;

    /**
     * 当前活跃线程数，主要用于页面展示。
     */
    private int activeCount;

    /**
     * 当前池中线程数，主要用于页面展示。
     */
    private int poolSize;

    /**
     * 队列类型，主要用于页面展示。
     */
    private String queueType;

    /**
     * 当前队列任务数，主要用于页面展示。
     */
    private int queueSize;

    /**
     * 队列剩余容量，主要用于页面展示。
     */
    private int remainingCapacity;

    /** 给 JSON 反序列化使用的无参构造方法。 */
    public ThreadPoolConfigEntity() {
    }

    /** 创建一个只带定位信息的配置对象。 */
    public ThreadPoolConfigEntity(String appName, String threadPoolName) {
        this.appName = appName;
        this.threadPoolName = threadPoolName;
    }

    public String getAppName() {
        return appName;
    }

    public String getThreadPoolName() {
        return threadPoolName;
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public void setMaximumPoolSize(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }

    public int getActiveCount() {
        return activeCount;
    }

    public void setActiveCount(int activeCount) {
        this.activeCount = activeCount;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public String getQueueType() {
        return queueType;
    }

    public void setQueueType(String queueType) {
        this.queueType = queueType;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    public int getRemainingCapacity() {
        return remainingCapacity;
    }

    public void setRemainingCapacity(int remainingCapacity) {
        this.remainingCapacity = remainingCapacity;
    }

}
