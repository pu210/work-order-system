package com.eeit219.work_order_system.modules.c.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eeit219.work_order_system.common.response.ApiResponse;
import com.eeit219.work_order_system.common.security.AuthenticatedUser;
import com.eeit219.work_order_system.modules.c.dto.AdminArchiveSuggestionResponse;
import com.eeit219.work_order_system.modules.c.service.AdminArchiveSuggestionService;

@RestController
@RequestMapping("/api/work-orders")
public class AdminArchiveSuggestionController {

    private final AdminArchiveSuggestionService suggestionService;

    public AdminArchiveSuggestionController(AdminArchiveSuggestionService suggestionService) {
        this.suggestionService = suggestionService;
    }

    @PostMapping("/{workOrderId}/admin-check/archive-suggestion")
    public ResponseEntity<ApiResponse<AdminArchiveSuggestionResponse>> generate(
            @PathVariable Integer workOrderId,
            @AuthenticationPrincipal AuthenticatedUser loginUser) {
        AdminArchiveSuggestionResponse suggestion = suggestionService.generate(
                workOrderId,
                loginUser.userId());
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                "AI 歸檔建議已產生",
                suggestion));
    }
}
