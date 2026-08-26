package com.eeit219.work_order_system.modules.f.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.eeit219.work_order_system.modules.f.entity.RepairTarget;
@Repository
public interface RepairTargetRepository extends JpaRepository<RepairTarget, Integer> {

    @Query("SELECT t FROM RepairTarget t WHERE " +
            "str(t.targetId) = :keyword OR " + // 讓數字 ID 支援精準比對 (輸入 3 找 ID=3)
            "t.targetNo LIKE CONCAT('%', :keyword, '%') OR " +
            "t.name LIKE CONCAT('%', :keyword, '%') OR " +
            "t.model LIKE CONCAT('%', :keyword, '%')")
    List<RepairTarget> searchByKeyword(@Param("keyword") String keyword);

    Optional<RepairTarget> findByTargetNo(String targetNo);

}