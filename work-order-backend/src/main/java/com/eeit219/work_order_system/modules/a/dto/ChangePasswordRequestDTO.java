package com.eeit219.work_order_system.modules.a.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequestDTO(
                @NotBlank(message = "目前密碼為必填") String currentPassword,

                @NotBlank(message = "新密碼為必填") @Size(min = 8, message = "新密碼至少需要 8 個字元") String newPassword,

                @NotBlank(message = "確認密碼為必填") String confirmPassword) {

}
