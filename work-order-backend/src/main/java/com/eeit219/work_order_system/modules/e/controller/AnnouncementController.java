package com.eeit219.work_order_system.modules.e.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eeit219.work_order_system.modules.e.entity.Announcement;
import com.eeit219.work_order_system.modules.e.service.AnnouncementService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/announcements")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    // 1. 取得所有公告列表：GET http://localhost:8080/api/announcements
    @GetMapping
    public List<Announcement> getAllAnnouncements() {
        return announcementService.getAllAnnouncements();
    }

    // 2. 依 ID 取得單筆公告：GET http://localhost:8080/api/announcements/1
    @GetMapping("/{id}")
    public ResponseEntity<Announcement> getAnnouncementById(@PathVariable Integer id) {
        return announcementService.getAnnouncementById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. 依分類取得公告：GET http://localhost:8080/api/announcements/category/MAINTENANCE
    @GetMapping("/category/{category}")
    public List<Announcement> getAnnouncementsByCategory(@PathVariable String category) {
        return announcementService.getAnnouncementsByCategory(category);
    }

    // 4. 發布新公告：POST http://localhost:8080/api/announcements
    @PostMapping
    public Announcement createAnnouncement(@RequestBody Announcement announcement) {
        return announcementService.createAnnouncement(announcement);
    }

    // 5. 修改公告：PUT http://localhost:8080/api/announcements/1
    @PutMapping("/{id}")
    public ResponseEntity<Announcement> updateAnnouncement(
            @PathVariable Integer id,
            @RequestBody Announcement announcement) {
        return ResponseEntity.ok(announcementService.updateAnnouncement(id, announcement));
    }

    // 6. 刪除公告：DELETE http://localhost:8080/api/announcements/1
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnnouncement(@PathVariable Integer id) {
        announcementService.deleteAnnouncement(id);
        return ResponseEntity.noContent().build();
    }
}
