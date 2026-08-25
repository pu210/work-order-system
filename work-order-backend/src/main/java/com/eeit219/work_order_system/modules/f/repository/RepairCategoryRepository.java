package com.eeit219.work_order_system.modules.f.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eeit219.work_order_system.modules.f.entity.RepairCategory;

public interface RepairCategoryRepository extends JpaRepository<RepairCategory, Integer> {

    @Query("SELECT c FROM RepairCategory c WHERE " +
            "CONCAT(c.repairCategoriesId, '') LIKE :keyword OR " +
            "c.name LIKE :keyword OR " +
            "CONCAT(c.defaultPriorityId, '') LIKE :keyword OR " +
            "c.defaultPriority.name LIKE :keyword OR " +
            "CONCAT(c.createdTime, '') LIKE :keyword OR " +
            "CONCAT(c.updatedTime, '') LIKE :keyword")
    List<RepairCategory> searchByKeyword(@Param("keyword") String keyword);

    // B 模組用：defaultPriority 是 @ManyToOne(fetch = EAGER)，但 EAGER 只保證「一定會載入」，
    // 沒有改動 F 模組的 findAll() 本身，改成獨立一支帶 JOIN FETCH 的查詢，
    // 給 RepairCategoryController 的 /all-with-priority 端點用（TicketList.vue
    // 工單列表篩選下拉選單在呼叫）。
    @Query("SELECT c FROM RepairCategory c LEFT JOIN FETCH c.defaultPriority")
    List<RepairCategory> findAllWithDefaultPriority();

    // B 模組用：新增工單頁下拉選單只要啟用中的大類，獨立一支查詢，跟上面給設定頁用的 searchByKeyword/findAll 分開，
    // 避免 F 模組之後調整那些查詢邏輯時，連帶把新增工單頁的下拉選單改壞。
    @Query("SELECT c FROM RepairCategory c LEFT JOIN FETCH c.defaultPriority WHERE c.status = true")
    List<RepairCategory> findByStatusTrue();
}
