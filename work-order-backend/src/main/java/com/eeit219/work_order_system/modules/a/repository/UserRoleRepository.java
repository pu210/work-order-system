package com.eeit219.work_order_system.modules.a.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import com.eeit219.work_order_system.modules.a.entity.UserRole;
import com.eeit219.work_order_system.modules.a.entity.UserRoleId;

public interface UserRoleRepository
        extends JpaRepository<UserRole, UserRoleId> {

    List<UserRole> findByIdUserId(Integer userId);

    List<UserRole> findByIdRoleId(Integer roleId);

    @Query("select ur.role.roleCode from UserRole ur where ur.user.userId = :userId")
    List<String> findRoleCodesByUserId(@Param("userId") Integer userId);

    // D模組留言通知使用。
    // 依角色代號與帳號狀態查詢使用者ID。工單尚未指定負責管理員時，會將查到的啟用中管理員加入通知對象。
    @Query("""
        select distinct ur.user.userId
        from UserRole ur
        where upper(ur.role.roleCode) = upper(:roleCode)
          and ur.user.status = :status
        """)
    List<Integer> findUserIdsByRoleCodeAndStatus(
            @Param("roleCode") String roleCode,
            @Param("status") Byte status);

    @Query("select ur from UserRole ur join fetch ur.role where ur.user.userId in :userIds")
    List<UserRole> findWithRoleByUserIds(@Param("userIds") List<Integer> userIds);

    @Query("""
            select count(distinct ur.user.userId)
            from UserRole ur
            where upper(ur.role.roleCode) = upper(:roleCode)
              and ur.user.status = :status
            """)
    long countUsersByRoleCodeAndStatus(
            @Param("roleCode") String roleCode,
            @Param("status") Byte status);

    boolean existsByIdUserIdAndIdRoleId(Integer userId, Integer roleId);

    void deleteByIdUserId(Integer userId);

    void deleteByIdUserIdAndIdRoleId(Integer userId, Integer roleId);
}
