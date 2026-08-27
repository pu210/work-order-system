package com.eeit219.work_order_system.modules.f.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eeit219.work_order_system.modules.f.dto.RepairTargetRequestDto;
import com.eeit219.work_order_system.modules.f.dto.RepairTargetResponseDto;
import com.eeit219.work_order_system.modules.f.entity.RepairTarget;
import com.eeit219.work_order_system.modules.f.repository.RepairTargetRepository;

@Service
public class RepairTargetService {

    @Autowired
    private RepairTargetRepository repairTargetRepository;

    // Entity 轉 ResponseDto
    public RepairTargetResponseDto convertToResponseDto(RepairTarget target) {
        if (target == null) {
            return null;
        }

        RepairTargetResponseDto dto = new RepairTargetResponseDto();
        dto.setTargetId(target.getTargetId());
        dto.setTargetNo(target.getTargetNo());
        dto.setName(target.getName());
        dto.setModel(target.getModel());
        dto.setStatus(target.getStatus());

        return dto;
    }

    // 查詢全部並轉成 DTO 列表
    public List<RepairTargetResponseDto> getAllRepairTargets() {
        return repairTargetRepository.findAll().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    // 關鍵字搜尋 (使用安全的模糊比對)
    public List<RepairTargetResponseDto> searchRepairTargets(String keyword) {
        List<RepairTarget> list;
        if (keyword == null || keyword.trim().isEmpty()) {
            list = repairTargetRepository.findAll();
        } else {
            list = repairTargetRepository.searchByKeyword(keyword);
        }
        return list.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    // 新增商業邏輯 (自動產生 10 位數亂碼作為設備編號)
    public RepairTarget createRepairTarget(RepairTargetRequestDto request) {
        RepairTarget target = new RepairTarget();

        // 核心修改：自動產生 10 位數隨機亂碼並賦值
        String randomTargetNo = generateRandomString(10);
        target.setTargetNo(randomTargetNo);

        target.setName(request.getName());
        target.setModel(request.getModel());
        target.setStatus(request.getStatus() != null ? request.getStatus() : true);

        return repairTargetRepository.save(target);
    }

    // 修改商業邏輯 (設備編號建立後維持不變，故移除 targetNo 的修改)
    public RepairTarget updateRepairTarget(Integer targetId, RepairTargetRequestDto request) {
        RepairTarget target = repairTargetRepository.findById(targetId)
                .orElseThrow(() -> new RuntimeException("找不到該維修目標 ID: " + targetId));

        target.setName(request.getName());
        target.setModel(request.getModel());
        if (request.getStatus() != null) {
            target.setStatus(request.getStatus());
        }

        return repairTargetRepository.save(target);
    }

    // 狀態開關切換 (軟刪除 / 停用)
    public RepairTarget updateStatus(Integer targetId, Boolean status) {
        RepairTarget target = repairTargetRepository.findById(targetId)
                .orElseThrow(() -> new RuntimeException("找不到該維修目標 ID: " + targetId));

        target.setStatus(status);
        return repairTargetRepository.save(target);
    }

    // 輔助方法：產生指定長度的隨機英數字串（大小寫英文＋數字）
    private String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }
        return sb.toString();
    }
}