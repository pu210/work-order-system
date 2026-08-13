package com.eeit219.work_order_system.modules.c.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eeit219.work_order_system.modules.c.entity.RepairTicketHistory;

public interface RepairTicketHistoryRepository extends JpaRepository<RepairTicketHistory, Integer> {

}
