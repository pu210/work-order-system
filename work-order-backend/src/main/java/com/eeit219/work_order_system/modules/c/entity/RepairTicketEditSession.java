package com.eeit219.work_order_system.modules.c.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.Nationalized;

import com.eeit219.work_order_system.modules.a.entity.User;
import com.eeit219.work_order_system.modules.b.entity.WorkOrder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "repair_ticket_edit_session")
@Getter
@Setter
@NoArgsConstructor
public class RepairTicketEditSession {

    @Id
    @Column(name = "ticket_id")
    private Integer ticketId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private WorkOrder workOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Nationalized
    @Column(name = "session_token", nullable = false, length = 100)
    private String sessionToken;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "last_active_time", nullable = false)
    private LocalDateTime lastActiveTime;
}