package com.eeit219.work_order_system.modules.e.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eeit219.work_order_system.modules.e.entity.Announcement;

public interface AnnouncementRepository extends JpaRepository<Announcement, Integer> {

    // 依「是否置頂倒序」及「建立時間倒序」查詢（置頂的公告排最前，最新建立的排在後）
    List<Announcement> findAllByOrderByIsPinnedDescCreatedTimeDesc();

    // 依分類查詢公告
    List<Announcement> findByCategoryOrderByIsPinnedDescCreatedTimeDesc(String category);
}
