package com.eeit219.work_order_system.modules.a.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eeit219.work_order_system.common.response.ApiResponse;
import com.eeit219.work_order_system.modules.a.dto.ChangePasswordRequestDTO;
import com.eeit219.work_order_system.modules.a.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/account")
public class AccountController {
    private final UserService userService;

    public AccountController(UserService userService) {
        this.userService = userService;
    }

    // 使用者更新初始密碼
    @PatchMapping("/initial-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequestDTO request) {

        userService.changeInitialPassword(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "密碼修改成功",
                        null));
    }

}
