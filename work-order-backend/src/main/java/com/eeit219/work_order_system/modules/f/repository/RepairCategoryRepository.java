package com.eeit219.work_order_system.modules.f.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eeit219.work_order_system.modules.f.entity.RepairCategory;

public interface RepairCategoryRepository extends JpaRepository<RepairCategory, Integer> {

    @Query("SELECT c FROM RepairCategory c WHERE " +
            "c.name LIKE :keyword OR " +
            "c.defaultPriority.name LIKE :keyword OR " +
            "CONCAT(c.createdTime, '') LIKE :keyword OR " +
            "CONCAT(c.updatedTime, '') LIKE :keyword")
    List<RepairCategory> searchByKeyword(@Param("keyword") String keyword);

    List<RepairCategory> findByStatusTrue();
}