package com.eeit219.work_order_system.modules.a.service;

import java.util.List;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eeit219.work_order_system.modules.a.dto.CreateUserRequestDTO;
import com.eeit219.work_order_system.modules.a.dto.CreateUserResponseDTO;
import com.eeit219.work_order_system.modules.a.dto.CurrentUserDTO;
import com.eeit219.work_order_system.modules.a.dto.PageResponseDTO;
import com.eeit219.work_order_system.modules.a.dto.RegisterRequestDTO;
import com.eeit219.work_order_system.modules.a.dto.RegisterResponseDTO;
import com.eeit219.work_order_system.modules.a.dto.UpdateUserRequestDTO;
import com.eeit219.work_order_system.modules.a.dto.UpdateUserResponseDTO;
import com.eeit219.work_order_system.modules.a.dto.UserResponseDTO;
import com.eeit219.work_order_system.common.exception.ResourceNotFoundException;
import com.eeit219.work_order_system.modules.a.entity.Role;
import com.eeit219.work_order_system.modules.a.entity.User;
import com.eeit219.work_order_system.modules.a.entity.UserRole;
import com.eeit219.work_order_system.modules.a.entity.UserRoleId;
import com.eeit219.work_order_system.modules.a.repository.RoleRepository;
import com.eeit219.work_order_system.modules.a.repository.UserRepository;
import com.eeit219.work_order_system.modules.a.repository.UserRoleRepository;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
            RoleRepository roleRepository,
            UserRoleRepository userRoleRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 使用者登入
    public CurrentUserDTO loginUser(String account, String password) {
        if (account != null && account.length() != 0 && password != null && password.length() != 0) {
            User user = userRepository.findByAccount(account).orElse(null);
            if (user != null
                    && user.getStatus() == User.UserStatus.ACTIVE
                    && user.getPasswordHash() != null
                    && passwordEncoder.matches(password, user.getPasswordHash())) {
                return new CurrentUserDTO(
                        user.getUserId(),
                        user.getAccount(),
                        user.getName(),
                        user.getEmail(),
                        user.getMustChangePassword(),
                        userRoleRepository.findRoleCodesByUserId(user.getUserId())
                                .stream()
                                .map(roleCode -> roleCode.trim().toUpperCase())
                                .distinct()
                                .sorted()
                                .toList());
            }
        }
        return null;
    }

    // 註冊帳號
    public RegisterResponseDTO registerUser(RegisterRequestDTO request) {
        if (request.account() == null || request.account().isBlank()) {
            throw new IllegalArgumentException("帳號為必填");
        }

        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("name為必填");
        }

        if (request.email() == null || request.email().isBlank()) {
            throw new IllegalArgumentException("email為必填");
        }
        if (request.password() == null || request.password().isBlank()) {
            throw new IllegalArgumentException("password為必填");
        }
        if (request.confirmPassword() == null || request.confirmPassword().isBlank()) {
            throw new IllegalArgumentException("confirmPassword為必填");
        }

        String account = request.account().trim();
        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByAccount(account)) {
            throw new IllegalArgumentException("帳號已被使用");
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email 已被使用");
        }

        if (request.password() == null || request.confirmPassword() == null
                || !request.password().equals(request.confirmPassword())) {
            throw new IllegalArgumentException("兩次輸入的密碼不一致");
        }
        User user = new User();
        user.setAccount(account);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setMustChangePassword(false);
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPhone(request.phone());
        user.setStatus(User.UserStatus.PENDING);

        User savedUser = userRepository.save(user);

        return new RegisterResponseDTO(
                savedUser.getAccount(),
                savedUser.getName(),
                savedUser.getEmail());
    }

    // 建立帳號
    public CreateUserResponseDTO createUser(CreateUserRequestDTO request) {
        if (request.account() == null || request.account().isBlank()) {
            throw new IllegalArgumentException("帳號為必填");
        }

        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("name為必填");
        }

        if (request.email() == null || request.email().isBlank()) {
            throw new IllegalArgumentException("email為必填");
        }
        if (request.password() == null || request.password().isBlank()) {
            throw new IllegalArgumentException("password為必填");
        }
        if (request.roleCodes() == null || request.roleCodes().isEmpty()) {
            throw new IllegalArgumentException("roleCodes為必填");
        }

        String account = request.account().trim();
        String email = request.email().trim().toLowerCase();
        List<String> roleCodes = normalizeRoleCodes(request.roleCodes());

        if (userRepository.existsByAccount(account)) {
            throw new IllegalArgumentException("帳號已被使用");
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email 已被使用");
        }

        List<Role> roles = findRoles(roleCodes);

        User user = new User();
        user.setAccount(account);
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPhone(request.phone());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setStatus(User.UserStatus.ACTIVE);
        user.setMustChangePassword(true);

        User savedUser = userRepository.save(user);

        saveUserRoles(savedUser, roles);

        return new CreateUserResponseDTO(
                savedUser.getAccount(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getPhone(),
                roleCodes,
                savedUser.getStatus(),
                savedUser.getMustChangePassword());
    }

    // 管理員編輯使用者資料
    public UpdateUserResponseDTO updateUser(Integer userId, UpdateUserRequestDTO request) {
        if (request.name() == null
                && request.email() == null
                && request.phone() == null
                && request.status() == null
                && request.roleCodes() == null) {
            throw new IllegalArgumentException("至少提供一個要修改的欄位");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("使用者不存在"));

        byte proposedStatus = request.status() != null
                ? validateStatus(request.status())
                : user.getStatus();
        List<String> currentRoleCodes = userRoleRepository.findRoleCodesByUserId(userId)
                .stream()
                .map(roleCode -> roleCode.trim().toUpperCase())
                .distinct()
                .sorted()
                .toList();
        List<String> proposedRoleCodes = request.roleCodes() != null
                ? normalizeRoleCodes(request.roleCodes())
                : currentRoleCodes;

        validateAdminProtection(user, proposedStatus, currentRoleCodes, proposedRoleCodes);

        if (request.name() != null) {
            if (request.name().isBlank()) {
                throw new IllegalArgumentException("name為必填");
            }
            user.setName(request.name().trim());
        }

        if (request.email() != null) {
            if (request.email().isBlank()) {
                throw new IllegalArgumentException("email為必填");
            }

            String email = request.email().trim().toLowerCase();
            if (userRepository.existsByEmailAndUserIdNot(email, userId)) {
                throw new IllegalArgumentException("Email 已被使用");
            }
            user.setEmail(email);
        }

        if (request.phone() != null) {
            String phone = request.phone().trim();
            user.setPhone(phone.isEmpty() ? null : phone);
        }

        if (request.status() != null) {
            user.setStatus(proposedStatus);
        }

        if (request.roleCodes() != null) {
            List<Role> roles = findRoles(proposedRoleCodes);

            userRoleRepository.deleteByIdUserId(userId);
            userRoleRepository.flush();
            saveUserRoles(user, roles);
        }

        User savedUser = userRepository.save(user);
        List<String> roleCodes = userRoleRepository.findRoleCodesByUserId(userId)
                .stream()
                .sorted()
                .toList();

        return new UpdateUserResponseDTO(
                savedUser.getUserId(),
                savedUser.getAccount(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getPhone(),
                savedUser.getStatus(),
                roleCodes);
    }

    private byte validateStatus(Byte status) {
        if (status != User.UserStatus.DISABLED
                && status != User.UserStatus.ACTIVE
                && status != User.UserStatus.PENDING
                && status != User.UserStatus.REJECTED) {
            throw new IllegalArgumentException("帳號狀態不存在");
        }
        return status;
    }

    private void validateAdminProtection(
            User targetUser,
            byte proposedStatus,
            List<String> currentRoleCodes,
            List<String> proposedRoleCodes) {
        boolean willBeDisabled = proposedStatus != User.UserStatus.ACTIVE;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean editingSelf = authentication != null
                && authentication.isAuthenticated()
                && targetUser.getAccount().equals(authentication.getName());

        if (editingSelf && willBeDisabled) {
            throw new IllegalArgumentException("管理員不能停用自己的帳號");
        }

        boolean currentlyActiveAdmin = targetUser.getStatus() == User.UserStatus.ACTIVE
                && currentRoleCodes.contains(Role.ADMIN);
        boolean remainsActiveAdmin = proposedStatus == User.UserStatus.ACTIVE
                && proposedRoleCodes.contains(Role.ADMIN);

        if (currentlyActiveAdmin && !remainsActiveAdmin) {
            long activeAdminCount = userRoleRepository.countUsersByRoleCodeAndStatus(
                    Role.ADMIN, User.UserStatus.ACTIVE);
            if (activeAdminCount <= 1) {
                throw new IllegalArgumentException("不能停用或移除系統最後一位管理員");
            }
        }
    }

    private List<String> normalizeRoleCodes(List<String> roleCodes) {
        LinkedHashSet<String> normalized = roleCodes.stream()
                .map(roleCode -> {
                    if (roleCode == null || roleCode.isBlank()) {
                        throw new IllegalArgumentException("roleCode不可為空");
                    }
                    return roleCode.trim().toUpperCase();
                })
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("roleCodes至少需要一個角色");
        }
        return List.copyOf(normalized);
    }

    private List<Role> findRoles(List<String> roleCodes) {
        return roleCodes.stream()
                .map(roleCode -> roleRepository.findByRoleCode(roleCode)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "roleCode不存在: " + roleCode)))
                .toList();
    }

    private void saveUserRoles(User user, List<Role> roles) {
        List<UserRole> userRoles = roles.stream()
                .map(role -> {
                    UserRole userRole = new UserRole();
                    userRole.setId(new UserRoleId(user.getUserId(), role.getRoleId()));
                    userRole.setUser(user);
                    userRole.setRole(role);
                    return userRole;
                })
                .toList();
        userRoleRepository.saveAll(userRoles);
    }

    // 查詢使用者(一筆)
    @Transactional(readOnly = true)
    public UserResponseDTO getUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("使用者不存在"));

        List<String> roleCodes = userRoleRepository.findRoleCodesByUserId(userId)
                .stream()
                .sorted()
                .toList();

        return toUserResponseDTO(user, roleCodes);
    }

    // 分頁與條件查詢使用者
    @Transactional(readOnly = true)
    public PageResponseDTO<UserResponseDTO> searchUsers(
            int page, int size, String keyword, Byte status, String roleCode) {
        if (page < 0) {
            throw new IllegalArgumentException("page 不可小於 0");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("size 必須介於 1 到 100");
        }
        if (status != null
                && status != User.UserStatus.DISABLED
                && status != User.UserStatus.ACTIVE
                && status != User.UserStatus.PENDING
                && status != User.UserStatus.REJECTED) {
            throw new IllegalArgumentException("帳號狀態不存在");
        }

        String normalizedKeyword = normalizeNullable(keyword);
        String normalizedRoleCode = normalizeNullable(roleCode);
        if (normalizedRoleCode != null) {
            normalizedRoleCode = normalizedRoleCode.toUpperCase();
        }

        Page<User> users = userRepository.searchUsers(
                normalizedKeyword,
                status,
                normalizedRoleCode,
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "userId")));

        List<Integer> userIds = users.getContent().stream()
                .map(User::getUserId)
                .toList();

        Map<Integer, List<String>> roleCodesByUserId = getRoleCodesByUserIds(userIds);

        Page<UserResponseDTO> result = users
                .map(user -> toUserResponseDTO(
                        user,
                        roleCodesByUserId.getOrDefault(
                                user.getUserId(),
                                List.of())));

        return PageResponseDTO.from(result);
    }

    private Map<Integer, List<String>> getRoleCodesByUserIds(List<Integer> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }

        return userRoleRepository.findWithRoleByUserIds(userIds)
                .stream()
                .collect(Collectors.groupingBy(
                        userRole -> userRole.getUser().getUserId(),
                        Collectors.mapping(
                                userRole -> userRole.getRole().getRoleCode(),
                                Collectors.collectingAndThen(
                                        Collectors.toList(),
                                        roleCodes -> roleCodes.stream()
                                                .distinct()
                                                .sorted()
                                                .toList()))));
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private UserResponseDTO toUserResponseDTO(User user, List<String> roleCodes) {
        return new UserResponseDTO(
                user.getUserId(),
                user.getAccount(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getStatus(),
                user.getMustChangePassword(),
                roleCodes,
                user.getCreatedTime(),
                user.getUpdatedTime());
    }
}
