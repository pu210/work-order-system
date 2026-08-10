package com.eeit219.work_order_system.modules.F.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.eeit219.work_order_system.modules.F.entity.Priority;

@Repository
public interface PriorityRepository extends JpaRepository<Priority, Integer> {

    @Query("SELECT p FROM Priority p WHERE "
            + "CAST(p.prioritiesId AS string) LIKE %:keyword% OR "
            + "p.name LIKE %:keyword% OR "
            + "CAST(p.hours AS string) LIKE %:keyword%")
    List<Priority> searchByKeyword(@Param("keyword") String keyword);

}
