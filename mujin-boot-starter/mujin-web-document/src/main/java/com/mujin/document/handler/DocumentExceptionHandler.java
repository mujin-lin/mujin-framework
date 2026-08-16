package com.mujin.document.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mujin.commons.lang.code.BaseErrorCode;
import com.mujin.commons.lang.exception.BusinessException;
import com.mujin.commons.lang.exception.CommonsException;
import com.mujin.commons.lang.exception.FrameworkException;
import com.mujin.commons.web.response.ResponseResult;
import com.mujin.commons.web.response.ResponseUtils;
import com.mujin.document.code.DocumentErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;

/**
 * 文档模块统一异常处理器
 * <p>
 * 将各种异常映射为 {@link ResponseResult}，避免 Controller 内重复 try/catch。
 *
 * @author chenglin.wu
 * @date 2026/08/16
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.mujin.document.controller")
public class DocumentExceptionHandler {

    /**
     * 业务异常
     *
     * @param e 业务异常
     * @return ResponseResult<Void>
     * @author chenglin.wu
     * @date 2026/08/16
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseResult<Void> handleBusiness(BusinessException e) {
        log.warn("业务异常：errCode={}, errMsg={}", e.getErrCode(), e.getErrMsg());
        return ResponseUtils.fail(e.getErrCode(), e.getErrMsg());
    }

    /**
     * 框架异常
     *
     * @param e 框架异常
     * @return ResponseResult<Void>
     * @author chenglin.wu
     * @date 2026/08/16
     */
    @ExceptionHandler(FrameworkException.class)
    public ResponseResult<Void> handleFramework(FrameworkException e) {
        log.error("框架异常：errCode={}, errMsg={}", e.getErrCode(), e.getErrMsg(), e);
        return ResponseUtils.fail(e.getErrCode(), e.getErrMsg());
    }

    /**
     * 通用异常
     *
     * @param e 通用异常
     * @return ResponseResult<Void>
     * @author chenglin.wu
     * @date 2026/08/16
     */
    @ExceptionHandler(CommonsException.class)
    public ResponseResult<Void> handleCommons(CommonsException e) {
        log.warn("通用异常：errCode={}, errMsg={}", e.getErrCode(), e.getErrMsg());
        return ResponseUtils.fail(e.getErrCode(), e.getErrMsg());
    }

    /**
     * JSON 序列化失败
     *
     * @param e JSON 异常
     * @return ResponseResult<Void>
     * @author chenglin.wu
     * @date 2026/08/16
     */
    @ExceptionHandler(JsonProcessingException.class)
    public ResponseResult<Void> handleJson(JsonProcessingException e) {
        log.error("JSON 序列化失败", e);
        return ResponseUtils.fail(DocumentErrorCode.SERIALIZE_FAILED.errorCode(),
                "JSON 序列化失败：" + e.getOriginalMessage());
    }

    /**
     * IO 异常（PDF 渲染、文件写入）
     *
     * @param e IO 异常
     * @return ResponseResult<Void>
     * @author chenglin.wu
     * @date 2026/08/16
     */
    @ExceptionHandler(IOException.class)
    public ResponseResult<Void> handleIo(IOException e) {
        log.error("IO 异常", e);
        return ResponseUtils.fail(DocumentErrorCode.EXPORT_FAILED.errorCode(),
                "IO 异常：" + e.getMessage());
    }

    /**
     * 兜底：其他未捕获异常
     *
     * @param e 异常
     * @return ResponseResult<Void>
     * @author chenglin.wu
     * @date 2026/08/16
     */
    @ExceptionHandler(Exception.class)
    public ResponseResult<Void> handleAny(Exception e) {
        log.error("未处理异常", e);
        return ResponseUtils.fail(BaseErrorCode.UNKNOWN_ERROR.errorCode(),
                "服务器内部错误：" + e.getMessage());
    }
}
