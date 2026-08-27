<template>
  <nav class="wo-nav">
    <div class="wo-nav-inner">
      <router-link to="/dashboard" class="wo-logo">
        <img src="/favicon.ico" alt="logo" class="wo-logo-img" />
        <div class="wo-logo-text">GongXia</div>
      </router-link>

      <div class="wo-nav-links">
        <router-link
          v-for="item in visibleNavItems"
          :key="item.key"
          :to="item.path"
        >
          {{ item.label }}
        </router-link>
      </div>

      <div class="wo-nav-right">
        <router-link to="/notifications" class="wo-bell" title="通知中心">
          <i class="bi bi-bell"></i>
          <!-- 綁定 Pinia Store 的 hasUnread，只要有未讀通知就亮起紅點 -->
          <span v-if="notificationStore.hasUnread" class="wo-bell-dot"></span>
        </router-link>

        <router-link to="/profile" class="wo-user-info" title="個人資料設定">
          <div class="wo-avatar">{{ initials }}</div>

          <div class="wo-user-text">
            <div>{{ authStore.name || authStore.account }}</div>
            <div class="role-tag">{{ roleLabel }}</div>
          </div>
        </router-link>

        <button type="button" class="wo-logout-btn" @click="handleLogout">
          <i class="bi bi-box-arrow-right"></i>
          登出
        </button>

        <button
          type="button"
          class="wo-menu-btn"
          :aria-expanded="mobileMenuOpen"
          aria-controls="wo-mobile-menu"
          :aria-label="mobileMenuOpen ? '關閉導覽選單' : '開啟導覽選單'"
          @click.stop="mobileMenuOpen = !mobileMenuOpen"
        >
          <i :class="mobileMenuOpen ? 'bi bi-x-lg' : 'bi bi-list'"></i>
        </button>
      </div>
    </div>

    <div
      id="wo-mobile-menu"
      class="wo-mobile-menu"
      :class="{ open: mobileMenuOpen }"
    >
      <router-link
        v-for="item in visibleNavItems"
        :key="item.key"
        :to="item.path"
        @click="mobileMenuOpen = false"
      >
        {{ item.label }}
      </router-link>

      <div class="wo-mobile-menu-divider"></div>

      <router-link to="/profile" @click="mobileMenuOpen = false">
        <i class="bi bi-person"></i>
        個人資料
      </router-link>

      <button type="button" class="wo-mobile-logout" @click="handleLogout">
        <i class="bi bi-box-arrow-right"></i>
        登出
      </button>
    </div>
  </nav>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "@/stores/auth.js";
import { useNotificationStore } from "@/stores/notification.js";
import { NAV_ITEMS } from "@/router/navItems.js";
import { notify } from "@/plugins/notify.js";

const router = useRouter();
const authStore = useAuthStore();
const notificationStore = useNotificationStore();
const mobileMenuOpen = ref(false);

const ROLE_LABEL = { ADMIN: "管理員", HANDLER: "工程師", EMPLOYEE: "一般員工" };

const visibleNavItems = computed(() =>
  NAV_ITEMS.filter(
    (item) => item.enabled && item.roles.some((role) => authStore.hasRole(role))
  )
);

const roleLabel = computed(() => {
  const primaryRole = authStore.roleCodes?.[0];
  return ROLE_LABEL[primaryRole] || primaryRole || "";
});

const initials = computed(() => {
  const name = authStore.name || authStore.account || "?";
  return name.length <= 2 ? name : name.slice(-2);
});

async function handleLogout() {
  mobileMenuOpen.value = false;
  notificationStore.disconnectWebSocket(); // 登出時斷開 WebSocket 連線
  const result = await notify.confirm({
    title: "確定要登出嗎？",
    icon: "question",
    confirmButtonText: "確定",
    cancelButtonText: "取消",
  });

  if (!result.isConfirmed) {
    return;
  }

  await authStore.logout();
  await router.replace({ name: "Login" });
}

function handleClickOutside(event) {
  if (!event.target.closest(".wo-nav")) {
    mobileMenuOpen.value = false;
  }
}

onMounted(() => {
  document.addEventListener("click", handleClickOutside);

  // 當 App 載入且使用者已登入，且「無需強制修改密碼」時，才自動建立 WebSocket 連線與拉取通知
  if (authStore.userId && !authStore.mustChangePassword) {
    notificationStore.fetchNotifications();
    notificationStore.connectWebSocket(authStore.userId);
  }
});

onUnmounted(() => document.removeEventListener("click", handleClickOutside));
</script>
