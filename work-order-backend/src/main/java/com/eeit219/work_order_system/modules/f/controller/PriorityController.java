package com.eeit219.work_order_system.modules.f.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eeit219.work_order_system.modules.f.entity.Priority;
import com.eeit219.work_order_system.modules.f.repository.PriorityRepository;

@RestController
@RequestMapping("/api/priorities")
public class PriorityController {

    @Autowired
    private PriorityRepository priorityRepository;

    @GetMapping
    public List<Priority> getAllOrSearchPriorities(@RequestParam(required = false) String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return priorityRepository.searchByKeyword(keyword);
        } else {
            return priorityRepository.findAll();
        }
    }

    @PostMapping
    public Priority createPriority(@RequestBody Priority priority) {
        if (priority.getStatus() == null) {
            priority.setStatus(true);
        }
        return priorityRepository.save(priority);
    }

    @PutMapping("/{prioritiesId}")
    public Priority updatePriority(@PathVariable Integer prioritiesId, @RequestBody Priority priorityDetails) {
        Priority priority = priorityRepository.findById(prioritiesId)
                .orElseThrow(() -> new RuntimeException("找不到該優先級 ID: " + prioritiesId));

        priority.setName(priorityDetails.getName());
        priority.setHours(priorityDetails.getHours());

        // 🌟 加上防呆：有傳 status 才更新，沒傳就維持原樣
        if (priorityDetails.getStatus() != null) {
            priority.setStatus(priorityDetails.getStatus());
        }

        return priorityRepository.save(priority);
    }

    @PatchMapping("/{prioritiesId}/status")
    public Priority updateStatus(@PathVariable Integer prioritiesId, @RequestParam Boolean status) {
        Priority priority = priorityRepository.findById(prioritiesId)
                .orElseThrow(() -> new RuntimeException("找不到該優先級 ID: " + prioritiesId));

        priority.setStatus(status);
        return priorityRepository.save(priority);
    }
}
