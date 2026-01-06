package com.mujin.orm.conditional;

import cn.hutool.core.util.BooleanUtil;
import com.mujin.orm.constants.OrmConfigurationConstants;
import lombok.NonNull;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 自动注入扫描的 conditional 判断条件
 *
 * @author chenglin.wu
 * @date 2025/12/29
 */
public class AutoFillCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {
        Environment env = context.getEnvironment();
        String enableStr = env.getProperty(OrmConfigurationConstants.MJ_ORM_ENABLE_AUTO_FILL_KEY, "true");
        return BooleanUtil.toBoolean(enableStr);
    }
}
