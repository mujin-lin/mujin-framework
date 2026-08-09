package com.mujin.commons.web.response;



/**
 * 组建统一返回的工具类
 *
 * @author chenglin.wu
 * @date 2026/05/06
 */
@SuppressWarnings({"unused", "rawtype"})
public final class ResponseUtils {
    /**
     * 正常返回的常量
     */
    private static final String OK = "OK";

    private ResponseUtils() {
    }

    /**
     * 返回成功有数据
     *
     * @param data the data
     * @return ResponseResult<T>
     * @date 2026/05/06
     */
    public static <T> ResponseResult<T> success(T data) {
        ResponseResult<T> result = new ResponseResult<>();
        result.setResCode(0);
        result.setResMsg(OK);
        result.setResData(data);
        return result;
    }

    /**
     * 返回失败，有数据，有message
     *
     * @param data the data
     * @param msg  the msg
     * @return ResponseResult<T>
     * @date 2026/05/06
     */
    public static <T> ResponseResult<T> success(T data, String msg) {
        ResponseResult<T> result = new ResponseResult<>();
        result.setResCode(0);
        result.setResMsg(msg);
        result.setResData(data);
        return result;
    }

    /**
     * 返回成功，有消息提示，无数据返回
     *
     * @param msg the msg
     * @return ResponseResult<T>
     * @date 2026/05/06
     */
    public static <T> ResponseResult<T> successMsg(String msg) {
        ResponseResult<T> result = new ResponseResult<>();
        result.setResCode(0);
        result.setResMsg(msg);
        return result;
    }

    /**
     * 返回成功，默认消息提示，无数据
     *
     * @return ResponseResult<T>
     * @date 2026/05/06
     */
    public static <T> ResponseResult<T> success() {
        return successMsg(OK);
    }

    /**
     * 返回失败，填充错误码和错误消息提示
     *
     * @param errCode the errCode
     * @param errMsg  the errMsg
     * @return ResponseResult<T>
     * @date 2026/05/06
     */
    public static <T> ResponseResult<T> fail(int errCode, String errMsg) {
        ResponseResult<T> result = new ResponseResult<>();
        result.setResCode(errCode);
        result.setResMsg(errMsg);
        return result;
    }

    /**
     * 返回失败，填充错误码和错误消息提示 详细信息
     *
     * @param errCode   the errCode
     * @param errMsg    the errMsg
     * @param detailMsg the detailMsg
     * @return ResponseResult<T>
     * @date 2026/05/06
     */
    public static <T> ResponseResult<T> fail(int errCode, String errMsg, String detailMsg) {
        ResponseResult<T> result = new ResponseResult<>();
        result.setResCode(errCode);
        result.setResMsg(errMsg);
        result.setDetailMsg(detailMsg);
        return result;
    }

}
