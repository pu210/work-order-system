package com.eeit219.work_order_system.modules.a.repository;

import com.eeit219.work_order_system.modules.a.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
}
