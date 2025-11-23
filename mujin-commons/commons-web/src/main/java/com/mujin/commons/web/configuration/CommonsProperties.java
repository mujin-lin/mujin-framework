package com.mujin.commons.web.configuration;


import com.mujin.commons.web.enums.CommonsConfigEnum;
import lombok.Data;

/**
 * 使用加密解密的manager
 *
 * @author chenglin.wu
 * @date 2025/11/23
 */
@Data
public class CommonsProperties {
    /**
     * 加密解密类型使用哪个缓存进行保存
     */
    private CommonsConfigEnum encryptManagerType = CommonsConfigEnum.DEFAULT;
    /**
     * 登录用户使用哪个缓存进行保存
     */
    private CommonsConfigEnum loginUserManagerType = CommonsConfigEnum.DEFAULT;
}
