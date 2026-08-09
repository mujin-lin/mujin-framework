package com.mujin.logging.aop;

import com.mujin.logging.annotations.OperationLog;
import com.mujin.logging.collector.DefaultLogContextCollector;
import com.mujin.logging.collector.ParamCollector;
import com.mujin.logging.configuration.LoggingProperties;
import com.mujin.logging.enums.LogResultEnum;
import com.mujin.logging.model.OperationLogContext;
import com.mujin.logging.persistence.LogStorage;
import com.mujin.logging.persistence.NoOpLogStorage;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.RejectedExecutionException;

/**
 * 操作日志 AOP 织入（L2 阶段：通过 collector 链填充 Web/登录人/SpEL/入参上下文，
 * 通过 Aspect 在 proceed() 后显式采集出参）
 *
 * @author chenglin.wu
 * @date 2026/08/08
 */
@Aspect
public class OperationLogAspect {

    private static final Logger LOG = LoggerFactory.getLogger(OperationLogAspect.class);

    /**
     * 通过 ObjectProvider 注入 LogStorage，未配置时回退 NoOpLogStorage
     */
    private final ObjectProvider<LogStorage> logStorageProvider;

    /**
     * 操作日志配置（用于读取全局 slowThreshold 等）
     */
    private final LoggingProperties properties;

    /**
     * 上下文采集器（串联多个 collector）
     */
    private final DefaultLogContextCollector contextCollector;

    /**
     * 参数采集器（入参随 collector 链收集，出参由 Aspect 在 proceed() 后显式收集）
     */
    private final ParamCollector paramCollector;

    /**
     * 异步执行器（可选）：由 LoggingAutoConfiguration 在 async=true 时注册
     */
    private final ObjectProvider<TaskExecutor> taskExecutorProvider;

    public OperationLogAspect(ObjectProvider<LogStorage> logStorageProvider,
                              LoggingProperties properties,
                              DefaultLogContextCollector contextCollector,
                              ParamCollector paramCollector,
                              ObjectProvider<TaskExecutor> taskExecutorProvider) {
        this.logStorageProvider = logStorageProvider;
        this.properties = properties;
        this.contextCollector = contextCollector;
        this.paramCollector = paramCollector;
        this.taskExecutorProvider = taskExecutorProvider;
    }

    /**
     * 拦截 @OperationLog 标注的方法
     *
     * @param joinPoint  连接点
     * @param annotation 注解
     * @return Object 方法返回值
     * @throws Throwable 方法异常
     */
    @Around("@annotation(annotation)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog annotation) throws Throwable {
        long start = System.currentTimeMillis();
        OperationLogContext context = buildContext(joinPoint, annotation);
        // L2：在方法执行前完成所有上下文采集
        contextCollector.collect(context, joinPoint, annotation);

        Object result = null;
        try {
            result = joinPoint.proceed();
            context.setResult(LogResultEnum.SUCCESS.getCode());
            // 出参在 proceed() 成功返回后采集，避开 Before 阶段无法获取返回值的问题
            paramCollector.collectOutput(context, result, annotation);
            return result;
        } catch (Throwable ex) {
            context.setResult(LogResultEnum.FAIL.getCode());
            context.setErrorMessage(ex.getClass().getSimpleName() + ": " + ex.getMessage());
            throw ex;
        } finally {
            long cost = System.currentTimeMillis() - start;
            context.setCostMs(cost);
            // 全局阈值与注解阈值取较小者，保证全局能覆盖注解
            long threshold = Math.min(annotation.slowThreshold(), properties.getSlowThreshold());
            context.setSlow(cost >= threshold);
            persist(context);
        }
    }

    /**
     * 构建基础上下文（注解上的 method/module/description）
     *
     * @param joinPoint  连接点
     * @param annotation 注解
     * @return OperationLogContext
     */
    private OperationLogContext buildContext(ProceedingJoinPoint joinPoint, OperationLog annotation) {
        OperationLogContext context = new OperationLogContext();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        context.setModule(signature.getDeclaringType().getSimpleName());
        context.setMethod(signature.getName());
        context.setDescription(annotation.value());
        return context;
    }

    /**
     * 持久化（L3 阶段：异步 + 同步降级）
     * <p>
     * 异步路径：{@link TaskExecutor#execute(Runnable)}；队列满时退化为同步执行，保证不丢日志。
     * 同步路径：直接由当前线程写入。
     * 任何存储异常均被吞掉，仅打印 warn，不影响业务主流程。
     *
     * @param context 上下文
     */
    private void persist(OperationLogContext context) {
        LogStorage storage = logStorageProvider.getIfAvailable(NoOpLogStorage::new);
        Runnable task = () -> safeSave(storage, context);

        if (!properties.isAsync()) {
            // 关闭异步时直接同步写
            task.run();
            return;
        }

        TaskExecutor executor = taskExecutorProvider.getIfAvailable();
        if (executor == null) {
            // 未配置 executor（异常场景），同步写
            task.run();
            return;
        }

        try {
            executor.execute(task);
        } catch (RejectedExecutionException ex) {
            // 队列满、shutdown 中等场景：CallerRunsPolicy 内部已可能处理，
            // 此处再兜底一次同步执行，确保不丢日志
            LOG.warn("[OPERATION-LOG] 异步队列拒绝，降级为同步写入：{}", ex.getMessage());
            task.run();
        }
    }

    /**
     * 实际存储调用，包一层 try-catch 防止任何异常拖垮业务
     *
     * @param storage 存储策略
     * @param context 上下文
     */
    private void safeSave(LogStorage storage, OperationLogContext context) {
        try {
            storage.save(context);
        } catch (Exception e) {
            LOG.warn("[OPERATION-LOG] 持久化失败：{}", e.getMessage(), e);
        }
    }
}
