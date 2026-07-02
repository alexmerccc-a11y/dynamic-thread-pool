package cn.bugstack;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@Configurable
// 排除 RedissonAutoConfiguration，是为了避免业务测试应用自动创建默认 RedissonClient，
// 让动态线程池 starter 使用自己命名的 dynamicThreadRedissonClient。
@SpringBootApplication
public class Application {

    /**
     * 测试应用启动入口。
     *
     * <p>它模拟一个真实业务系统：业务系统声明线程池 Bean，引入 starter 后，
     * starter 自动发现这些线程池并上报/调整。</p>
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }

    /**
     * 应用启动后持续向 threadPoolExecutor02 提交任务。
     *
     * <p>这样管理端页面能看到 activeCount、queueSize 等指标不断变化，
     * 便于观察动态调整 corePoolSize / maximumPoolSize 后的效果。</p>
     */
    @Bean
    public ApplicationRunner applicationRunner(ExecutorService threadPoolExecutor02) {
        return args -> {
            while (true){
                // 创建一个随机时间生成器
                Random random = new Random();
                // 随机时间，用于模拟任务启动延迟
                int initialDelay = random.nextInt(10) + 1; // 1到10秒之间
                // 随机休眠时间，用于模拟任务执行时间
                int sleepTime = random.nextInt(10) + 1; // 1到10秒之间

                // 提交任务到线程池
                threadPoolExecutor02.submit(() -> {
                    try {
                        // 模拟任务启动延迟
                        TimeUnit.SECONDS.sleep(initialDelay);
                        System.out.println("Task started after " + initialDelay + " seconds.");

                        // 模拟任务执行
                        TimeUnit.SECONDS.sleep(sleepTime);
                        System.out.println("Task executed for " + sleepTime + " seconds.");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });

                // 控制提交速度。随机短暂休眠可以制造不稳定流量，更接近真实业务的波动。
                Thread.sleep(random.nextInt(50) + 1);
            }
        };
    }


}
