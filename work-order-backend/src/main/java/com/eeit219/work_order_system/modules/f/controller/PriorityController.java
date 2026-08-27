package com.eeit219.work_order_system.modules.f.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eeit219.work_order_system.common.response.ApiResponse; // 引入統一回應類別
import com.eeit219.work_order_system.modules.f.dto.PriorityResponseDto;
import com.eeit219.work_order_system.modules.f.entity.Priority;
import com.eeit219.work_order_system.modules.f.repository.PriorityRepository;
import com.eeit219.work_order_system.modules.f.service.PriorityService;

@RestController
@RequestMapping("/api/priorities")
public class PriorityController {

    @Autowired
    private PriorityRepository priorityRepository;
    @Autowired
    private PriorityService priorityService;

    @GetMapping
    public ApiResponse<List<Priority>> getAllOrSearchPriorities(@RequestParam(required = false) String keyword) {
        List<Priority> result;
        if (keyword != null && !keyword.trim().isEmpty()) {
            result = priorityRepository.searchByKeyword(keyword);
        } else {
            result = priorityRepository.findAll();
        }
        return ApiResponse.success(200, "查詢成功", result);
    }

    @GetMapping("/active")
    public ApiResponse<List<PriorityResponseDto>> getActivePriorities() {
        List<PriorityResponseDto> result = priorityService.getActivePriorities();
        return ApiResponse.success(200, "查詢啟用中的優先級成功", result);
    }

    // B 模組用：工單列表篩選下拉選單，只回傳啟用中（status = true）的資料。
    // 故意不透過 PriorityService，直接查 priorityRepository —— 核心邏輯常在異動，
    // 所以這支路徑跟 getActivePriorities() 分開，改自己打一隻。
    @GetMapping("/active-for-b")
    public ApiResponse<List<Priority>> getActivePrioritiesForB() {
        List<Priority> data = priorityRepository.findByStatusTrue();
        return ApiResponse.success(HttpStatus.OK.value(), "成功", data);
    }

    @PostMapping
    public ApiResponse<Priority> createPriority(@RequestBody Priority priority) {
        if (priority.getStatus() == null) {
            priority.setStatus(true);
        }
        Priority saved = priorityRepository.save(priority);
        return ApiResponse.success(200, "新增優先級成功", saved);
    }

    @PutMapping("/{prioritiesId}")
    public ApiResponse<Priority> updatePriority(@PathVariable Integer prioritiesId,
            @RequestBody Priority priorityDetails) {
        Priority priority = priorityRepository.findById(prioritiesId)
                .orElseThrow(() -> new RuntimeException("找不到該優先級 ID: " + prioritiesId));

        priority.setName(priorityDetails.getName());
        priority.setHours(priorityDetails.getHours());

        // 🌟 加上防呆：有傳 status 才更新，沒傳就維持原樣
        if (priorityDetails.getStatus() != null) {
            priority.setStatus(priorityDetails.getStatus());
        }

        Priority updated = priorityRepository.save(priority);
        return ApiResponse.success(200, "更新優先級成功", updated);
    }

    @PatchMapping("/{prioritiesId}/status")
    public ApiResponse<Priority> updateStatus(@PathVariable Integer prioritiesId, @RequestParam Boolean status) {
        Priority priority = priorityRepository.findById(prioritiesId)
                .orElseThrow(() -> new RuntimeException("找不到該優先級 ID: " + prioritiesId));

        priority.setStatus(status);
        Priority updated = priorityRepository.save(priority);
        return ApiResponse.success(200, "更新優先級狀態成功", updated);
    }
}