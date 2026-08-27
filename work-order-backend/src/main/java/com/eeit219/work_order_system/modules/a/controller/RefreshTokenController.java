package com.eeit219.work_order_system.modules.a.controller;

import java.util.Optional;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eeit219.work_order_system.common.response.ApiResponse;
import com.eeit219.work_order_system.common.security.JsonWebTokenUtility;
import com.eeit219.work_order_system.common.security.RefreshTokenCookieUtility;
import com.eeit219.work_order_system.modules.a.dto.CurrentUserDTO;
import com.eeit219.work_order_system.modules.a.dto.LoginResponseDTO;
import com.eeit219.work_order_system.modules.a.service.RefreshTokenService;
import com.eeit219.work_order_system.modules.a.service.RefreshTokenService.RotationResult;
import com.eeit219.work_order_system.modules.a.service.UserService;

import jakarta.servlet.http.HttpServletResponse;

@RestController
public class RefreshTokenController {

        private final RefreshTokenService refreshTokenService;
        private final RefreshTokenCookieUtility refreshTokenCookieUtility;
        private final UserService userService;
        private final JsonWebTokenUtility jwtUtility;

        public RefreshTokenController(
                        RefreshTokenService refreshTokenService,
                        RefreshTokenCookieUtility refreshTokenCookieUtility,
                        UserService userService,
                        JsonWebTokenUtility jwtUtility) {
                this.refreshTokenService = refreshTokenService;
                this.refreshTokenCookieUtility = refreshTokenCookieUtility;
                this.userService = userService;
                this.jwtUtility = jwtUtility;
        }

        @PostMapping("/api/auth/refresh")
        public ResponseEntity<ApiResponse<LoginResponseDTO>> refresh(
                        @CookieValue(name = RefreshTokenCookieUtility.COOKIE_NAME, required = false) String rawRefreshToken,
                        HttpServletResponse response) {

                Optional<RotationResult> optionalResult = refreshTokenService.rotateRefreshToken(rawRefreshToken);

                if (optionalResult.isEmpty()) {
                        refreshTokenCookieUtility.clearCookie(response);

                        return ResponseEntity
                                        .status(HttpStatus.UNAUTHORIZED)
                                        .body(ApiResponse.error(
                                                        HttpStatus.UNAUTHORIZED.value(),
                                                        "Refresh Token 無效或已過期"));
                }

                RotationResult result = optionalResult.get();

                CurrentUserDTO user;

                try {
                        user = userService.getCurrentUserForRefresh(
                                        result.userId());
                } catch (IllegalArgumentException exception) {
                        refreshTokenService.revokeRefreshToken(
                                        result.refreshToken());
                        refreshTokenCookieUtility.clearCookie(response);

                        return ResponseEntity
                                        .status(HttpStatus.UNAUTHORIZED)
                                        .body(ApiResponse.error(
                                                        HttpStatus.UNAUTHORIZED.value(),
                                                        "使用者不存在或帳號已停用"));
                }

                JSONObject tokenData = new JSONObject()
                                .put("userId", user.userId())
                                .put("account", user.account())
                                .put("name", user.name())
                                .put(
                                                "mustChangePassword",
                                                user.mustChangePassword())
                                .put("roleCodes", user.roleCodes());

                String accessToken = jwtUtility.createToken(tokenData.toString());

                LoginResponseDTO data = new LoginResponseDTO(
                                accessToken,
                                user.account(),
                                user.userId(),
                                user.name(),
                                user.email(),
                                user.roleCodes(),
                                user.mustChangePassword());

                refreshTokenCookieUtility.addCookie(
                                response,
                                result.refreshToken());

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                HttpStatus.OK.value(),
                                                "Token 刷新成功",
                                                data));
        }

        @PostMapping("/api/auth/logout")
        public ResponseEntity<ApiResponse<Void>> logout(
                        @CookieValue(name = RefreshTokenCookieUtility.COOKIE_NAME, required = false) String rawRefreshToken,
                        HttpServletResponse response) {

                refreshTokenService.revokeRefreshToken(
                                rawRefreshToken);

                refreshTokenCookieUtility.clearCookie(response);

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                HttpStatus.OK.value(),
                                                "登出成功",
                                                null));
        }
}