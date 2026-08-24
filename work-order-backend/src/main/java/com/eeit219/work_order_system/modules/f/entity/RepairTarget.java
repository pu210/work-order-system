package com.eeit219.work_order_system.modules.f.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "repair_targets")
public class RepairTarget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "target_id")
    private Integer targetId;

    @Column(name = "target_no", nullable = false, unique = true, length = 30)
    private String targetNo;

    @Column(name = "name", nullable = false, length = 30)
    private String name;

    @Column(name = "model", length = 50)
    private String model;

    @Column(name = "status")
    private Boolean status; // true 啟用, false 停用 (軟刪除/狀態切換)
}
