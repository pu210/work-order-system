package com.eeit219.work_order_system.common.security;

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

    public Integer getUserId() {
        return getUser().getUserId();
    }
}
