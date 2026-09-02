package com.eeit219.work_order_system.modules.a.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eeit219.work_order_system.common.response.ApiResponse;
import com.eeit219.work_order_system.modules.a.dto.CreateUserRequestDTO;
import com.eeit219.work_order_system.modules.a.dto.CreateUserResponseDTO;
import com.eeit219.work_order_system.modules.a.dto.PageResponseDTO;
import com.eeit219.work_order_system.modules.a.dto.ReviewUserRequestDTO;
import com.eeit219.work_order_system.modules.a.dto.ReviewUserResponseDTO;
import com.eeit219.work_order_system.modules.a.dto.UpdateUserRequestDTO;
import com.eeit219.work_order_system.modules.a.dto.UpdateUserResponseDTO;
import com.eeit219.work_order_system.modules.a.dto.UserResponseDTO;
import com.eeit219.work_order_system.modules.a.service.UserService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

        private final UserService userService;

        public UserController(UserService userService) {
                this.userService = userService;
        }

        // 建立使用者
        @PostMapping
        public ResponseEntity<ApiResponse<CreateUserResponseDTO>> createUser(
                        @Valid @RequestBody CreateUserRequestDTO request) {

                CreateUserResponseDTO data = userService.createUser(request);

                return ResponseEntity.status(HttpStatus.CREATED).body(
                                ApiResponse.success(
                                                HttpStatus.CREATED.value(),
                                                "使用者新增成功",
                                                data));
        }

        // 更新使用者
        @PatchMapping("/{userId}")
        public ResponseEntity<ApiResponse<UpdateUserResponseDTO>> updateUser(
                        @PathVariable Integer userId,
                        @RequestBody UpdateUserRequestDTO request) {

                UpdateUserResponseDTO data = userService.updateUser(userId, request);

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                HttpStatus.OK.value(),
                                                "使用者資料更新成功",
                                                data));
        }

        // 查詢使用者（一筆）
        @GetMapping("/{userId}")
        public ResponseEntity<ApiResponse<UserResponseDTO>> getUser(
                        @PathVariable Integer userId) {

                UserResponseDTO data = userService.getUser(userId);

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                HttpStatus.OK.value(),
                                                "使用者查詢成功",
                                                data));
        }

        // 分頁與條件查詢使用者
        @GetMapping
        public ResponseEntity<ApiResponse<PageResponseDTO<UserResponseDTO>>> searchUsers(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(required = false) String keyword,
                        @RequestParam(required = false) Byte status,
                        @RequestParam(required = false) String roleCode) {
                PageResponseDTO<UserResponseDTO> data = userService.searchUsers(
                                page, size, keyword, status, roleCode);

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                HttpStatus.OK.value(),
                                                "使用者分頁查詢成功",
                                                data));

        }

        // 管理員審核使用者自行註冊
        @PatchMapping("/{userId}/approval")
        public ResponseEntity<ApiResponse<ReviewUserResponseDTO>> reviewRegistration(
                        @PathVariable Integer userId,
                        @RequestBody ReviewUserRequestDTO request) {

                ReviewUserResponseDTO data = userService.reviewRegistration(userId, request);

                String message = Boolean.TRUE.equals(request.approved())
                                ? "使用者帳號核准成功"
                                : "使用者帳號已拒絕";

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                HttpStatus.OK.value(),
                                                message,
                                                data));
        }
}
