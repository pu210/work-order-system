package com.eeit219.work_order_system.modules.e.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "repair_categories") // 告訴 JPA 對應 SQL Server 的 repair_categories 資料表
public class RepairCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    // --- 建構子 (Constructors) ---
    public RepairCategory() {
    }

    public RepairCategory(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    // --- Getter 與 Setter ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}