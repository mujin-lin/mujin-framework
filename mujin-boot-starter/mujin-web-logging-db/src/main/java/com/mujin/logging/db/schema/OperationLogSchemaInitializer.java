package com.mujin.logging.db.schema;

import com.mujin.logging.configuration.LoggingProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 操作日志表结构初始化器
 * <p>
 * 在 Spring Boot 启动完成后（{@link ApplicationRunner#run}）执行：
 * <ol>
 *     <li>通过 {@link DatabaseMetaData#getTables} 检查两张表是否存在</li>
 *     <li>任一表不存在则执行 {@code CREATE TABLE IF NOT EXISTS}（幂等）</li>
 *     <li>异常仅打印 warn，不阻塞启动</li>
 * </ol>
 * <p>
 * 数据源选择策略：
 * <ul>
 *     <li>配置了 {@code mujin.logging.db.datasource-bean-name}：使用指定 DataSource</li>
 *     <li>未配置：从 Spring 容器获取默认 DataSource</li>
 * </ul>
 *
 * @author chenglin.wu
 * @date 2026/08/09
 */
public class OperationLogSchemaInitializer implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(OperationLogSchemaInitializer.class);

    private final LoggingProperties properties;
    private final ApplicationContext applicationContext;

    public OperationLogSchemaInitializer(LoggingProperties properties, ApplicationContext applicationContext) {
        this.properties = properties;
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.getDb().isAutoCreateTable()) {
            return;
        }
        DataSource dataSource = resolveDataSource();
        if (dataSource == null) {
            LOG.warn("[OPERATION-LOG] 自动建表跳过：未找到 DataSource Bean");
            return;
        }

        String tablePrefix = properties.getDb().getTablePrefix();
        try (Connection connection = dataSource.getConnection()) {
            ensureTable(connection, OperationLogDdlProvider.OPERATION_LOG_DDL, tablePrefix);
            ensureTable(connection, OperationLogDdlProvider.OPERATION_LOG_PARAM_DDL, tablePrefix);
        } catch (SQLException e) {
            // 启动失败容忍：仅 warn，不抛
            LOG.warn("[OPERATION-LOG] 自动建表失败：{}", e.getMessage(), e);
        }
    }

    /**
     * 解析 DataSource：优先按配置名取独立数据源，否则取默认
     *
     * @return DataSource 解析到的数据源；解析不到返回 null
     */
    private DataSource resolveDataSource() {
        String name = properties.getDb().getDatasourceBeanName();
        if (name != null && !name.isEmpty()) {
            try {
                return applicationContext.getBean(name, DataSource.class);
            } catch (Exception e) {
                LOG.warn("[OPERATION-LOG] 指定数据源 [{}] 解析失败，使用默认数据源：{}", name, e.getMessage());
            }
        }
        try {
            return applicationContext.getBean(DataSource.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 检查并创建表
     *
     * @param connection 数据库连接
     * @param ddl        含 {@code {prefix}} 占位的 DDL
     * @param tablePrefix 表前缀
     * @throws SQLException SQL 执行异常
     */
    private void ensureTable(Connection connection, String ddl, String tablePrefix) throws SQLException {
        String sql = OperationLogDdlProvider.resolve(ddl, tablePrefix);
        // 提取表名（DDL 第一段 `CREATE TABLE IF NOT EXISTS xxx`）
        String tableName = extractTableName(sql);
        if (existsTable(connection, tableName)) {
            return;
        }
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
            LOG.info("[OPERATION-LOG] 自动建表成功：{}", tableName);
        }
    }

    /**
     * 通过 {@link DatabaseMetaData#getTables} 检查表是否存在
     *
     * @param connection 数据库连接
     * @param tableName  表名（不含前缀处理）
     * @return boolean true 表示已存在
     * @throws SQLException 元数据查询异常
     */
    private boolean existsTable(Connection connection, String tableName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet rs = metaData.getTables(connection.getCatalog(), null, tableName, new String[]{"TABLE"})) {
            return rs.next();
        }
    }

    /**
     * 从 {@code CREATE TABLE IF NOT EXISTS xxx (...)} 中提取表名
     *
     * @param ddl DDL 语句
     * @return String 表名
     */
    private String extractTableName(String ddl) {
        int idx = ddl.indexOf("IF NOT EXISTS");
        if (idx < 0) {
            idx = ddl.indexOf("TABLE");
        }
        String tail = ddl.substring(idx).trim();
        String[] parts = tail.split("\\s+");
        return parts[parts.length - 1].replace("(", "").trim();
    }
}
