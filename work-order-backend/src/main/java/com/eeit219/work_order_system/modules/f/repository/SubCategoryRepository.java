package com.eeit219.work_order_system.modules.f.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eeit219.work_order_system.modules.f.entity.SubCategory;

public interface SubCategoryRepository extends JpaRepository<SubCategory, Integer> {

        @Query("SELECT s FROM SubCategory s " +
                        "LEFT JOIN FETCH s.overridePriority " +
                        "LEFT JOIN FETCH s.repairCategory rc " +
                        "LEFT JOIN FETCH rc.defaultPriority " +
                        "WHERE s.subCategoriesId = :id")
        Optional<SubCategory> findByIdWithPriorityDetails(@Param("id") Integer id);

        @Query("SELECT s FROM SubCategory s LEFT JOIN s.repairCategory c WHERE "
                        + "s.name LIKE %:keyword% OR " // 子類名稱模糊搜尋
                        + "c.name LIKE %:keyword% OR " // 大類名稱模糊搜尋
                        + "s.overridePriorityName LIKE %:keyword% OR " // 優先級名稱模糊搜尋
                        + "str(s.subCategoriesId) = :keyword") // 只有在輸入精確 ID 時才撈 ID
        List<SubCategory> searchByKeyword(@Param("keyword") String keyword); // 註：保持你原本的方法名稱
}