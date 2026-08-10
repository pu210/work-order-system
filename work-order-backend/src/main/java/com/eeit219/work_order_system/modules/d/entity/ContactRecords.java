package com.eeit219.work_order_system.modules.d.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "contact_records")
public class ContactRecords {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id", nullable = false)
    private Integer recordId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @Column(name = "author_user_id", nullable = false)
    private Integer authorUserId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @Column(name = "work_order_id", nullable = false)
    private Integer workOrder;

    @Column(name = "content", nullable = false, length = 500)
    private String content;

    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;

}
