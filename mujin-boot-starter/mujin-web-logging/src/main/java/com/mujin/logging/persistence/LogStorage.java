package com.mujin.logging.persistence;

import com.mujin.logging.model.OperationLogContext;

/**
 * 日志存储策略接口
 *
 * @author chenglin.wu
 * @date 2026/08/08
 */
public interface LogStorage {

    /**
     * 持久化一条操作日志
     *
     * @param context 运行期上下文
     */
    void save(OperationLogContext context);
}
