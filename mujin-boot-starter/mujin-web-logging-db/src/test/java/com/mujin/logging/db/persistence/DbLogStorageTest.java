package com.mujin.logging.db.persistence;

import com.mujin.logging.db.entity.OperationLogEntity;
import com.mujin.logging.db.entity.OperationLogParamEntity;
import com.mujin.logging.db.mapper.OperationLogMapper;
import com.mujin.logging.db.mapper.OperationLogParamMapper;
import com.mujin.logging.enums.LogResultEnum;
import com.mujin.logging.model.OperationLogContext;
import com.mujin.logging.model.OperationLogParam;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link DbLogStorage} 持久化逻辑回归测试
 *
 * @author chenglin.wu
 * @date 2026/08/09
 */
class DbLogStorageTest {

    private OperationLogMapper logMapper;
    private OperationLogParamMapper paramMapper;
    private DbLogStorage storage;

    @BeforeEach
    void setUp() {
        logMapper = mock(OperationLogMapper.class);
        paramMapper = mock(OperationLogParamMapper.class);
        storage = new DbLogStorage(logMapper, paramMapper);
    }

    @Test
    void testSaveWithParams() {
        // 模拟主表 insert 后回填 ID
        when(logMapper.insert(any(OperationLogEntity.class))).thenAnswer(invocation -> {
            OperationLogEntity entity = invocation.getArgument(0);
            entity.setId(100L);
            return 1;
        });

        OperationLogContext context = new OperationLogContext();
        context.setTraceId("trace-1");
        context.setBizId("order-001");
        context.setModule("OrderController");
        context.setMethod("create");
        context.setDescription("创建订单");
        context.setOperator("alice");
        context.setResult(LogResultEnum.SUCCESS.getCode());
        context.setCostMs(123L);
        context.setSlow(false);
        context.setParams(Arrays.asList(
                OperationLogParam.ofIn(0, "req", "{\"id\":1}"),
                OperationLogParam.ofIn(1, "userId", "u-1")
        ));
        context.setResultParam(OperationLogParam.ofOut("result", "{\"ok\":true}"));

        storage.save(context);

        // 验证主表 insert 被调用一次
        ArgumentCaptor<OperationLogEntity> logCaptor = ArgumentCaptor.forClass(OperationLogEntity.class);
        verify(logMapper, times(1)).insert(logCaptor.capture());
        OperationLogEntity saved = logCaptor.getValue();
        assertEquals("trace-1", saved.getTraceId());
        assertEquals("order-001", saved.getBizId());
        assertEquals("OrderController", saved.getModule());
        assertEquals(0, saved.getIsSlow());
        assertEquals(1, saved.getResult());
        assertNotNull(saved.getCreateTime());

        // 验证参数表 insert 被调用 3 次（2 个入参 + 1 个出参）
        ArgumentCaptor<OperationLogParamEntity> paramCaptor = ArgumentCaptor.forClass(OperationLogParamEntity.class);
        verify(paramMapper, times(3)).insert(paramCaptor.capture());
        List<OperationLogParamEntity> paramEntities = paramCaptor.getAllValues();
        assertEquals("IN", paramEntities.get(0).getParamType());
        assertEquals("IN", paramEntities.get(1).getParamType());
        assertEquals("OUT", paramEntities.get(2).getParamType());
        // logId 应等于主表自增的 ID
        paramEntities.forEach(p -> assertEquals(100L, p.getLogId()));
    }

    @Test
    void testSaveWithoutParams() {
        when(logMapper.insert(any(OperationLogEntity.class))).thenAnswer(invocation -> {
            OperationLogEntity entity = invocation.getArgument(0);
            entity.setId(200L);
            return 1;
        });

        OperationLogContext context = new OperationLogContext();
        context.setTraceId("trace-2");
        context.setModule("X");
        context.setMethod("y");

        storage.save(context);

        verify(logMapper, times(1)).insert(any(OperationLogEntity.class));
        // 无入参/出参，参数表 insert 不应被调用
        verify(paramMapper, times(0)).insert(any(OperationLogParamEntity.class));
    }

    @Test
    void testSaveNullContext() {
        storage.save(null);
        verify(logMapper, times(0)).insert(any(OperationLogEntity.class));
        verify(paramMapper, times(0)).insert(any(OperationLogParamEntity.class));
    }

    @Test
    void testSaveOnlyResultParam() {
        when(logMapper.insert(any(OperationLogEntity.class))).thenAnswer(invocation -> {
            OperationLogEntity entity = invocation.getArgument(0);
            entity.setId(300L);
            return 1;
        });

        OperationLogContext context = new OperationLogContext();
        context.setResultParam(OperationLogParam.ofOut("result", "ok"));

        storage.save(context);

        // 仅 1 次参数表 insert（出参）
        verify(paramMapper, times(1)).insert(any(OperationLogParamEntity.class));
    }

    @Test
    void testSaveWhenLogIdIsNull() {
        // 极端情况：主表 insert 后未回填 ID，不应触发参数表 insert
        when(logMapper.insert(any(OperationLogEntity.class))).thenReturn(1);

        OperationLogContext context = new OperationLogContext();
        context.setParams(Arrays.asList(OperationLogParam.ofIn(0, "a", "1")));

        storage.save(context);

        verify(logMapper, times(1)).insert(any(OperationLogEntity.class));
        verify(paramMapper, times(0)).insert(any(OperationLogParamEntity.class));
    }
}
