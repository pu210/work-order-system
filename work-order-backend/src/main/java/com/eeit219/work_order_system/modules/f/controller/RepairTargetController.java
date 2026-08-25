package com.eeit219.work_order_system.modules.f.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.eeit219.work_order_system.common.response.ApiResponse; // 引入統一回應類別
import com.eeit219.work_order_system.modules.f.dto.RepairTargetRequestDto;
import com.eeit219.work_order_system.modules.f.dto.RepairTargetResponseDto;
import com.eeit219.work_order_system.modules.f.entity.RepairTarget;
import com.eeit219.work_order_system.modules.f.service.RepairTargetService;

@RestController
@RequestMapping("/api/repair-targets")
public class RepairTargetController {

    @Autowired
    private RepairTargetService repairTargetService;

    // 取得全部或透過關鍵字搜尋維修目標 (支援 keyword 參數)
    @GetMapping
    public ApiResponse<List<RepairTargetResponseDto>> getAllOrSearchRepairTargets(
            @RequestParam(required = false) String keyword) {
        List<RepairTargetResponseDto> result = repairTargetService.searchRepairTargets(keyword);
        return ApiResponse.success(200, "查詢成功", result);
    }

    // 新增維修目標
    @PostMapping
    public ApiResponse<RepairTarget> createRepairTarget(@RequestBody RepairTargetRequestDto requestDto) {
        RepairTarget saved = repairTargetService.createRepairTarget(requestDto);
        return ApiResponse.success(200, "新增維修目標成功", saved);
    }

    // 修改維修目標
    @PutMapping("/{id}")
    public ApiResponse<RepairTarget> updateRepairTarget(
            @PathVariable("id") Integer id,
            @RequestBody RepairTargetRequestDto requestDto) {
        RepairTarget updated = repairTargetService.updateRepairTarget(id, requestDto);
        return ApiResponse.success(200, "更新維修目標成功", updated);
    }

    // 狀態開關切換 (啟用/停用)
    @PatchMapping("/{id}/status")
    public ApiResponse<RepairTarget> updateStatus(
            @PathVariable("id") Integer id,
            @RequestParam("status") Boolean status) {
        RepairTarget updated = repairTargetService.updateStatus(id, status);
        return ApiResponse.success(200, "更新維修目標狀態成功", updated);
    }
}