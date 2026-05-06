
package com.mujin.commons.web.response;


import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 统一响应的数据接收类
 *
 * @author chenglin.wu
 * @date 2026/05/06
 */
@Data
@SuppressWarnings("unused")
@EqualsAndHashCode(callSuper = true)
public class ResponseResult<T> extends BaseResponseResult implements Serializable {

    /**
     * 返回数据
     */
    private T resData;
}
