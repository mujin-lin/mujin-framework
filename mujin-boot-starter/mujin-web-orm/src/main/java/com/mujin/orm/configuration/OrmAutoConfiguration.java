package com.mujin.orm.configuration;


import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.mujin.orm.handler.InsertFillColumnHandler;
import com.mujin.orm.handler.UpdateFillColumnHandler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * ORM 自动配置类
 * <p>
 * 仅在 {@code mujin.orm.enabled=true} 且已注册 {@link MjOrmConfig} 时生效。
 *
 * @author chenglin.wu
 * @date 2025/12/27
 */
@Configuration
@AutoConfigureAfter(MjOrmConfig.class)
@ConditionalOnProperty(prefix = "mujin.orm", name = "enabled", matchIfMissing = false)
public class OrmAutoConfiguration {

    /**
     * @param config the config
     * @return MybatisPlusInterceptor
     * @date 2025/12/27
     */
    @Bean
    @ConditionalOnBean(MjOrmConfig.class)
    public MybatisPlusInterceptor mybatisPlusInterceptor(MjOrmConfig config) {
        return MybatisPlusInterceptorBuilder
                .create()
                .openPage(config.isOpenPageInterceptor())
                .openOptimisticLocker(config.isOptimisticLocker())
                .openBlockAttackInner(config.isBlockAttackInner())
                .build();
    }

    /**
     * 注入自动填充数据
     *
     * @return MybatisPlusMetaHandler
     * @author chenglin.wu
     * @date 2025/12/27
     */
    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusMetaHandler mybatisPlusMetaHandler(ObjectProvider<InsertFillColumnHandler> insertFillColumnHandlers, ObjectProvider<UpdateFillColumnHandler> updateFillColumnHandlers) {

        return new MybatisPlusMetaHandler(
                insertFillColumnHandlers
                        .stream()
                        .filter(Objects::nonNull)
                        .filter(item -> Objects.nonNull(item.insertFill()))
                        .sorted(Comparator.comparing(InsertFillColumnHandler::getInsertFillOrder))
                        .map(InsertFillColumnHandler::insertFill)
                        .collect(Collectors.toList()),
                updateFillColumnHandlers
                        .stream()
                        .filter(Objects::nonNull)
                        .filter(item -> Objects.nonNull(item.updateFill()))
                        .sorted(Comparator.comparing(UpdateFillColumnHandler::getUpdateFillOrder))
                        .map(UpdateFillColumnHandler::updateFill)
                        .collect(Collectors.toList())
        );
    }

    /**
     * 插件管理
     *
     * @author chenglin.wu
     * @date 2025/12/27
     */
    static class MybatisPlusInterceptorBuilder {
        private final MybatisPlusInterceptor interceptor;
        private boolean openPage;

        private MybatisPlusInterceptorBuilder(MybatisPlusInterceptor interceptor) {
            this.interceptor = interceptor;
        }

        protected static MybatisPlusInterceptorBuilder create() {
            return new MybatisPlusInterceptorBuilder(new MybatisPlusInterceptor());
        }

        protected MybatisPlusInterceptorBuilder openPage(boolean openPage) {
            this.openPage = openPage;
            return this;
        }

        protected MybatisPlusInterceptorBuilder openOptimisticLocker(boolean openOptimisticLocker) {
            if (openOptimisticLocker) {
                this.interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
            }
            return this;
        }

        protected MybatisPlusInterceptorBuilder openBlockAttackInner(boolean blockAttackInner) {
            if (blockAttackInner) {
                this.interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
            }
            return this;
        }


        protected MybatisPlusInterceptor build() {
            if (this.openPage) {
                PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
                this.interceptor.addInnerInterceptor(paginationInnerInterceptor);
            }
            return this.interceptor;
        }

    }
}
