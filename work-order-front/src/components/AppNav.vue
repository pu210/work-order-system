<template>
  <nav class="wo-nav">
    <div class="wo-nav-inner">
      <router-link to="/dashboard" class="wo-logo">
        <div class="wo-logo-mark">WO</div>
        <div class="wo-logo-text">WOHub<small>WORK ORDER SYSTEM</small></div>
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

        <div class="wo-role-switch">
          <button class="wo-role-btn" type="button" @click="dropdownOpen = !dropdownOpen">
            <div class="wo-avatar">{{ initials }}</div>
            <div style="text-align: left;">
              <div>{{ authStore.name || authStore.account }}</div>
              <div class="role-tag">{{ roleLabel }}</div>
            </div>
            <i class="bi bi-chevron-down"></i>
          </button>
          <div class="wo-dropdown" :class="{ open: dropdownOpen }">
            <router-link class="wo-dropdown-item" to="/profile" @click="dropdownOpen = false"><i class="bi bi-person"></i> 個人資料</router-link>
            <router-link class="wo-dropdown-item" to="/settings" @click="dropdownOpen = false"><i class="bi bi-gear"></i> 設定</router-link>
            <hr />
            <div class="wo-dropdown-item" @click="handleLogout"><i class="bi bi-box-arrow-right"></i> 登出</div>
          </div>
        </div>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.js'
import { useNotificationStore } from '@/stores/notification.js'
import { NAV_ITEMS } from '@/router/navItems.js'

const router = useRouter()
const authStore = useAuthStore()
const notificationStore = useNotificationStore()
const dropdownOpen = ref(false)

const ROLE_LABEL = { ADMIN: '管理員', HANDLER: '工程師', EMPLOYEE: '一般員工' }

const visibleNavItems = computed(() =>
  NAV_ITEMS.filter((item) => item.enabled && item.roles.some((role) => authStore.hasRole(role)))
)

const roleLabel = computed(() => {
  const primaryRole = authStore.roleCodes[0]
  return ROLE_LABEL[primaryRole] || primaryRole || ''
})

const initials = computed(() => {
  const name = authStore.name || authStore.account || '?'
  return name.length <= 2 ? name : name.slice(-2)
})

async function handleLogout() {
  dropdownOpen.value = false
  notificationStore.disconnectWebSocket() // 登出時斷開 WebSocket 連線
  authStore.logout()
  await router.replace({ name: 'Login' })
}

function handleClickOutside(event) {
  if (!event.target.closest('.wo-role-switch')) {
    dropdownOpen.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)

  // 當 App 載入且使用者已登入，且「無需強制修改密碼」時，才自動建立 WebSocket 連線與拉取通知
  if (authStore.userId && !authStore.mustChangePassword) {
    notificationStore.fetchNotifications()
    notificationStore.connectWebSocket(authStore.userId)
  }
})

onUnmounted(() => document.removeEventListener('click', handleClickOutside))
</script>
