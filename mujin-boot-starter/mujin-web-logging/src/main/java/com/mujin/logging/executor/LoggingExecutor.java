package com.mujin.logging.executor;

import com.mujin.logging.configuration.LoggingProperties;
import lombok.NonNull;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 操作日志异步写入线程池
 * <p>
 * 基于 Spring {@link ThreadPoolTaskExecutor} 构建，参数遵循设计文档：
 * <ul>
 *     <li>corePoolSize = {@code mujin.logging.thread-pool-size}（默认 4）</li>
 *     <li>maxPoolSize = core * 2（默认 8）</li>
 *     <li>queueCapacity = {@code mujin.logging.queue-capacity}（默认 1024）</li>
 *     <li>rejectedExecutionHandler = {@link ThreadPoolExecutor.CallerRunsPolicy}：
 *         队列满时由调用线程同步执行，保证不丢日志</li>
 * </ul>
 * 通过 {@link MdcTaskDecorator} 透传 SLF4J {@link MDC}，确保子线程可读取父线程的 traceId。
 * <p>
 * 由 {@code LoggingAutoConfiguration} 装配为名为 {@code loggingTaskExecutor} 的 Bean。
 *
 * @author chenglin.wu
 * @date 2026/08/09
 */
public class LoggingExecutor extends ThreadPoolTaskExecutor {

    private static final long serialVersionUID = 1L;

    /**
     * 构造一个根据 {@link LoggingProperties} 初始化的线程池
     *
     * @param properties 操作日志配置
     */
    public LoggingExecutor(LoggingProperties properties) {
        int core = Math.max(1, properties.getThreadPoolSize());
        setCorePoolSize(core);
        setMaxPoolSize(core * 2);
        setQueueCapacity(Math.max(1, properties.getQueueCapacity()));
        setKeepAliveSeconds(60);
        setThreadNamePrefix("logging-");
        // 拒绝策略：队列满时由调用线程同步执行，避免丢日志
        RejectedExecutionHandler rejectedHandler = new ThreadPoolExecutor.CallerRunsPolicy();
        setRejectedExecutionHandler(rejectedHandler);
        // 透传 MDC（如 traceId），保证异步线程可读到父线程上下文
        setTaskDecorator(new MdcTaskDecorator());
        // 允许核心线程超时回收
        setAllowCoreThreadTimeOut(true);
        // 关闭时等待任务完成，避免日志丢失
        setWaitForTasksToCompleteOnShutdown(true);
        setAwaitTerminationSeconds(10);
    }

    /**
     * MDC 透传装饰器：将父线程的 {@link MDC} 上下文快照传递给子线程
     *
     * @author chenglin.wu
     * @date 2026/08/09
     */
    private static class MdcTaskDecorator implements TaskDecorator {

        @Override
        public Runnable decorate(@NonNull Runnable runnable) {
            // 捕获父线程 MDC 快照（traceId / 自定义 key）
            Map<String, String> context = MDC.getCopyOfContextMap();
            return () -> {
                Map<String, String> previous = MDC.getCopyOfContextMap();
                if (context == null) {
                    MDC.clear();
                } else {
                    MDC.setContextMap(context);
                }
                try {
                    runnable.run();
                } finally {
                    // 恢复线程原本的 MDC，避免污染线程池复用的下一个任务
                    if (previous == null) {
                        MDC.clear();
                    } else {
                        MDC.setContextMap(previous);
                    }
                }
            };
        }
    }
}
