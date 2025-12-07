package com.mujin.security.annotations;

import com.mujin.security.MujinSecurityAutoConfiguration;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

/**
 * validator 选择器
 *
 * @author chenglin.wu
 * @date 2025/12/7
 */
public class ValidatorConfigurationSelector implements ImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[]{MujinSecurityAutoConfiguration.class.getName()};
    }
}
