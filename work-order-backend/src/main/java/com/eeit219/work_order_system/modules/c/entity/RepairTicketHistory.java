package com.eeit219.work_order_system.modules.c.entity;

import java.time.LocalDateTime;

import com.eeit219.work_order_system.modules.c.statemachine.WorkOrderState;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "repair_ticket_history")
public class RepairTicketHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Integer HistoryId;
    @Column(name = "ticket_id",nullable = false)
    private Integer TicketId;
    @Enumerated(EnumType.STRING)
    @Column(name = "status",nullable = false)
    private WorkOrderState status;
    @Column(name = "edited_time",nullable = false)
    private LocalDateTime editedTime;
    @Column (name ="editor_id",nullable = false)
    private Integer editorId;

}
