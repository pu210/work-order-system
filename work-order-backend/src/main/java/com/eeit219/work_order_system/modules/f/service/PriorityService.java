package com.eeit219.work_order_system.modules.f.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eeit219.work_order_system.modules.f.dto.PriorityRequestDto;
import com.eeit219.work_order_system.modules.f.dto.PriorityResponseDto;
import com.eeit219.work_order_system.modules.f.entity.Priority;
import com.eeit219.work_order_system.modules.f.repository.PriorityRepository;

@Service
public class PriorityService {

    @Autowired
    private PriorityRepository priorityRepository;

    // Entity 轉 ResponseDto
    public PriorityResponseDto convertToResponseDto(Priority priority) {
        if (priority == null) {
            return null;
        }
        PriorityResponseDto dto = new PriorityResponseDto();
        dto.setPrioritiesId(priority.getPrioritiesId());
        dto.setName(priority.getName());
        dto.setHours(priority.getHours());
        dto.setStatus(priority.getStatus());
        return dto;

    }

    // 商業邏輯：新增
    public Priority createPriority(PriorityRequestDto request) {
        Priority priority = new Priority();
        priority.setName(request.getName());
        priority.setHours(request.getHours());
        priority.setStatus(request.getStatus() != null ? request.getStatus() : true);
        return priorityRepository.save(priority);
    }

    // 商業邏輯：修改
    public Priority updatePriority(Integer prioritiesId, PriorityRequestDto request) {
        Priority priority = priorityRepository.findById(prioritiesId)
                .orElseThrow(() -> new RuntimeException("找不到該優先級 ID: " + prioritiesId));

        priority.setName(request.getName());
        priority.setHours(request.getHours());
        if (request.getStatus() != null) {
            priority.setStatus(request.getStatus());
        }
        return priorityRepository.save(priority);
    }
}
