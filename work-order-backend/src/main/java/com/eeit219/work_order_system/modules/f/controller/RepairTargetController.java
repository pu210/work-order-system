package com.eeit219.work_order_system.modules.f.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<List<RepairTargetResponseDto>> getAllOrSearchRepairTargets(
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(repairTargetService.searchRepairTargets(keyword));
    }

    // 新增維修目標
    @PostMapping
    public ResponseEntity<RepairTarget> createRepairTarget(@RequestBody RepairTargetRequestDto requestDto) {
        return ResponseEntity.ok(repairTargetService.createRepairTarget(requestDto));
    }

    // 修改維修目標
    @PutMapping("/{id}")
    public ResponseEntity<RepairTarget> updateRepairTarget(
            @PathVariable("id") Integer id,
            @RequestBody RepairTargetRequestDto requestDto) {
        return ResponseEntity.ok(repairTargetService.updateRepairTarget(id, requestDto));
    }

    // 狀態開關切換 (啟用/停用)
    @PatchMapping("/{id}/status")
    public ResponseEntity<RepairTarget> updateStatus(
            @PathVariable("id") Integer id,
            @RequestParam("status") Boolean status) {
        return ResponseEntity.ok(repairTargetService.updateStatus(id, status));
    }
}