package com.mujin.logging.collector;

import com.mujin.logging.annotations.OperationLog;
import com.mujin.logging.model.OperationLogContext;
import com.mujin.logging.model.OperationLogParam;
import com.mujin.logging.serializer.ParamJsonSerializer;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 操作日志核心参数采集器
 * <p>
 * 负责将方法入参与出参序列化为 JSON 字符串，写入 {@link OperationLogContext}。
 * 序列化过程复用 {@link ParamJsonSerializer}，自动应用 {@code @LogMask} 脱敏与
 * {@code @LogIgnore} 忽略规则，并过滤掉 Servlet API、文件上传等不应进入日志的参数类型。
 * <p>
 * 入参采集通过实现 {@link OperationLogCollector} 接口纳入统一采集链；
 * 出参采集通过独立的 {@link #collectOutput(OperationLogContext, Object, OperationLog)}
 * 方法，由 {@code OperationLogAspect} 在 {@code proceed()} 成功返回后显式调用，
 * 避免出参在 Before 阶段无法获取的问题。
 *
 * @author chenglin.wu
 * @date 2026/08/09
 */
public class ParamCollector implements OperationLogCollector {

    /**
     * 参数名解析器，需要编译期开启 {@code -parameters} 才能拿到真实参数名
     */
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    @Override
    public int order() {
        // 与 WebContextCollector 同序，依靠 DefaultLogContextCollector 的稳定排序保证执行顺序
        return 0;
    }

    @Override
    public void collect(OperationLogContext context, ProceedingJoinPoint joinPoint, OperationLog annotation) {
        if (!annotation.saveParam()) {
            return;
        }
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return;
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);

        List<OperationLogParam> params = new ArrayList<>(args.length);
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (shouldSkip(arg)) {
                continue;
            }
            String paramName = (parameterNames != null && parameterNames.length > i) ? parameterNames[i] : "arg" + i;
            String paramValue = ParamJsonSerializer.toJson(arg);
            params.add(OperationLogParam.ofIn(i, paramName, paramValue));
        }
        context.setParams(params);
    }

    /**
     * 采集方法出参
     * <p>
     * 由 {@code OperationLogAspect} 在 {@code proceed()} 成功返回后调用。
     * 仅在 {@link OperationLog#saveResult()} 为 {@code true} 且返回值非空时执行。
     *
     * @param context    操作日志上下文
     * @param result     方法返回值
     * @param annotation 方法上的 {@code @OperationLog}
     */
    public void collectOutput(OperationLogContext context, Object result, OperationLog annotation) {
        if (!annotation.saveResult() || result == null) {
            return;
        }
        if (shouldSkip(result)) {
            return;
        }
        String paramValue = ParamJsonSerializer.toJson(result);
        context.setResultParam(OperationLogParam.ofOut("result", paramValue));
    }

    /**
     * 判断参数是否应跳过序列化（Servlet API、文件上传、Spring 内部模型等）
     *
     * @param arg 入参或返回值
     * @return boolean true 表示应跳过
     */
    private boolean shouldSkip(Object arg) {
        if (arg == null) {
            return true;
        }
        if (arg instanceof ServletRequest || arg instanceof ServletResponse) {
            return true;
        }
        if (arg instanceof MultipartFile) {
            return true;
        }
        String className = arg.getClass().getName();
        // 反射比对 Spring 内部模型，避免对 spring-web 的强依赖
        return "org.springframework.validation.BindingResult".equals(className)
                || "org.springframework.ui.Model".equals(className)
                || "org.springframework.ui.ModelMap".equals(className);
    }
}
