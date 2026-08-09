package com.mujin.commons.web.response;


import lombok.Data;

/**
 * web 统一响应对象
 *
 * @author chenglin.wu
 * @date 2026/05/06
 */
@Data
@SuppressWarnings("unused")
public class BaseResponseResult {
    /**
     * 返回代码
     */
    private int resCode;

    /**
     * 返回信息
     */
    private String resMsg;
    /**
     * 返回详细信息
     */
    private String detailMsg;

}
