package com.eeit219.work_order_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class WorkOrderSystem {

    public static void main(String[] args) {
        SpringApplication.run(WorkOrderSystem.class, args);
    }
}