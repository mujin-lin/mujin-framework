package com.mujin.logging.collector;

import com.mujin.logging.annotations.OperationLog;
import com.mujin.logging.model.OperationLogContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.reflect.Method;

/**
 * SpEL 参数采集器：解析 {@code @OperationLog.bizId()} 与 {@code @OperationLog.operator()}
 * <p>
 * 解析失败时降级为空串，不抛异常污染主流程。
 * 非 {@code #} 开头的字符串会被 SpEL 当字面量解析（如 {@code "foo"} → {@code "foo"}），
 * 业务可直接写常量 ID（如 {@code "system"}）而无需用 {@code #{'system'}} 包裹。
 *
 * @author chenglin.wu
 * @date 2026/08/08
 */
public class SpelParamCollector implements OperationLogCollector {

    private static final Logger LOG = LoggerFactory.getLogger(SpelParamCollector.class);

    /**
     * SpEL 表达式解析器（线程安全，可作为单例）
     */
    private final SpelExpressionParser parser = new SpelExpressionParser();

    /**
     * 参数名解析器（需编译期保留 -parameters 才能拿到真实参数名）
     */
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    @Override
    public int order() {
        // SpEL 依赖方法签名参数，先于其它上下文采集
        return -100;
    }

    @Override
    public void collect(OperationLogContext context, ProceedingJoinPoint joinPoint, OperationLog annotation) {
        String bizId = parse(joinPoint, annotation.bizId());
        if (bizId != null && !bizId.isEmpty()) {
            context.setBizId(bizId);
        }

        String operator = parse(joinPoint, annotation.operator());
        if (operator != null && !operator.isEmpty()) {
            context.setOperator(operator);
        }
    }

    /**
     * 解析 SpEL 表达式（含字面量）
     *
     * @param joinPoint  切入点
     * @param expression SpEL 表达式或字面量
     * @return String 解析结果；空表达式返回空串；解析失败返回空串
     */
    private String parse(ProceedingJoinPoint joinPoint, String expression) {
        if (expression == null || expression.isEmpty()) {
            return "";
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();
        Object target = joinPoint.getTarget();
        // rootObject 由 MethodBasedEvaluationContext 在构造时绑定为 target，
        // 表达式里可用 #root / 直接引用 Bean 属性访问
        MethodBasedEvaluationContext evalContext = new MethodBasedEvaluationContext(
                target, method, args, parameterNameDiscoverer);

        try {
            Expression exp = parser.parseExpression(expression);
            Object value = exp.getValue(evalContext);
            return value == null ? "" : value.toString();
        } catch (EvaluationException e) {
            // 包括 SpelParseException / SpelEvaluationException，统一兜底
            LOG.warn("[OPERATION-LOG] SpEL 解析失败：expr={}, err={}", expression, e.getMessage());
            return "";
        } catch (Exception e) {
            LOG.warn("[OPERATION-LOG] SpEL 执行异常：expr={}, err={}", expression, e.getMessage());
            return "";
        }
    }
}
