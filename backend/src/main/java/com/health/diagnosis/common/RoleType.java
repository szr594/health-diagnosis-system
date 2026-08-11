package com.health.diagnosis.common;

/**
 * 系统角色枚举（RBAC）。
 *
 * <p>数据库 t_user.role 字段存储 int 值，此枚举统一映射：
 * <ul>
 *   <li>0 → PATIENT  患者</li>
 *   <li>1 → DOCTOR   医生</li>
 *   <li>2 → ADMIN    管理员</li>
 * </ul>
 * 在 Spring Security 方法级注解中以 ROLE_ 前缀使用，如 @PreAuthorize("hasRole('DOCTOR')")。</p>
 */
public enum RoleType {

    PATIENT(0, "ROLE_PATIENT"),
    DOCTOR(1, "ROLE_DOCTOR"),
    ADMIN(2, "ROLE_ADMIN");

    private final int code;
    private final String authority;

    RoleType(int code, String authority) {
        this.code = code;
        this.authority = authority;
    }

    public int getCode() {
        return code;
    }

    public String getAuthority() {
        return authority;
    }

    /**
     * 根据 int code 转为枚举，非法值默认返回 PATIENT。
     */
    public static RoleType fromCode(Integer code) {
        if (code == null) return PATIENT;
        for (RoleType role : values()) {
            if (role.code == code) {
                return role;
            }
        }
        return PATIENT;
    }

    /**
     * 根据 int code 直接获取 Spring Security authority 字符串。
     */
    public static String authorityOf(Integer code) {
        return fromCode(code).getAuthority();
    }
}
