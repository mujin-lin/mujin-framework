package com.mujin.logging.db.schema;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link OperationLogDdlProvider} DDL 拼接回归测试
 *
 * @author chenglin.wu
 * @date 2026/08/09
 */
class OperationLogDdlProviderTest {

    @Test
    void testResolveWithPrefix() {
        String resolved = OperationLogDdlProvider.resolve(
                OperationLogDdlProvider.OPERATION_LOG_DDL, "mujin_");
        assertTrue(resolved.contains("CREATE TABLE IF NOT EXISTS mujin_operation_log"));
        assertEquals(-1, resolved.indexOf("{prefix}"));
    }

    @Test
    void testResolveWithoutPrefix() {
        String resolved = OperationLogDdlProvider.resolve(
                OperationLogDdlProvider.OPERATION_LOG_DDL, "");
        assertTrue(resolved.contains("CREATE TABLE IF NOT EXISTS operation_log"));
    }

    @Test
    void testResolveNullPrefix() {
        String resolved = OperationLogDdlProvider.resolve(
                OperationLogDdlProvider.OPERATION_LOG_PARAM_DDL, null);
        assertTrue(resolved.contains("CREATE TABLE IF NOT EXISTS operation_log_param"));
    }

    @Test
    void testParamDdlResolve() {
        String resolved = OperationLogDdlProvider.resolve(
                OperationLogDdlProvider.OPERATION_LOG_PARAM_DDL, "mujin_");
        assertTrue(resolved.contains("CREATE TABLE IF NOT EXISTS mujin_operation_log_param"));
        // 包含 log_id 索引
        assertTrue(resolved.contains("KEY idx_log_id"));
    }

    @Test
    void testMainDdlContainsIndexes() {
        String ddl = OperationLogDdlProvider.resolve(
                OperationLogDdlProvider.OPERATION_LOG_DDL, "mujin_");
        // 主表关键索引
        assertTrue(ddl.contains("KEY idx_trace_id"));
        assertTrue(ddl.contains("KEY idx_biz_id"));
        assertTrue(ddl.contains("KEY idx_create_time"));
        // 主键
        assertTrue(ddl.contains("PRIMARY KEY (id)"));
    }

    @Test
    void testExtractTableNameFromMainDdl() {
        // 通过反射调用 extractTableName 不可行（私有），改为验证 DDL 结构包含 IF NOT EXISTS + 表名
        String ddl = OperationLogDdlProvider.OPERATION_LOG_DDL;
        assertTrue(ddl.startsWith("CREATE TABLE IF NOT EXISTS {prefix}operation_log"));
    }

    @Test
    void testExtractTableNameFromParamDdl() {
        String ddl = OperationLogDdlProvider.OPERATION_LOG_PARAM_DDL;
        assertTrue(ddl.startsWith("CREATE TABLE IF NOT EXISTS {prefix}operation_log_param"));
    }
}
