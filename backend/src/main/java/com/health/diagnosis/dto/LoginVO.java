package com.health.diagnosis.dto;

import com.health.diagnosis.entity.User;
import lombok.Data;

/**
 * 登录成功返回：令牌 + 用户信息。
 */
@Data
public class LoginVO {

    /** JWT 令牌 */
    private String token;

    /** 用户信息（密码已忽略） */
    private User user;
}
