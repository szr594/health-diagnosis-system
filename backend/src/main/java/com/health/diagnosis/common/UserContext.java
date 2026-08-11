package com.health.diagnosis.common;

/**
 * 用户上下文：在请求线程内保存当前登录用户信息。
 *
 * <p>由 JwtAuthenticationFilter 在认证通过后写入，请求结束后清理，避免内存泄漏。
 * 携带 userId、username、role 三项，业务代码可直接读取。</p>
 */
public class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USERNAME = new ThreadLocal<>();
    private static final ThreadLocal<Integer> ROLE = new ThreadLocal<>();

    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static void setUsername(String username) {
        USERNAME.set(username);
    }

    public static String getUsername() {
        return USERNAME.get();
    }

    public static void setRole(Integer role) {
        ROLE.set(role);
    }

    public static Integer getRole() {
        return ROLE.get();
    }

    /**
     * 判断当前用户是否为指定角色。
     */
    public static boolean hasRole(RoleType roleType) {
        Integer role = getRole();
        return role != null && role == roleType.getCode();
    }

    public static void clear() {
        USER_ID.remove();
        USERNAME.remove();
        ROLE.remove();
    }
}
