<!-- src/components/AppNavbar.vue -->
<template>
  <header
    class="navbar navbar-expand-lg navbar-dark bg-slate sticky-top px-3 py-2 border-bottom border-dark-subtle shadow-sm"
  >
    <div class="container-fluid px-0">
      <!-- 左側 Logo -->
      <router-link
        to="/dashboard"
        class="navbar-brand d-flex align-items-center me-4"
      >
        <div
          class="logo-icon me-2 rounded-2 d-flex align-items-center justify-content-center"
        >
          <i class="bi bi-tools text-white fs-5"></i>
        </div>
        <span class="fw-semibold text-white tracking-wide">WorkOrder Pro</span>
      </router-link>

      <!-- 導覽頁籤 -->
      <div class="collapse navbar-collapse" id="navbarNav">
        <ul class="navbar-nav me-auto mb-2 mb-lg-0 gap-1">
          <li class="nav-item" v-for="item in navItems" :key="item.path">
            <router-link
              :to="item.path"
              class="nav-link px-3 py-2 rounded-2 d-flex align-items-center gap-2"
              active-class="active"
            >
              <i :class="item.icon"></i>
              <span>{{ item.title }}</span>
            </router-link>
          </li>
        </ul>

        <!-- 右側工具區：通知 + 個人帳號 + 聊天室開關 -->
        <div class="d-flex align-items-center gap-3">
          <NotificationBell />
          <UserDropdown />

          <div class="vr bg-secondary opacity-50 my-1"></div>

          <!-- 開啟聊天室按鈕 -->
          <button
            class="btn btn-outline-light btn-sm position-relative d-flex align-items-center gap-2 px-3 py-1-5 rounded-2"
            :class="{ active: isChatOpen }"
            @click="$emit('toggle-chat')"
            title="開啟即時聊天室"
          >
            <i class="bi bi-chat-text-fill"></i>
            <span class="d-none d-xl-inline">訊息</span>
            <span
              class="position-absolute top-0 start-100 translate-middle p-1 bg-danger border border-light rounded-circle"
            >
              <span class="visually-hidden">新訊息</span>
            </span>
          </button>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
defineProps({
  isChatOpen: Boolean,
});

defineEmits(["toggle-chat"]);

import NotificationBell from "./NotificationBell.vue";
import UserDropdown from "./UserDropdown.vue";

const navItems = [
  { title: "首頁", path: "/dashboard", icon: "bi bi-grid-1x2-fill" },
  { title: "工單報修", path: "/repair", icon: "bi bi-wrench-adjustable" },
  { title: "設備管理", path: "/equipment", icon: "bi bi-cpu-fill" },
  { title: "帳號管理", path: "/users", icon: "bi bi-people-fill" },
  { title: "統計報表", path: "/report", icon: "bi bi-bar-chart-line-fill" },
];
</script>

<style scoped>
.bg-slate {
  background-color: #1e293b; /* 沉穩低調深灰，避免正黑色的刺眼感 */
}

.logo-icon {
  width: 32px;
  height: 32px;
  background-color: #0d6efd;
}

/* 簡約頁籤點亮效果 */
.nav-link {
  color: #94a3b8 !important;
  font-size: 0.95rem;
  transition: all 0.2s ease;
}

.nav-link:hover {
  color: #f8fafc !important;
  background-color: rgba(255, 255, 255, 0.05);
}

.nav-link.active {
  color: #ffffff !important;
  background-color: rgba(255, 255, 255, 0.12) !important;
  font-weight: 500;
}

.btn-outline-light {
  border-color: #475569;
  color: #cbd5e1;
}

.btn-outline-light:hover,
.btn-outline-light.active {
  background-color: #334155;
  border-color: #64748b;
  color: #fff;
}
</style>
