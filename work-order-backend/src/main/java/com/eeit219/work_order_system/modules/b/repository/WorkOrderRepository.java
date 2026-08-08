package com.eeit219.work_order_system.modules.b.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eeit219.work_order_system.modules.b.entity.WorkOrder;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, Integer> {

    boolean existsByWorkOrderNo(String workOrderNo);

    Optional<WorkOrder> findFirstByWorkOrderNoStartingWithOrderByWorkOrderNoDesc(String prefix);
}
