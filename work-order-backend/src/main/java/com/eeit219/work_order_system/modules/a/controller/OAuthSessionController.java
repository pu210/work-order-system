package com.eeit219.work_order_system.modules.a.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eeit219.work_order_system.common.response.ApiResponse;
import com.eeit219.work_order_system.common.security.OAuth2LoginSuccessHandler;
import com.eeit219.work_order_system.modules.a.dto.LoginResponseDTO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import com.eeit219.work_order_system.common.security.RefreshTokenCookieUtility;
import com.eeit219.work_order_system.modules.a.service.RefreshTokenService;

import jakarta.servlet.http.HttpServletResponse;

@RestController
public class OAuthSessionController {
        private final RefreshTokenService refreshTokenService;
        private final RefreshTokenCookieUtility refreshTokenCookieUtility;

        public OAuthSessionController(
                        RefreshTokenService refreshTokenService,
                        RefreshTokenCookieUtility refreshTokenCookieUtility) {
                this.refreshTokenService = refreshTokenService;
                this.refreshTokenCookieUtility = refreshTokenCookieUtility;
        }

        @GetMapping(value = "/auth/oauth2/session", produces = "application/json;charset=UTF-8")
        public ResponseEntity<ApiResponse<LoginResponseDTO>> exchangeOAuthSession(
                        HttpServletRequest request,
                        HttpServletResponse response) {

                HttpSession session = request.getSession(false);

                if (session == null) {
                        return ResponseEntity
                                        .status(HttpStatus.UNAUTHORIZED)
                                        .body(ApiResponse.error(
                                                        HttpStatus.UNAUTHORIZED.value(),
                                                        "OAuth 登入資料不存在或已過期"));
                }

                Object value = session.getAttribute(
                                OAuth2LoginSuccessHandler.SESSION_ATTRIBUTE);

                if (!(value instanceof LoginResponseDTO loginResult)) {
                        return ResponseEntity
                                        .status(HttpStatus.UNAUTHORIZED)
                                        .body(ApiResponse.error(
                                                        HttpStatus.UNAUTHORIZED.value(),
                                                        "OAuth 登入資料不存在或已領取"));
                }
                String refreshToken = refreshTokenService.createRefreshToken(
                                loginResult.userId());

                refreshTokenCookieUtility.addCookie(
                                response,
                                refreshToken);

                // 登入結果只能領取一次
                session.removeAttribute(
                                OAuth2LoginSuccessHandler.SESSION_ATTRIBUTE);

                // 領取完成後不再使用 OAuth Session
                session.invalidate();

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                HttpStatus.OK.value(),
                                                "Google 登入成功",
                                                loginResult));
        }
}