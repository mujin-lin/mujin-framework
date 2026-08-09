package com.mujin.logging.collector;

import com.mujin.logging.annotations.OperationLog;
import com.mujin.logging.model.OperationLogContext;
import lombok.Getter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 默认上下文采集器：按 {@link OperationLogCollector#order()} 串联执行已注册的 collector
 * <p>
 * 任一 collector 抛异常被吞掉，不影响其它 collector 与主流程。
 * 框架自身默认注入 {@link SpelParamCollector}、{@link WebContextCollector}、{@link LoginUserCollector}，
 * 业务侧可通过 Spring 自动注入额外的 {@link OperationLogCollector} Bean 实现扩展，
 * 由 {@code LoggingAutoConfiguration} 统一收集并按 {@code order()} 排序。
 *
 * @author chenglin.wu
 * @date 2026/08/08
 */
@Getter
public class DefaultLogContextCollector implements OperationLogCollector {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultLogContextCollector.class);

    /**
     * 已按 order 升序排列的 collector 列表
     */
    private final List<OperationLogCollector> collectors;

    public DefaultLogContextCollector(List<OperationLogCollector> collectors) {
        List<OperationLogCollector> copy = new ArrayList<>(collectors);
        copy.sort(Comparator.comparingInt(OperationLogCollector::order));
        this.collectors = Collections.unmodifiableList(copy);
    }

    @Override
    public int order() {
        return Integer.MIN_VALUE;
    }

    @Override
    public void collect(OperationLogContext context, ProceedingJoinPoint joinPoint, OperationLog annotation) {
        for (OperationLogCollector collector : collectors) {
            try {
                collector.collect(context, joinPoint, annotation);
            } catch (Exception e) {
                LOG.warn("[OPERATION-LOG] collector={} 执行失败：{}",
                        collector.getClass().getSimpleName(), e.getMessage());
            }
        }
    }
}
