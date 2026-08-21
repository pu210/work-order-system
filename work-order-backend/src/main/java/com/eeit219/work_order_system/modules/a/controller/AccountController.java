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
import org.springframework.web.bind.annotation.GetMapping;
import com.eeit219.work_order_system.modules.a.dto.ProfileResponseDTO;
import com.eeit219.work_order_system.modules.a.dto.UpdateProfileRequestDTO;

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

    // 使用者查詢個人資料
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<ProfileResponseDTO>> getProfile() {
        ProfileResponseDTO data = userService.getCurrentUserProfile();

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "個人資料查詢成功",
                        data));
    }

    // 使用者更新個人資料
    @PatchMapping("/profile")
    public ResponseEntity<ApiResponse<ProfileResponseDTO>> updateProfile(
            @RequestBody UpdateProfileRequestDTO request) {

        ProfileResponseDTO data = userService.updateCurrentUserProfile(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "個人資料更新成功",
                        data));
    }

    // 使用者修改自己密碼
    @PatchMapping("/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(
            @Valid @RequestBody ChangePasswordRequestDTO request) {

        userService.changePassword(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "密碼修改成功",
                        null));
    }

}
