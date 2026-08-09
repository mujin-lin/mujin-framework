package com.mujin.logging.collector;

import com.mujin.logging.annotations.OperationLog;
import com.mujin.logging.context.LoginContextHolder;
import com.mujin.logging.model.OperationLogContext;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * 登录人采集器：从 {@link LoginContextHolder} 读取当前线程登录人
 * <p>
 * 优先级：
 * <ol>
 *   <li>若 {@code @OperationLog.operator()} 非空（SpEL 已解析）则跳过，覆盖语义优先</li>
 *   <li>否则读取 ThreadLocal 中的登录人姓名</li>
 *   <li>最后兜底为登录人 ID</li>
 * </ol>
 *
 * @author chenglin.wu
 * @date 2026/08/08
 */
public class LoginUserCollector implements OperationLogCollector {

    @Override
    public int order() {
        // 登录人放在 SpEL 之后，避免与显式 operator() 冲突
        return 100;
    }

    @Override
    public void collect(OperationLogContext context, ProceedingJoinPoint joinPoint, OperationLog annotation) {
        // SpEL 已写入则不覆盖
        if (context.getOperator() != null && !context.getOperator().isEmpty()) {
            return;
        }
        String loginName = LoginContextHolder.getLoginName();
        if (loginName != null && !loginName.isEmpty()) {
            context.setOperator(loginName);
            return;
        }
        context.setOperator(LoginContextHolder.getLoginId());
    }
}
