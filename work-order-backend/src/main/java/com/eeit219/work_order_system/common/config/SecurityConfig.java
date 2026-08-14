package com.eeit219.work_order_system.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.eeit219.work_order_system.common.security.JsonWebTokenFilter;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        private final JsonWebTokenFilter jwtFilter;

        public SecurityConfig(JsonWebTokenFilter jwtFilter) {
                this.jwtFilter = jwtFilter;
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .cors(cors -> {
                                })
                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                                .requestMatchers("/auth/login", "/auth/register",
                                                                "/auth/forgot-password", "/auth/reset-password")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.POST, "/users", "/api/repair-categories/**",
                                                                "/api/priorities/**")
                                                .hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.PATCH, "/users/**").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.GET, "/users", "/users/**").hasRole("ADMIN")
                                                .anyRequest().authenticated())
                                .exceptionHandling(exception -> exception
                                                .authenticationEntryPoint(authenticationEntryPoint())
                                                .accessDeniedHandler(accessDeniedHandler()))
                                .addFilterBefore(
                                                jwtFilter,
                                                UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        private AuthenticationEntryPoint authenticationEntryPoint() {
                return (request, response, exception) -> writeSecurityError(
                                response, HttpServletResponse.SC_UNAUTHORIZED, "未登入或 Token 無效");
        }

        private AccessDeniedHandler accessDeniedHandler() {
                return (request, response, exception) -> writeSecurityError(
                                response, HttpServletResponse.SC_FORBIDDEN, "沒有權限執行此操作");
        }

        private void writeSecurityError(HttpServletResponse response, int status, String message)
                        throws java.io.IOException {
                response.setStatus(status);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"success\":false,\"status\":" + status
                                + ",\"message\":\"" + message + "\",\"data\":null}");
        }
}
