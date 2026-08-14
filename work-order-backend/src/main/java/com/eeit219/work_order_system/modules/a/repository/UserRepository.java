package com.eeit219.work_order_system.modules.a.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eeit219.work_order_system.modules.a.entity.User;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByAccount(String account);

    Optional<User> findByName(String name);

    Optional<User> findByEmail(String email);

    boolean existsByAccount(String account);

    boolean existsByEmail(String email);

    boolean existsByEmailAndUserIdNot(String email, Integer userId);

    List<User> findByStatus(Byte status);

    List<User> findAllByOrderByUserIdAsc();

    List<User> findByNameContainingIgnoreCaseOrAccountContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String name,
            String account,
            String email);

    @Query(value = """
            select distinct u from User u
            left join u.userRoles ur
            left join ur.role r
            where (:keyword is null
                   or lower(u.name) like lower(concat('%', :keyword, '%'))
                   or lower(u.account) like lower(concat('%', :keyword, '%'))
                   or lower(u.email) like lower(concat('%', :keyword, '%')))
              and (:status is null or u.status = :status)
              and (:roleCode is null or upper(r.roleCode) = :roleCode)
            """,
            countQuery = """
            select count(distinct u.userId) from User u
            left join u.userRoles ur
            left join ur.role r
            where (:keyword is null
                   or lower(u.name) like lower(concat('%', :keyword, '%'))
                   or lower(u.account) like lower(concat('%', :keyword, '%'))
                   or lower(u.email) like lower(concat('%', :keyword, '%')))
              and (:status is null or u.status = :status)
              and (:roleCode is null or upper(r.roleCode) = :roleCode)
            """)
    Page<User> searchUsers(
            @Param("keyword") String keyword,
            @Param("status") Byte status,
            @Param("roleCode") String roleCode,
            Pageable pageable);
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
