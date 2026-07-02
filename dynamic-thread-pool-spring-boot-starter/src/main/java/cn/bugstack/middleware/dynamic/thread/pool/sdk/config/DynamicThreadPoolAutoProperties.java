package cn.bugstack.middleware.dynamic.thread.pool.sdk.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**

 * @description 动态线程池 starter 的配置属性对象
 *
 * <p>{@link ConfigurationProperties} 会把配置文件里的
 * {@code dynamic.thread.pool.config.*} 绑定到本类字段。
 * 例如 {@code dynamic.thread.pool.config.host} 会绑定到 {@link #host}。</p>
 *
 * <p>初学者可以把这个类理解成“配置文件在 Java 代码里的接收器”：
 * yml 写的是字符串和数字，Spring 启动时会自动转成这个 Java 对象。</p>
 * @create 2024-05-12 16:23
 */
@ConfigurationProperties(prefix = "dynamic.thread.pool.config", ignoreInvalidFields = true)
public class DynamicThreadPoolAutoProperties {

    /**
     * 动态线程池开关。
     *
     * <p>注意：当前代码只是声明了这个字段，还没有用 @ConditionalOnProperty
     * 或 if 判断控制 starter 是否启用。</p>
     */
    private boolean enable;
    /** Redis 主机地址，例如 127.0.0.1。 */
    private String host;
    /** Redis 端口，例如 6379。 */
    private int port;
    /** Redis 密码；没有密码时通常为空。 */
    private String password;
    /** Redisson 连接池大小，默认 64。 */
    private int poolSize = 64;
    /** Redisson 最小空闲连接数，默认 10。 */
    private int minIdleSize = 10;
    /** 连接最大空闲时间，单位毫秒，超过后空闲连接会被关闭。 */
    private int idleTimeout = 10000;
    /** 建立连接的超时时间，单位毫秒。 */
    private int connectTimeout = 10000;
    /** 连接失败后的重试次数。 */
    private int retryAttempts = 3;
    /** 两次连接重试之间的间隔，单位毫秒。 */
    private int retryInterval = 1000;
    /** 定期检查连接是否可用的间隔，单位毫秒；0 表示不主动检查。 */
    private int pingInterval = 0;
    /** 是否保持 TCP 长连接。 */
    private boolean keepAlive = true;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public int getMinIdleSize() {
        return minIdleSize;
    }

    public void setMinIdleSize(int minIdleSize) {
        this.minIdleSize = minIdleSize;
    }

    public int getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(int idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getRetryAttempts() {
        return retryAttempts;
    }

    public void setRetryAttempts(int retryAttempts) {
        this.retryAttempts = retryAttempts;
    }

    public int getRetryInterval() {
        return retryInterval;
    }

    public void setRetryInterval(int retryInterval) {
        this.retryInterval = retryInterval;
    }

    public int getPingInterval() {
        return pingInterval;
    }

    public void setPingInterval(int pingInterval) {
        this.pingInterval = pingInterval;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

}
