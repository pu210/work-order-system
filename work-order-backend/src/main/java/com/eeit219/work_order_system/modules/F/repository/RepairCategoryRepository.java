package com.eeit219.work_order_system.modules.f.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.eeit219.work_order_system.modules.f.entity.RepairCategory;

@Repository
public interface RepairCategoryRepository extends JpaRepository<RepairCategory, Integer> {

    @Query("SELECT c FROM RepairCategory c WHERE " +
            "CONCAT(c.repairCategoriesId, '') LIKE :keyword OR " +
            "c.name LIKE :keyword OR " +
            "CONCAT(c.defaultPriorityId, '') LIKE :keyword OR " +
            "c.defaultPriority.name LIKE :keyword OR " +
            "CONCAT(c.createdTime, '') LIKE :keyword OR " +
            "CONCAT(c.updatedTime, '') LIKE :keyword")
    List<RepairCategory> searchByKeyword(@Param("keyword") String keyword);
}