package com.mujin.commons.web.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录用户的 model
 *
 * @author chenglin.wu
 * @date 2026/05/06
 */
public interface LoginUserModel {
    /**
     * 获取登录账号
     *
     * @return String 登录账号
     * @date 2026/05/06
     */
    String getAccount();

    /**
     * 获取用户名
     *
     * @return String 用户名
     * @date 2026/05/06
     */
    String getUserName();

    /**
     * 获取缓存的id
     *
     * @return String 缓存id
     * @date 2026/05/06
     */
    String getCacheId();

    /**
     * 获取数据库id
     *
     * @return ID
     * @date 2024/11/19
     */
    <ID extends Serializable> ID getId();

    /**
     * 获取邮箱
     *
     * @return String 邮箱
     * @date 2026/05/06
     */
    String getEmail();

    /**
     * 电话号码
     *
     * @return String 电话号码
     * @date 2026/05/06
     */
    String getPhone();

    /**
     * 身份证号或者其他唯一标识
     *
     * @return String
     * @date 2026/05/06
     */
    String getIdCard();

    /**
     * 性别
     *
     * @return String
     * @date 2026/05/06
     */
    String getGender();

    /**
     * 头像
     *
     * @return String
     * @date 2026/05/06
     * @date 2026/05/06
     */
    String getHeaderImg();

    /**
     * 生日
     *
     * @return String
     * @date 2026/05/06
     */
    LocalDateTime getBirthday();

    /**
     * 地址或者住址
     *
     * @return String
     * @date 2026/05/06
     */
    String getAddress();

    /**
     * 请求来源
     *
     * @return String
     * @date 2024/11/13
     */
    String getCustomAgent();

    /**
     * 获取token
     *
     * @return String
     * @date 2024/11/13
     */
    String getToken();

    /**
     * 请求ip
     *
     * @return String
     * @date 2024/11/13
     */
    String getLoginIp();

    /**
     * 是否单态登录
     *
     * @return boolean
     * @date 2024/11/18
     */
    boolean isSingleLogin();

    /**
     * 扩展字段
     *
     * @return Object
     * @date 2024/11/13
     */
    Object getExtras();


}
