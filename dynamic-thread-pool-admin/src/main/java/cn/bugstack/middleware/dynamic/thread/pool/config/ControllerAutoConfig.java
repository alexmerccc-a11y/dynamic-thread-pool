package cn.bugstack.middleware.dynamic.thread.pool.config;


import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author ljx
 * @date 2026/7/2 21:58
 */
@Configuration
// 关键：在这里指定你 Controller 所在的包路径，支持配置多个
@ComponentScan(basePackages = "cn.bugstack.middleware.dynamic.thread.pool.trigger")
@ConditionalOnProperty(prefix = "sdk.controller", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ControllerAutoConfig {
}