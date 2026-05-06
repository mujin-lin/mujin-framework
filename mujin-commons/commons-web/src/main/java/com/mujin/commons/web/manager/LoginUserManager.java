package com.mujin.commons.web.manager;


import com.mujin.commons.web.model.LoginUserModel;

/**
 * 登录对象的 manager
 *
 * @author chenglin.wu
 * @date 2026/05/06
 */
public interface LoginUserManager {


    /**
     * 添加用户到缓存中,默认30分钟
     *
     * @param loginUser 登录对象
     * @date 2026/05/06
     **/
    void setLoginUser(LoginUserModel loginUser);

    /**
     * 通过请求对象获取 token
     *
     * @param request 请求对象
     * @return LoginUser
     * @date 2026/05/06
     */
    <T> LoginUserModel getLoginUserByRequest(T request);

    /**
     * 获取缓存中的 LoginUser
     *
     * @param token 登录令牌
     * @return LoginUserModel
     * @date 2026/05/06
     **/
    LoginUserModel getLoginUserByToken(String token);

    /**
     * 删除缓存中的 user
     *
     * @param token 登录的令牌
     * @date 2026/05/06
     **/
    void deleteLoginUser(String token);

    /**
     * 更新缓存中的 LoginUser
     *
     * @param token token信息
     * @date 2026/05/06
     **/
    void refreshLoginTime(String token);

    /**
     * 通过 userId 获取缓存中的 LoginUser
     *
     * @param userId 用户 id
     * @date 2026/05/06
     **/
    LoginUserModel getLoginUser(String userId);

    /**
     * 从缓存中获取 userId
     *
     * @param token 登录令牌
     * @date 2026/05/06
     **/
    String getCacheId(String token);

    /**
     * 通过 id 批量删除登录对象
     *
     * @param cacheId 缓存id
     * @date 2026/05/06
     */
    void deleteLoginUserById(String... cacheId);
}
