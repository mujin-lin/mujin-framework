package com.mujin.logging.collector;

import com.mujin.logging.annotations.OperationLog;
import com.mujin.logging.model.OperationLogContext;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * 操作日志上下文采集器接口（L2 阶段）
 * <p>
 * 每个实现负责采集某一类上下文信息（Web、登录人、SpEL…）。
 * 多个 collector 由 {@code DefaultLogContextCollector} 串联执行，
 * 任一 collector 抛异常不应影响其它 collector 与主流程。
 *
 * @author chenglin.wu
 * @date 2026/08/08
 */
public interface OperationLogCollector {

    /**
     * 采集顺序：值越小越先执行
     *
     * @return int 默认 0
     */
    default int order() {
        return 0;
    }

    /**
     * 执行采集，写入 context
     *
     * @param context    操作日志上下文
     * @param joinPoint  切入点
     * @param annotation 方法上的 @OperationLog
     */
    void collect(OperationLogContext context, ProceedingJoinPoint joinPoint, OperationLog annotation);
}
