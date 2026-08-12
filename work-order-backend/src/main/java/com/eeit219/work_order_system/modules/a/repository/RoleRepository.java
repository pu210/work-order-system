package com.eeit219.work_order_system.modules.a.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eeit219.work_order_system.modules.a.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Integer> {

    Optional<Role> findByRoleCode(String roleCode);

    boolean existsByRoleCode(String roleCode);

    List<Role> findAllByOrderByRoleIdAsc();
}