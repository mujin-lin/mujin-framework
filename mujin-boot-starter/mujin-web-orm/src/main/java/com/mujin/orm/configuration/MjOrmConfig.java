package com.mujin.orm.configuration;


import com.mujin.orm.constants.OrmConfigurationConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;


/**
 * 配置类
 *
 * @author chenglin.wu
 * @date 2025/12/27
 */
@Data
@ConfigurationProperties(OrmConfigurationConstants.MJ_ORM_CONFIG_KEY)
public class MjOrmConfig {
    /**
     * 是否开启乐观锁插件
     */
    private boolean optimisticLocker;
    /**
     * 是否开启防全表更新与删除插件
     */
    private boolean blockAttackInner;
    /**
     * 开启分页插件拦截
     */
    private boolean openPageInterceptor = true;

    /**
     * 开启分页插件拦截
     */
    private boolean enableAutoFill = true;
}
