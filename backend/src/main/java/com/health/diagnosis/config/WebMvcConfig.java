package com.health.diagnosis.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置。
 *
 * <p>JWT 认证已迁移至 Spring Security（SecurityConfig + JwtAuthenticationFilter），
 * 此处不再注册拦截器。保留 WebMvcConfigurer 以备后续扩展。</p>
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    // 认证 & 授权已由 Spring Security 接管，无需在此注册拦截器
}
