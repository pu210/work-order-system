package com.eeit219.work_order_system.modules.a.dto;

import java.util.List;

// 只給前端判斷登入後流程需要的欄位，不包含密碼等敏感資料

public record CurrentUserDTO(
                Integer userId,
                String account,
                String name,
                String email,
                Boolean mustChangePassword,
                List<String> roleCodes) {
}
