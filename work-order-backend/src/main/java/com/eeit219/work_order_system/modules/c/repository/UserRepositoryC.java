package com.eeit219.work_order_system.modules.c.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eeit219.work_order_system.modules.a.entity.User;

public interface UserRepositoryC extends JpaRepository<User, Integer> {
    @Query("""
            SELECT DISTINCT u
            FROM User u
            JOIN FETCH u.userRoles ur
            JOIN FETCH ur.role r
            WHERE u.userId = :userId
              AND u.status = 1
              AND r.roleCode = 'HANDLER'
            """)
    Optional<User> findActiveHandlerById(
            @Param("userId") Integer userId);
}
