package com.mujin.logging.db.persistence;

import com.mujin.logging.db.entity.OperationLogEntity;
import com.mujin.logging.db.entity.OperationLogParamEntity;
import com.mujin.logging.db.mapper.OperationLogMapper;
import com.mujin.logging.db.mapper.OperationLogParamMapper;
import com.mujin.logging.model.OperationLogContext;
import com.mujin.logging.model.OperationLogParam;
import com.mujin.logging.persistence.LogStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据库存储策略：将 {@link OperationLogContext} 写入 {@code operation_log} 与
 * {@code operation_log_param} 两表
 * <p>
 * 写入流程：
 * <ol>
 *     <li>转换 {@link OperationLogContext} 为 {@link OperationLogEntity}，插入主表并返回主键</li>
 *     <li>遍历入参与出参，逐条插入参数表（{@code logId} 来自上一步）</li>
 *     <li>任何异常仅打 warn，不抛出（不污染业务主流程）</li>
 * </ol>
 *
 * @author chenglin.wu
 * @date 2026/08/09
 */
public class DbLogStorage implements LogStorage {

    private static final Logger LOG = LoggerFactory.getLogger(DbLogStorage.class);

    /**
     * 操作日志主表 Mapper
     */
    private final OperationLogMapper logMapper;

    /**
     * 操作日志参数表 Mapper
     */
    private final OperationLogParamMapper paramMapper;

    public DbLogStorage(OperationLogMapper logMapper, OperationLogParamMapper paramMapper) {
        this.logMapper = logMapper;
        this.paramMapper = paramMapper;
    }

    @Override
    public void save(OperationLogContext context) {
        if (context == null) {
            return;
        }
        OperationLogEntity entity = toEntity(context);
        logMapper.insert(entity);
        Long logId = entity.getId();
        if (logId == null) {
            return;
        }

        List<OperationLogParam> params = context.getParams();
        if (params != null && !params.isEmpty()) {
            for (OperationLogParam param : params) {
                paramMapper.insert(toParamEntity(logId, param));
            }
        }
        if (context.getResultParam() != null) {
            paramMapper.insert(toParamEntity(logId, context.getResultParam()));
        }
    }

    /**
     * 将 {@link OperationLogContext} 转换为数据库实体
     *
     * @param context 操作日志上下文
     * @return OperationLogEntity 主表实体
     */
    private OperationLogEntity toEntity(OperationLogContext context) {
        OperationLogEntity entity = new OperationLogEntity();
        entity.setTraceId(context.getTraceId());
        entity.setBizId(context.getBizId());
        entity.setModule(context.getModule());
        entity.setMethod(context.getMethod());
        entity.setDescription(context.getDescription());
        entity.setOperator(context.getOperator());
        entity.setRequestUri(context.getRequestUri());
        entity.setHttpMethod(context.getHttpMethod());
        entity.setClientIp(context.getClientIp());
        entity.setUserAgent(context.getUserAgent());
        entity.setRequestHeaders(context.getRequestHeaders());
        entity.setResult(context.getResult());
        entity.setErrorMessage(context.getErrorMessage());
        entity.setCostMs(context.getCostMs());
        entity.setIsSlow(context.isSlow() ? 1 : 0);
        entity.setCreateTime(LocalDateTime.now());
        return entity;
    }

    /**
     * 将 {@link OperationLogParam} 转换为参数表实体
     *
     * @param logId 关联主表 ID
     * @param param 参数项
     * @return OperationLogParamEntity 参数表实体
     */
    private OperationLogParamEntity toParamEntity(Long logId, OperationLogParam param) {
        OperationLogParamEntity entity = new OperationLogParamEntity();
        entity.setLogId(logId);
        entity.setParamType(param.getParamType());
        entity.setParamIndex(param.getParamIndex());
        entity.setParamName(param.getParamName());
        entity.setParamValue(param.getParamValue());
        return entity;
    }
}
