package com.mujin.logging.context;

/**
 * 登录上下文持有者（L2 临时方案）
 * <p>
 * 基于 ThreadLocal 保存当前请求的登录人信息。L6 security 模块落地后，
 * 应改为读取 {@code com.mujin.security.context.SecurityContextHolder}，
 * 避免双 holder 维护成本。当前实现仅供 logging 模块内部使用，
 * 业务侧不要直接调用 {@link #setLoginId(String)}，
 * 应由统一拦截器（未来 L6）写入。
 *
 * @author chenglin.wu
 * @date 2026/08/08
 */
public final class LoginContextHolder {

    /**
     * 当前线程登录人 ID
     */
    private static final ThreadLocal<String> LOGIN_ID = new ThreadLocal<>();

    /**
     * 当前线程登录人姓名（可选，便于日志展示）
     */
    private static final ThreadLocal<String> LOGIN_NAME = new ThreadLocal<>();

    /**
     * 私有构造，禁止实例化
     */
    private LoginContextHolder() {
    }

    /**
     * 设置当前线程登录上下文
     *
     * @param loginId   登录人 ID
     * @param loginName 登录人姓名（可空）
     */
    public static void set(String loginId, String loginName) {
        LOGIN_ID.set(loginId);
        LOGIN_NAME.set(loginName);
    }

    /**
     * 获取登录人 ID
     *
     * @return String 未登录返回 null
     */
    public static String getLoginId() {
        return LOGIN_ID.get();
    }

    /**
     * 获取登录人姓名
     *
     * @return String 未登录返回 null
     */
    public static String getLoginName() {
        return LOGIN_NAME.get();
    }

    /**
     * 清理当前线程登录上下文，必须在请求结束后调用避免线程复用泄漏
     */
    public static void clear() {
        LOGIN_ID.remove();
        LOGIN_NAME.remove();
    }
}
