package com.health.diagnosis.config;

import cn.hutool.json.JSONUtil;
import com.health.diagnosis.common.Result;
import com.health.diagnosis.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security 配置。
 *
 * <p>核心策略：
 * <ul>
 *   <li>STATELESS：无 session，纯 JWT 认证</li>
 *   <li>关闭 CSRF（前后端分离 + JWT 不需要）</li>
 *   <li>CORS 集成到 Security 过滤链，替代原 CorsConfig</li>
 *   <li>白名单：登录/注册/健康检查/SSE 流式问诊</li>
 *   <li>方法级权限：通过 @EnableMethodSecurity 开启 @PreAuthorize</li>
 * </ul>
 * </p>
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /** 白名单路径：无需认证即可访问 */
    private static final String[] WHITELIST = {
            "/api/user/login",
            "/api/user/register",
            "/api/ai/health",
            "/api/medical/hot",
            "/error"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 关闭 CSRF（前后端分离 + JWT）
                .csrf(AbstractHttpConfigurer::disable)
                // CORS 配置
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 无状态会话
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 路径权限
                .authorizeHttpRequests(auth -> auth
                        // OPTIONS 预检全部放行
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // 白名单
                        .requestMatchers(WHITELIST).permitAll()
                        // 其余 /api/** 需认证
                        .requestMatchers("/api/**").authenticated()
                        // 非 /api 路径放行
                        .anyRequest().permitAll()
                )
                // JWT 过滤器放在 UsernamePasswordAuthenticationFilter 之前
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // 异常处理
                .exceptionHandling(ex -> ex
                        // 未认证（401）
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(401);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write(
                                    JSONUtil.toJsonStr(Result.error(401, "未登录或登录已过期")));
                        })
                        // 权限不足（403）
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(403);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write(
                                    JSONUtil.toJsonStr(Result.error(403, "权限不足，无法访问此资源")));
                        })
                );

        return http.build();
    }

    /**
     * 密码编码器：Spring Security 标准 BCrypt（替代 Hutool BCrypt）。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * CORS 配置源（集成到 Security 过滤链）。
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
