package com.eeit219.work_order_system.modules.F.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eeit219.work_order_system.common.response.ApiResponse;
import com.eeit219.work_order_system.modules.F.entity.Priority;
import com.eeit219.work_order_system.modules.F.repository.PriorityRepository;

@RestController
@RequestMapping("/api/priorities")
public class PriorityController {

    @Autowired
    private PriorityRepository priorityRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Priority>>> getAllOrSearchPriorities(
            @RequestParam(required = false) String keyword) {
        try {
            List<Priority> list;
            if (keyword != null && !keyword.trim().isEmpty()) {
                list = priorityRepository.searchByKeyword(keyword);

                // 🌟 關鍵：如果關鍵字搜尋結果為空，主動拋出例外讓 catch 攔截
                if (list.isEmpty()) {
                    throw new RuntimeException("找不到符合條件的優先級");
                }
            } else {
                list = priorityRepository.findAll();
            }

            ApiResponse<List<Priority>> response = ApiResponse.success(HttpStatus.OK.value(), "查詢優先級成功", list);
            return ResponseEntity.ok(response);

        } catch (RuntimeException ex) {
            // 當查不到資料拋出例外時，會自動被這裡抓到並轉成失敗格式
            return errorResponse(HttpStatus.UNAUTHORIZED.value(), ex.getMessage());
        } catch (Exception ex) {
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error");
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Object>> createPriority(@RequestBody Priority priority) {
        try {
            Priority savedPriority = priorityRepository.save(priority);
            ApiResponse<Object> response = ApiResponse.success(HttpStatus.CREATED.value(), "新增優先級成功", savedPriority);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception ex) {
            return errorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        }
    }

    @PutMapping("/{prioritiesId}")
    public ResponseEntity<ApiResponse<Object>> updatePriority(@PathVariable Integer prioritiesId,
            @RequestBody Priority priorityDetails) {
        try {
            Priority priority = priorityRepository.findById(prioritiesId)
                    .orElseThrow(() -> new RuntimeException("找不到該優先級 ID: " + prioritiesId));

            priority.setName(priorityDetails.getName());
            priority.setHours(priorityDetails.getHours());
            priority.setStatus(priorityDetails.getStatus());

            Priority updatedPriority = priorityRepository.save(priority);
            ApiResponse<Object> response = ApiResponse.success(HttpStatus.OK.value(), "更新優先級成功", updatedPriority);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            // 對應 401 或 400 失敗格式
            return errorResponse(HttpStatus.UNAUTHORIZED.value(), ex.getMessage());
        } catch (Exception ex) {
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error");
        }
    }

    @PatchMapping("/{prioritiesId}/status")
    public ResponseEntity<ApiResponse<Object>> updateStatus(@PathVariable Integer prioritiesId,
            @RequestParam Boolean status) {
        try {
            Priority priority = priorityRepository.findById(prioritiesId)
                    .orElseThrow(() -> new RuntimeException("找不到該優先級 ID: " + prioritiesId));

            priority.setStatus(status);
            Priority updatedPriority = priorityRepository.save(priority);
            ApiResponse<Object> response = ApiResponse.success(HttpStatus.OK.value(), "更新優先級狀態成功", updatedPriority);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            return errorResponse(HttpStatus.UNAUTHORIZED.value(), ex.getMessage());
        } catch (Exception ex) {
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error");
        }
    }

    private <T> ResponseEntity<ApiResponse<T>> errorResponse(int status, String message) {
        ApiResponse<T> response = ApiResponse.error(status, message);
        return ResponseEntity.status(status).body(response);
    }

}