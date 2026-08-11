package com.health.diagnosis.security;

import com.health.diagnosis.common.JwtUtil;
import com.health.diagnosis.common.RoleType;
import com.health.diagnosis.common.UserContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER = "Authorization";

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(AUTH_HEADER);
        String token = JwtUtil.extractToken(authHeader);

        if (token != null && jwtUtil.verify(token)) {
            Long userId = jwtUtil.getUserId(token);
            String username = jwtUtil.getUsername(token);
            Integer role = jwtUtil.getRole(token);

            if (userId != null) {
                UserContext.setUserId(userId);
                UserContext.setUsername(username);
                UserContext.setRole(role);

                String authority = RoleType.authorityOf(role);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                List.of(new SimpleGrantedAuthority(authority))
                        );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
            SecurityContextHolder.clearContext();
        }
    }
}
