package com.eeit219.work_order_system.modules.f.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eeit219.work_order_system.modules.f.entity.SubCategory;

public interface SubCategoryRepository extends JpaRepository<SubCategory, Integer> {

    @Query("SELECT s FROM SubCategory s "
            + "LEFT JOIN FETCH s.overridePriority "
            + "LEFT JOIN FETCH s.repairCategory rc "
            + "LEFT JOIN FETCH rc.defaultPriority "
            + "WHERE s.subCategoriesId = :id")
    Optional<SubCategory> findByIdWithPriorityDetails(@Param("id") Integer id);

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
