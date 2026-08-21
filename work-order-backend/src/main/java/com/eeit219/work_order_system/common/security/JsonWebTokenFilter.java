package com.eeit219.work_order_system.common.security;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.eeit219.work_order_system.modules.a.entity.User;
import com.eeit219.work_order_system.modules.a.repository.UserRepository;
import com.eeit219.work_order_system.modules.a.repository.UserRoleRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JsonWebTokenFilter extends OncePerRequestFilter {

    private final JsonWebTokenUtility jwtUtil;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    public JsonWebTokenFilter(JsonWebTokenUtility jwtUtil,
            UserRepository userRepository,
            UserRoleRepository userRoleRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String method = request.getMethod();
        // 如 method 等於(OPTIONS、TRACE、CONNECT) 不檢查 Filter，執行後續程式 (呼叫 controller)
        if ("OPTIONS".equalsIgnoreCase(method) || "TRACE".equalsIgnoreCase(method) ||
                "CONNECT".equalsIgnoreCase(method)) {
            filterChain.doFilter(request, response); // 執行後續程式
            return;
        }

        String auth = request.getHeader("Authorization");
        String token = null;
        if (auth != null && auth.startsWith("Bearer ")) {
            token = auth.substring(7); // 去掉前面的"Bearer "
        } else if (request.getParameter("token") != null) {
            token = request.getParameter("token"); // 支援 WebSocket 從 URL 帶入 ?token=xxx
        }

        if (token != null) {
            String subject = jwtUtil.validateToken(token); // 驗證token
            if (subject != null) {
                JSONObject userData = new JSONObject(subject);
                Integer userId = userData.getInt("userId");
                User user = userRepository.findById(userId).orElse(null);

                if (user != null && user.getStatus() == User.UserStatus.ACTIVE) {
                    List<SimpleGrantedAuthority> authorities = userRoleRepository
                            .findRoleCodesByUserId(userId)
                            .stream()
                            .map(roleCode -> roleCode.trim().toUpperCase())
                            .distinct()
                            .map(roleCode -> new SimpleGrantedAuthority("ROLE_" + roleCode))
                            .toList();

                    AuthenticatedUser principal = new AuthenticatedUser(
                            user.getUserId(),
                            user.getAccount());
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            principal, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    String path = request.getServletPath();
                    boolean isChangeInitialPasswordRequest = "/account/initial-password".equals(path)
                            && "PATCH".equalsIgnoreCase(request.getMethod());

                    if (Boolean.TRUE.equals(user.getMustChangePassword())
                            && !isChangeInitialPasswordRequest) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        response.setCharacterEncoding("UTF-8");

                        JSONObject body = new JSONObject()
                                .put("success", false)
                                .put("status", HttpServletResponse.SC_FORBIDDEN)
                                .put("code", "PASSWORD_CHANGE_REQUIRED")
                                .put("message", "首次登入必須先修改密碼")
                                .put("data", JSONObject.NULL)
                                .put("timestamp", LocalDateTime.now().toString());
                        response.getWriter().write(body.toString());
                        return;
                    }

                    filterChain.doFilter(request, response); // 執行後續程式
                    return;
                }
            }
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        JSONObject body = new JSONObject()
                .put("success", false)
                .put("status", HttpServletResponse.SC_UNAUTHORIZED)
                .put("message", "未登入或 Token 無效")
                .put("data", JSONObject.NULL)
                .put("timestamp", LocalDateTime.now().toString());
        response.getWriter().write(body.toString());
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        // 回傳 true (不檢查 Filter)；回傳 false (檢查 Filter)
        return "/auth/login".equals(path) ||
                "/auth/register".equals(path) ||
                "/auth/reset-password".equals(path) ||
                "/auth/forgot-password".equals(path)
                || "/auth/oauth2/session".equals(path)
                || path.startsWith("/oauth2/")
                || path.startsWith("/login/oauth2/");
    }

}
