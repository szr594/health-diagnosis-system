package com.health.diagnosis.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域配置（已迁移至 SecurityConfig.corsConfigurationSource）。
 *
 * <p>Spring Security 过滤链中的 CORS 配置优先级更高，此处保留类占位以防其他模块引用。
 * 实际 CORS 规则在 {@link SecurityConfig#corsConfigurationSource()} 中定义。</p>
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    // CORS 已由 Spring Security 过滤链统一处理，此处无需重复配置
}
