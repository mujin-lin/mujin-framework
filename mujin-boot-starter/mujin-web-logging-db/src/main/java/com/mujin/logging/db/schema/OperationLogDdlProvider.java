package com.mujin.logging.db.schema;

/**
 * 操作日志两表 DDL 模板（MySQL 8.0+）
 * <p>
 * 表结构遵循 {@code docs/logging-design.md} 第 6 节；DDL 使用 {@code CREATE TABLE IF NOT EXISTS} 幂等创建。
 * 表名由 {@code ${table-prefix}operation_log} / {@code ${table-prefix}operation_log_param} 拼接。
 *
 * @author chenglin.wu
 * @date 2026/08/09
 */
public final class OperationLogDdlProvider {

    /**
     * 主表 DDL 模板，使用 {@code {prefix}} 占位，运行期替换为实际前缀
     */
    public static final String OPERATION_LOG_TABLE = "{prefix}operation_log";

    /**
     * 参数表 DDL 模板，使用 {@code {prefix}} 占位，运行期替换为实际前缀
     */
    public static final String OPERATION_LOG_PARAM_TABLE = "{prefix}operation_log_param";

    /**
     * 主表 DDL（占位未替换）
     */
    public static final String OPERATION_LOG_DDL = "CREATE TABLE IF NOT EXISTS " + OPERATION_LOG_TABLE + " (\n"
            + "  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',\n"
            + "  trace_id VARCHAR(64) DEFAULT NULL COMMENT '链路追踪 ID',\n"
            + "  biz_id VARCHAR(128) DEFAULT NULL COMMENT '业务对象标识',\n"
            + "  module VARCHAR(64) DEFAULT NULL COMMENT '类名',\n"
            + "  method VARCHAR(128) DEFAULT NULL COMMENT '方法签名',\n"
            + "  description VARCHAR(255) DEFAULT NULL COMMENT '操作描述',\n"
            + "  operator VARCHAR(64) DEFAULT NULL COMMENT '操作人',\n"
            + "  request_uri VARCHAR(255) DEFAULT NULL COMMENT '请求 URI',\n"
            + "  http_method VARCHAR(8) DEFAULT NULL COMMENT 'HTTP 方法',\n"
            + "  client_ip VARCHAR(64) DEFAULT NULL COMMENT '客户端 IP',\n"
            + "  user_agent VARCHAR(255) DEFAULT NULL COMMENT 'User-Agent',\n"
            + "  request_headers TEXT COMMENT '请求头 JSON',\n"
            + "  result TINYINT DEFAULT NULL COMMENT '1=成功 0=失败',\n"
            + "  error_message TEXT COMMENT '异常堆栈摘要',\n"
            + "  cost_ms BIGINT DEFAULT NULL COMMENT '耗时（毫秒）',\n"
            + "  is_slow TINYINT DEFAULT 0 COMMENT '是否慢方法',\n"
            + "  create_time DATETIME DEFAULT NULL COMMENT '创建时间',\n"
            + "  PRIMARY KEY (id),\n"
            + "  KEY idx_trace_id (trace_id),\n"
            + "  KEY idx_biz_id (biz_id),\n"
            + "  KEY idx_create_time (create_time)\n"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志主表'";

    /**
     * 参数表 DDL（占位未替换）
     */
    public static final String OPERATION_LOG_PARAM_DDL = "CREATE TABLE IF NOT EXISTS "
            + OPERATION_LOG_PARAM_TABLE + " (\n"
            + "  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',\n"
            + "  log_id BIGINT NOT NULL COMMENT '关联 operation_log.id',\n"
            + "  param_type VARCHAR(16) DEFAULT NULL COMMENT 'IN / OUT',\n"
            + "  param_index INT DEFAULT NULL COMMENT '参数顺序',\n"
            + "  param_name VARCHAR(128) DEFAULT NULL COMMENT '参数名',\n"
            + "  param_value LONGTEXT COMMENT '参数值（JSON / 字符串）',\n"
            + "  PRIMARY KEY (id),\n"
            + "  KEY idx_log_id (log_id)\n"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志参数表'";

    private OperationLogDdlProvider() {
    }

    /**
     * 替换 DDL 占位为实际表前缀
     *
     * @param ddl       含 {@code {prefix}} 占位的 DDL
     * @param tablePrefix 表名前缀
     * @return String 替换后的 DDL
     */
    public static String resolve(String ddl, String tablePrefix) {
        if (tablePrefix == null) {
            tablePrefix = "";
        }
        return ddl.replace("{prefix}", tablePrefix);
    }
}
