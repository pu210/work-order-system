package com.eeit219.work_order_system.modules.f.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.eeit219.work_order_system.modules.f.entity.RepairTarget;

@Repository
public interface RepairTargetRepository extends JpaRepository<RepairTarget, Integer> {

    // 多欄位模糊比對：同時搜尋 targetNo, name, model
    @Query("SELECT t FROM RepairTarget t WHERE " +
            "t.targetNo LIKE CONCAT('%', :keyword, '%') OR " +
            "t.name LIKE CONCAT('%', :keyword, '%') OR " +
            "t.model LIKE CONCAT('%', :keyword, '%')")
    List<RepairTarget> searchByKeyword(@Param("keyword") String keyword);
}