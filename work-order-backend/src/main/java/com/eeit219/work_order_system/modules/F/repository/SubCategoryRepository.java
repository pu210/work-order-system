package com.eeit219.work_order_system.modules.F.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.eeit219.work_order_system.modules.F.entity.SubCategory;

@Repository
public interface SubCategoryRepository extends JpaRepository<SubCategory, Integer> {

    @Query("SELECT s FROM SubCategory s LEFT JOIN s.repairCategory c WHERE "
            + "CONCAT(s.subCategoriesId, '') LIKE %:keyword% OR "
            + "CONCAT(s.categoryId, '') LIKE %:keyword% OR "
            + "s.name LIKE %:keyword% OR "
            + "CONCAT(s.overridePriorityId, '') LIKE %:keyword% OR "
            + "s.overridePriorityName LIKE %:keyword% OR "
            + "CONCAT(s.createdTime, '') LIKE %:keyword% OR "
            + "CONCAT(s.updatedTime, '') LIKE %:keyword% OR "
            + "c.name LIKE %:keyword% OR " // 支援用大類名稱搜尋 (如: 電腦設備)
            + "CONCAT(c.defaultPriorityId, '') LIKE %:keyword%")
    List<SubCategory> searchByKeyword(@Param("keyword") String keyword);
}
