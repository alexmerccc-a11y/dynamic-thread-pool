package cn.bugstack.middleware.dynamic.thread.pool.sdk.domain.model.entity;

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
public class ThreadPoolConfigEntity {

    /**
     * 应用名称，用来区分不同业务系统。
     *
     * <p>例如 dynamic-thread-pool-test-app。</p>
     */
    private String appName;

    /**
     * 线程池名称，也就是 ThreadPoolExecutor 在 Spring 容器里的 Bean 名称。
     *
     * <p>例如 threadPoolExecutor01。</p>
     */
    private String threadPoolName;

    /**
     * 核心线程数。线程池会尽量保留这么多工作线程。
     */
    private int corePoolSize;

    /**
     * 最大线程数。当队列满了且还没到最大线程数时，线程池可以继续创建线程。
     */
    private int maximumPoolSize;

    /**
     * 当前活跃线程数，表示正在执行任务的线程数量。
     */
    private int activeCount;

    /**
     * 当前池中线程数，包含空闲线程和正在工作的线程。
     */
    private int poolSize;

    /**
     * 队列类型，例如 LinkedBlockingQueue。
     */
    private String queueType;

    /**
     * 当前队列任务数，表示已经提交但还没被线程执行的任务数量。
     */
    private int queueSize;

    /**
     * 队列剩余容量，表示队列还能再放多少个等待执行的任务。
     */
    private int remainingCapacity;

    /**
     * 无参构造方法给 JSON 反序列化使用。
     *
     * <p>Redisson/Jackson 收到 Redis 消息或读取 Redis 数据时，需要先创建空对象，
     * 再把 JSON 字段填进去。</p>
     */
    public ThreadPoolConfigEntity() {
    }

    /**
     * 业务代码创建快照时，通常先确定应用名和线程池名，再补充线程池参数。
     */
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
