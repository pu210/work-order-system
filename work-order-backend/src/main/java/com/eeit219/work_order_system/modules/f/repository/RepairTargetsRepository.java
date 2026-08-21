package com.eeit219.work_order_system.modules.f.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eeit219.work_order_system.modules.f.entity.RepairTargets;

public interface RepairTargetsRepository extends JpaRepository<RepairTargets, Integer> {
        Optional<RepairTargets> findByTargetNo(String targetNo);

}
