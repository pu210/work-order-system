package com.eeit219.work_order_system.modules.a.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.eeit219.work_order_system.common.response.ApiResponse;
import com.eeit219.work_order_system.common.security.JsonWebTokenUtility;
import com.eeit219.work_order_system.modules.a.dto.CurrentUserDTO;
import com.eeit219.work_order_system.modules.a.dto.LoginRequestDTO;
import com.eeit219.work_order_system.modules.a.dto.LoginResponseDTO;
import com.eeit219.work_order_system.modules.a.service.UserService;

import org.json.JSONObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class LoginController {

        private final UserService userService;
        private final JsonWebTokenUtility jwtUtil;

        public LoginController(UserService userService, JsonWebTokenUtility jwtUtil) {
                this.userService = userService;
                this.jwtUtil = jwtUtil;
        }

        @PostMapping("/auth/login")
        public ResponseEntity<ApiResponse<LoginResponseDTO>> login(
                        @RequestBody LoginRequestDTO request) {
                if (request.account() == null || request.account().isBlank()
                                || request.password() == null || request.password().isBlank()) {
                        return ResponseEntity.badRequest().body(
                                        ApiResponse.error(
                                                        HttpStatus.BAD_REQUEST.value(),
                                                        "請輸入帳號與密碼"));
                }

                CurrentUserDTO bean = userService.loginUser(
                                request.account(),
                                request.password());

                if (bean == null) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                                        ApiResponse.error(
                                                        HttpStatus.UNAUTHORIZED.value(),
                                                        "帳號或密碼錯誤"));
                }

                JSONObject user = new JSONObject()
                                .put("userId", bean.userId())
                                .put("account", bean.account())
                                .put("name", bean.name())
                                .put("mustChangePassword", bean.mustChangePassword())
                                .put("roleCodes", bean.roleCodes());

                String token = jwtUtil.createToken(user.toString());
                LoginResponseDTO data = new LoginResponseDTO(
                                token, bean.account(),
                                bean.userId(), bean.name(), bean.email(), bean.roleCodes(), bean.mustChangePassword());

                return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "登入成功", data));
        }

}
