package com.eeit219.work_order_system.modules.e.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eeit219.work_order_system.modules.e.entity.Announcement;
import com.eeit219.work_order_system.modules.e.repository.AnnouncementRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;

    // 1. 查詢所有公告（優先顯示置頂，並依建立時間倒序）
    public List<Announcement> getAllAnnouncements() {
        return announcementRepository.findAllByOrderByIsPinnedDescCreatedTimeDesc();
    }

    // 2. 依分類查詢公告
    public List<Announcement> getAnnouncementsByCategory(String category) {
        return announcementRepository.findByCategoryOrderByIsPinnedDescCreatedTimeDesc(category);
    }

    // 3. 依 ID 查詢單筆公告
    public Optional<Announcement> getAnnouncementById(Integer id) {
        return announcementRepository.findById(id);
    }

    // 4. 新增公告
    @Transactional
    public Announcement createAnnouncement(Announcement announcement) {
        if (announcement.getCategory() == null || announcement.getCategory().isBlank()) {
            announcement.setCategory("GENERAL");
        }
        if (announcement.getIsPinned() == null) {
            announcement.setIsPinned(0);
        }
        return announcementRepository.save(announcement);
    }

    // 5. 修改公告
    @Transactional
    public Announcement updateAnnouncement(Integer id, Announcement updatedAnnouncement) {
        return announcementRepository.findById(id).map(existing -> {
            existing.setTitle(updatedAnnouncement.getTitle());
            existing.setContent(updatedAnnouncement.getContent());
            existing.setCategory(updatedAnnouncement.getCategory());
            existing.setIsPinned(updatedAnnouncement.getIsPinned());
            existing.setStartTime(updatedAnnouncement.getStartTime());
            existing.setEndTime(updatedAnnouncement.getEndTime());
            return announcementRepository.save(existing);
        }).orElseThrow(() -> new RuntimeException("找不到公告 ID: " + id));
    }

    // 6. 刪除公告
    @Transactional
    public void deleteAnnouncement(Integer id) {
        announcementRepository.deleteById(id);
    }
}
