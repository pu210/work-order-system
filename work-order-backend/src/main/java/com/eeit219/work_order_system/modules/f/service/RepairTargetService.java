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

    // 新增商業邏輯 (預設 status 若沒傳則給 true)
    public RepairTarget createRepairTarget(RepairTargetRequestDto request) {
        RepairTarget target = new RepairTarget();
        target.setTargetNo(request.getTargetNo());
        target.setName(request.getName());
        target.setModel(request.getModel());
        target.setStatus(request.getStatus() != null ? request.getStatus() : true);

        return repairTargetRepository.save(target);
    }

    // 修改商業邏輯
    public RepairTarget updateRepairTarget(Integer targetId, RepairTargetRequestDto request) {
        RepairTarget target = repairTargetRepository.findById(targetId)
                .orElseThrow(() -> new RuntimeException("找不到該維修目標 ID: " + targetId));

        target.setTargetNo(request.getTargetNo());
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
}