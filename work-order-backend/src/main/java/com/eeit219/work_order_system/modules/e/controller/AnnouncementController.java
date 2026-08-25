package com.eeit219.work_order_system.modules.e.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
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

import com.eeit219.work_order_system.common.response.ApiResponse;
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
    public ResponseEntity<ApiResponse<List<Announcement>>> getAllAnnouncements() {
        List<Announcement> list = announcementService.getAllAnnouncements();
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "查詢所有公告成功", list));
    }

    // 2. 依 ID 取得單筆公告：GET http://localhost:8080/api/announcements/1
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Announcement>> getAnnouncementById(@PathVariable Integer id) {
        return announcementService.getAnnouncementById(id)
                .map(announcement -> ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "查詢公告成功", announcement)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), "找不到該筆公告")));
    }

    // 3. 依分類取得公告：GET http://localhost:8080/api/announcements/category/{category}
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<Announcement>>> getAnnouncementsByCategory(@PathVariable String category) {
        List<Announcement> list = announcementService.getAnnouncementsByCategory(category);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "依分類查詢公告成功", list));
    }

    // 4. 發布新公告：POST http://localhost:8080/api/announcements
    @PostMapping
    public ResponseEntity<ApiResponse<Announcement>> createAnnouncement(@RequestBody Announcement announcement) {
        Announcement created = announcementService.createAnnouncement(announcement);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "發布公告成功", created));
    }

    // 5. 修改公告：PUT http://localhost:8080/api/announcements/1
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Announcement>> updateAnnouncement(
            @PathVariable Integer id,
            @RequestBody Announcement announcement) {
        Announcement updated = announcementService.updateAnnouncement(id, announcement);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "修改公告成功", updated));
    }

    // 6. 刪除公告：DELETE http://localhost:8080/api/announcements/1
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAnnouncement(@PathVariable Integer id) {
        announcementService.deleteAnnouncement(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "刪除公告成功", null));
    }
}
