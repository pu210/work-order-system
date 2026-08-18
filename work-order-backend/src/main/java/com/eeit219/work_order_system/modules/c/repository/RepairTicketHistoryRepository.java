package com.eeit219.work_order_system.modules.c.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eeit219.work_order_system.modules.c.entity.RepairTicketHistory;
import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderEvent;

public interface RepairTicketHistoryRepository extends JpaRepository<RepairTicketHistory, Integer> {

    Optional<RepairTicketHistory> findTopByWorkOrderWorkOrderIdAndEventOrderByHistoryIdDesc(
            Integer workOrderId, WorkOrderEvent event);

}
