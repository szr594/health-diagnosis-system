package com.health.diagnosis.interceptor;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.health.diagnosis.common.JwtUtil;
import com.health.diagnosis.common.Result;
import com.health.diagnosis.common.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String auth = request.getHeader(AUTH_HEADER);
        if (StrUtil.isNotBlank(auth) && auth.startsWith(BEARER_PREFIX)) {
            String token = auth.substring(BEARER_PREFIX.length());
            if (jwtUtil.verify(token)) {
                Long userId = jwtUtil.getUserId(token);
                if (userId != null) {
                    UserContext.setUserId(userId);
                    return true;
                }
            }
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSONUtil.toJsonStr(Result.error(401, "未登录或登录已过期")));
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
