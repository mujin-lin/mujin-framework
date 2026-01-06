package com.mujin.orm;

import com.mujin.orm.annotations.EnableAutoFill;
import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.core.Ordered;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;

/**
 * 自动注入的扫描包配置
 *
 * @author chenglin.wu
 * @date 2025/12/28
 */
public class AutoFillComponentSelector implements DeferredImportSelector, Ordered {

    // 核心配置类（真正处理扫描和注册的逻辑类）
    private static final String AUTO_FILL_CONFIG_CLASS = "com.mujin.orm.AutoFillRegister";

    @Override
    @NonNull
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        // 验证@EnableAutoFill注解是否存在（避免空操作）
        if (!importingClassMetadata.hasAnnotation(EnableAutoFill.class.getName())) {
            return new String[0];
        }
        // 返回核心配置类名：延迟导入该配置类（这是DeferredImportSelector的核心价值）
        return new String[]{AUTO_FILL_CONFIG_CLASS};
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
