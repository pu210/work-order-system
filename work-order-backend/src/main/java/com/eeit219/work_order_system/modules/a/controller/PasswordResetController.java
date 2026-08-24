package com.eeit219.work_order_system.modules.a.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eeit219.work_order_system.common.response.ApiResponse;
import com.eeit219.work_order_system.modules.a.dto.ForgotPasswordRequestDTO;
import com.eeit219.work_order_system.modules.a.dto.ResetPasswordRequestDTO;
import com.eeit219.work_order_system.modules.a.service.PasswordResetService;

@RestController
@RequestMapping("/api/auth")
public class PasswordResetController {
        private final PasswordResetService passwordResetService;

        public PasswordResetController(
                        PasswordResetService passwordResetService) {
                this.passwordResetService = passwordResetService;
        }

        @PostMapping("/forgot-password")
        public ResponseEntity<ApiResponse<Void>> forgotPassword(
                        @RequestBody ForgotPasswordRequestDTO request) {

                passwordResetService.requestPasswordReset(request);

                return ResponseEntity.status(HttpStatus.OK).body(
                                ApiResponse.success(
                                                HttpStatus.OK.value(),
                                                "若此 Email 與有效帳號相符，我們將寄送密碼重設信件，請檢查您的信箱。", null));
        }

        @PostMapping("/reset-password")
        public ResponseEntity<ApiResponse<Void>> resetPassword(
                        @RequestBody ResetPasswordRequestDTO request) {

                passwordResetService.resetPassword(request);

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                HttpStatus.OK.value(),
                                                "密碼重設成功",
                                                null));
        }

}
