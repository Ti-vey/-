package com.warehouse.security;

/**
 * 基于 ThreadLocal 的登录用户上下文。
 * 请求进入时由拦截器写入，请求结束时清理，避免线程复用导致的数据串号。
 */
public class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static Long getUserId() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.getUserId();
    }

    public static String getRoleCode() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.getRoleCode();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
