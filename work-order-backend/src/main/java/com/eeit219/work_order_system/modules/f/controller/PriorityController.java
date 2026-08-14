package com.eeit219.work_order_system.modules.f.controller;

import java.util.List;
import java.util.stream.Collectors;

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
import com.eeit219.work_order_system.modules.f.dto.PriorityRequestDto;
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
    private PriorityService priorityService; // 注入 Service 來處理轉換與商業邏輯

    @GetMapping
    public ResponseEntity<ApiResponse<List<PriorityResponseDto>>> getAllOrSearchPriorities(
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

            // 將 Entity 列表轉換為 ResponseDto 列表
            List<PriorityResponseDto> dtoList = list.stream()
                    .map(priorityService::convertToResponseDto)
                    .collect(Collectors.toList());

            ApiResponse<List<PriorityResponseDto>> response = ApiResponse.success(HttpStatus.OK.value(), "查詢優先級成功", dtoList);
            return ResponseEntity.ok(response);

        } catch (RuntimeException ex) {
            // 當查不到資料拋出例外時，會自動被這裡抓到並轉成失敗格式
            return errorResponse(HttpStatus.UNAUTHORIZED.value(), ex.getMessage());
        } catch (Exception ex) {
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error");
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PriorityResponseDto>> createPriority(@RequestBody PriorityRequestDto request) {
        try {
            // 透過 Service 進行新增
            Priority savedPriority = priorityService.createPriority(request);
            // 轉成 ResponseDto 確保格式乾淨
            PriorityResponseDto responseDto = priorityService.convertToResponseDto(savedPriority);

            ApiResponse<PriorityResponseDto> response = ApiResponse.success(HttpStatus.CREATED.value(), "新增優先級成功", responseDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception ex) {
            return errorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        }
    }

    @PutMapping("/{prioritiesId}")
    public ResponseEntity<ApiResponse<PriorityResponseDto>> updatePriority(@PathVariable Integer prioritiesId,
            @RequestBody PriorityRequestDto request) {
        try {
            // 透過 Service 進行更新
            Priority updatedPriority = priorityService.updatePriority(prioritiesId, request);
            // 轉成 ResponseDto
            PriorityResponseDto responseDto = priorityService.convertToResponseDto(updatedPriority);

            ApiResponse<PriorityResponseDto> response = ApiResponse.success(HttpStatus.OK.value(), "更新優先級成功", responseDto);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            // 對應 401 或 400 失敗格式
            return errorResponse(HttpStatus.UNAUTHORIZED.value(), ex.getMessage());
        } catch (Exception ex) {
            return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error");
        }
    }

    @PatchMapping("/{prioritiesId}/status")
    public ResponseEntity<ApiResponse<PriorityResponseDto>> updateStatus(@PathVariable Integer prioritiesId,
            @RequestParam Boolean status) {
        try {
            Priority priority = priorityRepository.findById(prioritiesId)
                    .orElseThrow(() -> new RuntimeException("找不到該優先級 ID: " + prioritiesId));

            priority.setStatus(status);
            Priority updatedPriority = priorityRepository.save(priority);
            PriorityResponseDto responseDto = priorityService.convertToResponseDto(updatedPriority);

            ApiResponse<PriorityResponseDto> response = ApiResponse.success(HttpStatus.OK.value(), "更新優先級狀態成功", responseDto);
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
