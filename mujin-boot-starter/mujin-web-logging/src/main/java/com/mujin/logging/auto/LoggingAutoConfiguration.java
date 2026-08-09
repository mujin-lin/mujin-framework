package com.mujin.logging.auto;

import com.mujin.logging.aop.OperationLogAspect;
import com.mujin.logging.collector.DefaultLogContextCollector;
import com.mujin.logging.collector.LoginUserCollector;
import com.mujin.logging.collector.OperationLogCollector;
import com.mujin.logging.collector.ParamCollector;
import com.mujin.logging.collector.SpelParamCollector;
import com.mujin.logging.collector.WebContextCollector;
import com.mujin.logging.configuration.LoggingProperties;
import com.mujin.logging.executor.LoggingExecutor;
import com.mujin.logging.persistence.FileLogStorage;
import com.mujin.logging.persistence.LogStorage;
import com.mujin.logging.persistence.NoOpLogStorage;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 操作日志自动装配（L2 阶段：默认注册 NoOpLogStorage + Aspect + 默认 collector 链）
 * <p>
 * collector 装配策略：
 * <ul>
 *   <li>内置四件套（SpEL / Param / Web / LoginUser）以 {@link OperationLogCollector} 类型注册为 Spring Bean</li>
 *   <li>{@code defaultLogContextCollector} 通过 {@link ObjectProvider#orderedStream()}
 *       一次性收集所有 {@code OperationLogCollector} Bean（含业务扩展），按 Spring
 *       {@link org.springframework.core.annotation.Order} 排序后再按 collector.order() 兜底排序</li>
 *   <li>业务可通过 {@code @ConditionalOnMissingBean} 替换任一内置 collector</li>
 * </ul>
 *
 * @author chenglin.wu
 * @date 2026/08/08
 */
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableConfigurationProperties(LoggingProperties.class)
@ConditionalOnProperty(prefix = "mujin.logging", name = "enabled", matchIfMissing = true)
public class LoggingAutoConfiguration {

    /**
     * 异步写入开关：开启时启用 {@link EnableAsync} 与 {@link LoggingExecutor}
     */
    @Configuration
    @ConditionalOnProperty(prefix = "mujin.logging", name = "async", matchIfMissing = true)
    @EnableAsync
    public static class AsyncConfig {

        /**
         * 操作日志专用异步线程池
         * <p>
         * 由 {@link OperationLogAspect} 通过 {@link ObjectProvider} 注入，
         * 队列满时由 {@code CallerRunsPolicy} 退化为同步执行，确保不丢日志。
         *
         * @param properties 配置
         * @return TaskExecutor logging 专用线程池
         */
        @Bean(name = "loggingTaskExecutor", destroyMethod = "shutdown")
        @ConditionalOnMissingBean(name = "loggingTaskExecutor")
        public TaskExecutor loggingTaskExecutor(LoggingProperties properties) {
            return new LoggingExecutor(properties);
        }
    }

    /**
     * 文件存储策略：仅在 {@code storage-type=FILE} 时装配
     *
     * @param properties 配置
     * @return LogStorage 文件存储实现
     */
    @Bean
    @ConditionalOnMissingBean(LogStorage.class)
    @ConditionalOnProperty(prefix = "mujin.logging", name = "storage-type", havingValue = "FILE")
    public LogStorage fileLogStorage(LoggingProperties properties) {
        return new FileLogStorage(properties);
    }

    /**
     * 默认 LogStorage 占位实现；DB 子模块或 FILE 装配时会被取代
     *
     * @return LogStorage
     */
    @Bean
    @ConditionalOnMissingBean(LogStorage.class)
    public LogStorage defaultLogStorage() {
        return new NoOpLogStorage();
    }

    /**
     * SpEL 采集器 Bean（暴露为 Spring Bean，业务可被同类型 Bean 替换）
     *
     * @return OperationLogCollector
     */
    @Bean
    @ConditionalOnMissingBean(SpelParamCollector.class)
    public OperationLogCollector spelParamCollector() {
        return new SpelParamCollector();
    }

    /**
     * 参数采集器 Bean：处理入参序列化与 {@code @LogMask} / {@code @LogIgnore}
     * <p>
     * 出参采集由 {@code OperationLogAspect} 在 {@code proceed()} 后通过
     * {@link ParamCollector#collectOutput} 方法显式触发。
     *
     * @return OperationLogCollector
     */
    @Bean
    @ConditionalOnMissingBean(ParamCollector.class)
    public OperationLogCollector paramCollector() {
        return new ParamCollector();
    }

    /**
     * Web 上下文采集器 Bean
     *
     * @param properties 操作日志配置
     * @return OperationLogCollector
     */
    @Bean
    @ConditionalOnMissingBean(WebContextCollector.class)
    public OperationLogCollector webContextCollector(LoggingProperties properties) {
        return new WebContextCollector(properties.isCaptureHeader());
    }

    /**
     * 登录人采集器 Bean
     *
     * @return OperationLogCollector
     */
    @Bean
    @ConditionalOnMissingBean(LoginUserCollector.class)
    public OperationLogCollector loginUserCollector() {
        return new LoginUserCollector();
    }

    /**
     * 默认上下文采集器（按 order 串联所有 collector）
     * <p>
     * 通过 {@link ObjectProvider#orderedStream()} 收集 Spring 容器中所有 {@link OperationLogCollector}
     * Bean（内置三件套 + 业务扩展），按 Spring {@code @Order} 排序后交给
     * {@link DefaultLogContextCollector}，后者按 collector.order() 二次排序。
     *
     * @param collectorProvider Spring 容器中所有 OperationLogCollector Bean
     * @return DefaultLogContextCollector
     */
    @Bean
    @ConditionalOnMissingBean(DefaultLogContextCollector.class)
    public DefaultLogContextCollector defaultLogContextCollector(
            ObjectProvider<OperationLogCollector> collectorProvider) {
        List<OperationLogCollector> collectors = collectorProvider.orderedStream()
                .collect(Collectors.toList());
        return new DefaultLogContextCollector(collectors);
    }

    /**
     * 注册操作日志 AOP
     *
     * @param logStorageProvider   日志存储提供者（默认 NoOpLogStorage 已 @ConditionalOnMissingBean 保证存在）
     * @param properties           操作日志配置
     * @param contextCollector     上下文采集器
     * @param paramCollector       参数采集器（用于出参采集）
     * @param taskExecutorProvider 异步执行器（async=true 时存在，否则为空）
     * @return OperationLogAspect
     */
    @Bean
    public OperationLogAspect operationLogAspect(ObjectProvider<LogStorage> logStorageProvider,
                                                LoggingProperties properties,
                                                DefaultLogContextCollector contextCollector,
                                                ParamCollector paramCollector,
                                                ObjectProvider<TaskExecutor> taskExecutorProvider) {
        return new OperationLogAspect(logStorageProvider, properties, contextCollector, paramCollector,
                taskExecutorProvider);
    }
}
