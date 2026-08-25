package com.eeit219.work_order_system.modules.c.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eeit219.work_order_system.common.response.ApiResponse;
import com.eeit219.work_order_system.modules.c.dto.EquipmentHistoryResponse;
import com.eeit219.work_order_system.modules.c.service.EquipmentHistoryService;

@RestController
@RequestMapping("/api/equipment")
public class EquipmentHistoryController {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final EquipmentHistoryService equipmentHistoryService;

    public EquipmentHistoryController(
            EquipmentHistoryService equipmentHistoryService
    ) {
        this.equipmentHistoryService = equipmentHistoryService;
    }

    /**
     * 查詢指定設備的歷史工單。
     *
     * 範例：
     * GET /api/equipment/PC-001/work-orders?page=0&size=20
     */
    @GetMapping("/{targetNo}/work-orders")
    public ResponseEntity<ApiResponse<EquipmentHistoryResponse>> getHistory(
            @PathVariable String targetNo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + DEFAULT_PAGE_SIZE) int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(
                Math.max(size, 1),
                MAX_PAGE_SIZE
        );

        Pageable pageable = PageRequest.of(
                safePage,
                safeSize
        );

        EquipmentHistoryResponse response =
                equipmentHistoryService.getHistory(
                        targetNo,
                        pageable
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "設備歷史工單查詢成功",
                        response
                )
        );
    }
}