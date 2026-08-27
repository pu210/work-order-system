package com.eeit219.work_order_system.common.security;

import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.eeit219.work_order_system.common.exception.AuthenticatedUserNotFoundException;
import com.eeit219.work_order_system.modules.a.entity.User;
import com.eeit219.work_order_system.modules.a.repository.UserRepository;

// 從 SecurityContext 解析目前登入使用者，避免各模組 Controller 各自重複實作同樣的查詢邏輯。
// 任何模組的 Controller/Service 需要「目前登入的是誰」都可以注入這個元件使用。
@Component
public class CurrentUserProvider {

    private final UserRepository userRepository;

    public CurrentUserProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUser() {
        String account = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByAccount(account)
                .orElseThrow(() -> new AuthenticatedUserNotFoundException("找不到登入使用者：" + account));
    }

    // B 模組用：只要 userId 的話不用查資料庫。JsonWebTokenFilter 驗證 token 時已經把 userId
    // 放進 SecurityContext 的 AuthenticatedUser principal 裡，這裡直接讀，不用像 getUser()
    // 那樣再查一次資料庫。
    public Integer getUserId() {
        AuthenticatedUser principal = (AuthenticatedUser) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return principal.userId();
    }

    // B 模組用：角色清單也不用查資料庫。JsonWebTokenFilter 驗證 token 時已經把角色查出來，
    // 存成 ROLE_ADMIN 這種格式放進 Authentication 的 authorities，這裡直接讀、去掉 ROLE_ 前綴就好
    public List<String> getRoleCodes() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring(5))
                .toList();
    }
}
