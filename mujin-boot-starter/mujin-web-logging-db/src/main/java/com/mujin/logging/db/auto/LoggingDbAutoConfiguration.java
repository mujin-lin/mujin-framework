package com.mujin.logging.db.auto;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.mujin.logging.configuration.LoggingProperties;
import com.mujin.logging.db.mapper.OperationLogMapper;
import com.mujin.logging.db.mapper.OperationLogParamMapper;
import com.mujin.logging.db.persistence.DbLogStorage;
import com.mujin.logging.db.schema.OperationLogSchemaInitializer;
import com.mujin.logging.persistence.LogStorage;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * 操作日志 DB 存储自动装配
 * <p>
 * 触发条件：
 * <ul>
 *     <li>{@code mujin.logging.enabled=true}（默认）</li>
 *     <li>{@code mujin.logging.storage-type=DB}</li>
 *     <li>{@link BaseMapper} 类存在（即业务引入了 MyBatis-Plus）</li>
 * </ul>
 * <p>
 * 数据源策略：
 * <ul>
 *     <li>{@code mujin.logging.db.datasource-bean-name} <b>为空</b>：复用业务默认 {@link SqlSessionFactory}
 *         与 Mapper（业务需通过 {@code @MapperScan(basePackages = "com.mujin.logging.db.mapper")} 显式扫描）</li>
 *     <li>{@code mujin.logging.db.datasource-bean-name} <b>非空</b>：从 Spring 容器按名获取
 *         {@link DataSource}，本模块自动构建独立 {@link SqlSessionFactory} 与 Mapper 实例，
 *         业务侧无需任何额外配置</li>
 * </ul>
 *
 * @author chenglin.wu
 * @date 2026/08/09
 */
@Configuration
@AutoConfigureAfter(name = "com.mujin.logging.auto.LoggingAutoConfiguration")
@EnableConfigurationProperties(LoggingProperties.class)
@ConditionalOnClass(BaseMapper.class)
@ConditionalOnProperty(prefix = "mujin.logging", name = "storage-type", havingValue = "DB")
public class LoggingDbAutoConfiguration {

    /**
     * 数据库存储 Bean
     *
     * @param logMapper    主表 Mapper
     * @param paramMapper  参数表 Mapper
     * @return DbLogStorage
     */
    @Bean
    @ConditionalOnMissingBean(LogStorage.class)
    @ConditionalOnBean({OperationLogMapper.class, OperationLogParamMapper.class})
    public DbLogStorage dbLogStorage(OperationLogMapper logMapper,
                                     OperationLogParamMapper paramMapper) {
        return new DbLogStorage(logMapper, paramMapper);
    }

    /**
     * 自动建表 Runner：当 {@code mujin.logging.db.auto-create-table=true}（默认）时启用
     *
     * @param properties         操作日志配置
     * @param applicationContext Spring 容器
     * @return ApplicationRunner
     */
    @Bean
    @ConditionalOnMissingBean(ApplicationRunner.class)
    @ConditionalOnProperty(prefix = "mujin.logging.db", name = "auto-create-table", matchIfMissing = true)
    public ApplicationRunner operationLogSchemaInitializer(LoggingProperties properties,
                                                            ApplicationContext applicationContext) {
        return new OperationLogSchemaInitializer(properties, applicationContext);
    }

    /**
     * 独立数据源装配：当 {@code mujin.logging.db.datasource-bean-name} 非空时自动激活
     * <p>
     * 仅在以下场景创建：
     * <ul>
     *     <li>配置了独立数据源 Bean 名称</li>
     *     <li>业务 {@link SqlSessionFactory} 中尚未包含日志 Mapper（即未走业务默认 Mapper 扫描）</li>
     * </ul>
     */
    @Configuration
    @ConditionalOnProperty(prefix = "mujin.logging.db", name = "datasource-bean-name")
    public static class IndependentDataSourceConfig {

        /**
         * 独立 SqlSessionFactory Bean，基于指定的 DataSource
         *
         * @param properties       操作日志配置（读取 datasource-bean-name）
         * @param applicationContext Spring 容器
         * @param defaultFactory   业务默认 SqlSessionFactory（用于参考配置）
         * @return SqlSessionFactory 独立 SqlSessionFactory
         */
        @Bean(name = "operationLogSqlSessionFactory")
        @ConditionalOnMissingBean(name = "operationLogSqlSessionFactory")
        public SqlSessionFactory operationLogSqlSessionFactory(LoggingProperties properties,
                                                                ApplicationContext applicationContext,
                                                                ObjectProvider<SqlSessionFactory> defaultFactory) {
            String dataSourceName = properties.getDb().getDatasourceBeanName();
            DataSource dataSource = applicationContext.getBean(dataSourceName, DataSource.class);

            MybatisSqlSessionFactoryBean factoryBean = new MybatisSqlSessionFactoryBean();
            factoryBean.setDataSource(dataSource);
            // 复用业务 MybatisConfiguration（如有），否则使用默认
            SqlSessionFactory defaultSessionFactory = defaultFactory.getIfAvailable();
            if (defaultSessionFactory != null) {
                MybatisConfiguration configuration = (MybatisConfiguration) defaultSessionFactory.getConfiguration();
                factoryBean.setConfiguration(configuration);
            }
            try {
                return factoryBean.getObject();
            } catch (Exception e) {
                throw new IllegalStateException("创建 operationLogSqlSessionFactory 失败：" + e.getMessage(), e);
            }
        }

        /**
         * 独立事务管理器，与独立 SqlSessionFactory 配套
         *
         * @param properties       操作日志配置
         * @param applicationContext Spring 容器
         * @return PlatformTransactionManager
         */
        @Bean(name = "operationLogTransactionManager")
        @ConditionalOnMissingBean(name = "operationLogTransactionManager")
        public PlatformTransactionManager operationLogTransactionManager(LoggingProperties properties,
                                                                        ApplicationContext applicationContext) {
            String dataSourceName = properties.getDb().getDatasourceBeanName();
            DataSource dataSource = applicationContext.getBean(dataSourceName, DataSource.class);
            return new DataSourceTransactionManager(dataSource);
        }

        /**
         * 独立 OperationLogMapper Bean
         *
         * @param sqlSessionFactory 独立 SqlSessionFactory
         * @return OperationLogMapper
         */
        @Bean
        @ConditionalOnMissingBean
        public OperationLogMapper operationLogMapper(
                @org.springframework.beans.factory.annotation.Qualifier("operationLogSqlSessionFactory")
                SqlSessionFactory sqlSessionFactory) {
            return new IndependentMapperFactoryBean<>(OperationLogMapper.class, sqlSessionFactory).create();
        }

        /**
         * 独立 OperationLogParamMapper Bean
         *
         * @param sqlSessionFactory 独立 SqlSessionFactory
         * @return OperationLogParamMapper
         */
        @Bean
        @ConditionalOnMissingBean
        public OperationLogParamMapper operationLogParamMapper(
                @org.springframework.beans.factory.annotation.Qualifier("operationLogSqlSessionFactory")
                SqlSessionFactory sqlSessionFactory) {
            return new IndependentMapperFactoryBean<>(OperationLogParamMapper.class, sqlSessionFactory).create();
        }
    }

    /**
     * 通用 Mapper 工厂：基于 SqlSessionFactory 创建 BaseMapper 代理
     *
     * @param <T> Mapper 类型
     */
    private static class IndependentMapperFactoryBean<T> {

        private final Class<T> mapperInterface;
        private final SqlSessionFactory sqlSessionFactory;

        IndependentMapperFactoryBean(Class<T> mapperInterface, SqlSessionFactory sqlSessionFactory) {
            this.mapperInterface = mapperInterface;
            this.sqlSessionFactory = sqlSessionFactory;
        }

        /**
         * 通过 SqlSessionFactory.openSession().getMapper() 创建 Mapper 代理
         *
         * @return T Mapper 实例
         */
        T create() {
            try (org.apache.ibatis.session.SqlSession session = sqlSessionFactory.openSession()) {
                return session.getMapper(mapperInterface);
            }
        }
    }
}
