package com.health.diagnosis.common;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    @Value("${health.jwt.secret}")
    private String secret;

    @Value("${health.jwt.expire-hours}")
    private Long expireHours;

    public String generateToken(Long userId, String username, Integer role) {
        Date now = new Date();
        Date expireAt = DateUtil.offsetHour(now, expireHours.intValue());
        return JWT.create()
                .setPayload("userId", String.valueOf(userId))
                .setPayload("username", username)
                .setPayload("role", String.valueOf(role))
                .setIssuedAt(now)
                .setExpiresAt(expireAt)
                .setKey(secret.getBytes(StandardCharsets.UTF_8))
                .sign();
    }

    public boolean verify(String token) {
        try {
            return JWTUtil.verify(token, secret.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.debug("JWT 校验失败: {}", e.getMessage());
            return false;
        }
    }

    public Long getUserId(String token) {
        JWT jwt = JWTUtil.parseToken(token);
        Object value = jwt.getPayload("userId");
        return value == null ? null : Long.valueOf(value.toString());
    }

    public String getUsername(String token) {
        JWT jwt = JWTUtil.parseToken(token);
        Object value = jwt.getPayload("username");
        return value == null ? null : value.toString();
    }

    public Integer getRole(String token) {
        JWT jwt = JWTUtil.parseToken(token);
        Object value = jwt.getPayload("role");
        return value == null ? null : Integer.valueOf(value.toString());
    }

    public static String extractToken(String authHeader) {
        if (StrUtil.isNotBlank(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
