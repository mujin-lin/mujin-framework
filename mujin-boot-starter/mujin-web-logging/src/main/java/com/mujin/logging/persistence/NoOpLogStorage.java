package com.mujin.logging.persistence;

import com.mujin.logging.model.OperationLogContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * L1 阶段默认实现：仅打印日志，便于在 L2/L3 之前调试 Aspect 流程。
 * L3 阶段将由 {@code DbLogStorage} / {@code FileLogStorage} 取代。
 *
 * @author chenglin.wu
 * @date 2026/08/08
 */
public class NoOpLogStorage implements LogStorage {

    private static final Logger LOG = LoggerFactory.getLogger(NoOpLogStorage.class);

    @Override
    public void save(OperationLogContext context) {
        LOG.info("[OPERATION-LOG] desc={}, bizId={}, cost={}ms, slow={}, result={}, params={}",
                context.getDescription(),
                context.getBizId(),
                context.getCostMs(),
                context.isSlow(),
                context.getResult(),
                context.getParams());
    }
}
