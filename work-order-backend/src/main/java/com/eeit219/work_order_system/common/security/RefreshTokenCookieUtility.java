package com.eeit219.work_order_system.common.security;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletResponse;

@Component
public class RefreshTokenCookieUtility {

        public static final String COOKIE_NAME = "refresh_token";

        private final long expireDays;
        private final boolean secure;

        public RefreshTokenCookieUtility(
                        @Value("${refresh.token.expire-days}") long expireDays,
                        @Value("${refresh.cookie.secure:false}") boolean secure) {
                this.expireDays = expireDays;
                this.secure = secure;
        }

        public void addCookie(
                        HttpServletResponse response,
                        String refreshToken) {

                ResponseCookie cookie = ResponseCookie
                                .from(COOKIE_NAME, refreshToken)
                                .httpOnly(true)
                                .secure(secure)
                                .sameSite("Lax")
                                .path("/api/auth")
                                .maxAge(Duration.ofDays(expireDays))
                                .build();

                response.addHeader(
                                HttpHeaders.SET_COOKIE,
                                cookie.toString());
        }

        public void clearCookie(HttpServletResponse response) {
                ResponseCookie cookie = ResponseCookie
                                .from(COOKIE_NAME, "")
                                .httpOnly(true)
                                .secure(secure)
                                .sameSite("Lax")
                                .path("/api/auth")
                                .maxAge(Duration.ZERO)
                                .build();

                response.addHeader(
                                HttpHeaders.SET_COOKIE,
                                cookie.toString());
        }
}