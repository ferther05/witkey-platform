// JwtAuthFilter —— 安全过滤器：解析请求头里的 JWT，把登录用户放入
package com.psc.witkey.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import com.psc.witkey.repository.SysUserRepository;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 每次请求读一次 Authorization: Bearer xxx，解析成功则写入 SecurityContext。
 * 解析失败不直接返回 401，交给 Security 的 EntryPoint 统一处理，避免影响白名单接口。
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final SysUserRepository userRepository;

    public JwtAuthFilter(JwtUtil jwtUtil, SysUserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    // 解析 JWT 并写入安全上下文
    @Override
    // 执行 doFilterInternal
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            LoginUser user = jwtUtil.parse(header.substring(7).trim());
            if (user != null) {
                // 每次请求都校验账号状态：封禁后旧 token 立即失效，不用等 7 天过期
                var dbUser = userRepository.findById(user.id()).orElse(null);
                if (dbUser == null || (dbUser.getStatus() != null && dbUser.getStatus() == 0)) {
                    response.setStatus(401);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                    response.getWriter().write(
                            "{\"code\":401,\"message\":\"账号已被封禁或不存在，请重新登录\",\"data\":null}");
                    return;
                }
                var auth = new UsernamePasswordAuthenticationToken(
                        user, null, List.of(new SimpleGrantedAuthority("ROLE_" + user.role())));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        chain.doFilter(request, response);
    }
}
