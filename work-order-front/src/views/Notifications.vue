<template>
  <div class="notifications-page container-fluid py-4 px-2 px-sm-4">
    <!-- 頁面標題與操作欄 -->
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h2 class="h4 fw-bold text-dark mb-1">
          <i class="bi bi-bell-fill text-primary me-2"></i>通知中心
        </h2>
        <p class="text-muted small mb-0">管理並檢視與您相關的所有系統通知與工單異動</p>
      </div>

      <div class="d-flex align-items-center gap-2">
        <button 
          class="btn btn-outline-secondary btn-sm rounded-pill px-3" 
          @click="loadNotifications" 
          :disabled="loading"
        >
          <i class="bi bi-arrow-clockwise me-1" :class="{ 'spin': loading }"></i> 重新整理
        </button>
        <button 
          v-if="unreadCount > 0" 
          class="btn btn-outline-primary btn-sm rounded-pill px-3" 
          @click="markAllAsRead"
        >
          <i class="bi bi-check2-all me-1"></i> 全部標為已讀
        </button>
      </div>
    </div>

    <!-- 頁籤切換與統計 -->
    <div class="card shadow-sm border-0 mb-4 rounded-3">
      <div class="card-body p-3 d-flex justify-content-between align-items-center flex-wrap gap-2">
        <div class="nav nav-pills custom-pills" id="notification-tabs">
          <button 
            class="nav-link px-3 py-1.5 rounded-pill me-2" 
            :class="{ active: currentTab === 'all' }"
            @click="currentTab = 'all'"
          >
            全部 <span class="badge bg-secondary ms-1">{{ notifications.length }}</span>
          </button>
          <button 
            class="nav-link px-3 py-1.5 rounded-pill me-2" 
            :class="{ active: currentTab === 'unread' }"
            @click="currentTab = 'unread'"
          >
            未讀 <span class="badge bg-danger ms-1">{{ unreadCount }}</span>
          </button>
          <button 
            class="nav-link px-3 py-1.5 rounded-pill" 
            :class="{ active: currentTab === 'read' }"
            @click="currentTab = 'read'"
          >
            已讀 <span class="badge bg-light text-dark ms-1">{{ readCount }}</span>
          </button>
        </div>

        <div class="text-muted small">
          共 <span class="fw-bold text-dark">{{ filteredNotifications.length }}</span> 則通知
        </div>
      </div>
    </div>

    <!-- 載入中狀態 -->
    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">載入中...</span>
      </div>
      <p class="text-muted small mt-2">載入通知列表中...</p>
    </div>

    <!-- 錯誤訊息提示 -->
    <div v-else-if="errorMessage" class="alert alert-danger shadow-sm rounded-3 py-3" role="alert">
      <i class="bi bi-exclamation-triangle-fill me-2"></i> {{ errorMessage }}
      <button class="btn btn-outline-danger btn-sm ms-3" @click="loadNotifications">重試</button>
    </div>

    <!-- 空資料狀態 -->
    <div v-else-if="filteredNotifications.length === 0" class="card shadow-sm border-0 rounded-3 text-center py-5">
      <div class="card-body">
        <div class="empty-icon-wrap mx-auto mb-3">
          <i class="bi bi-bell-slash fs-1 text-muted opacity-50"></i>
        </div>
        <h6 class="fw-bold text-secondary mb-1">目前沒有通知</h6>
        <p class="text-muted small mb-0">當有工單狀態變更或新任務指派時，將會顯示於此處。</p>
      </div>
    </div>

    <!-- 通知清單列表 -->
    <div v-else class="notification-list d-flex flex-column gap-3">
      <div 
        v-for="item in filteredNotifications" 
        :key="item.notificationId"
        class="card notification-card shadow-2xs border-0 rounded-3 transition-all"
        :class="{ 'unread-card': !item.isRead }"
      >
        <div class="card-body p-3.5">
          <div class="d-flex align-items-start gap-3">
            <!-- 左側圖示 -->
            <div class="icon-avatar rounded-circle d-flex align-items-center justify-content-center flex-shrink-0" :class="getIconBgClass(item)">
              <i :class="getNotificationIcon(item)"></i>
            </div>

            <!-- 中間核心內文 -->
            <div class="flex-grow-1 min-w-0">
              <div class="d-flex align-items-center justify-content-between mb-1 gap-2">
                <div class="d-flex align-items-center gap-2 flex-wrap">
                  <h6 class="fw-bold text-dark mb-0 fs-6">{{ item.title || '系統通知' }}</h6>
                  <span v-if="!item.isRead" class="badge bg-danger rounded-pill extra-small">新通知</span>
                  <span v-if="item.status" class="badge bg-light text-secondary border extra-small">{{ formatStatus(item.status) }}</span>
                </div>
                <span class="text-muted extra-small flex-shrink-0">{{ formatTime(item.createdTime) }}</span>
              </div>

              <!-- 訊息主要內容 -->
              <p class="text-secondary small mb-2 text-break" style="line-height: 1.6;">
                {{ item.message }}
              </p>

              <!-- 底部資訊與操作 -->
              <div class="d-flex align-items-center justify-content-between pt-1">
                <div class="d-flex align-items-center gap-3 extra-small text-muted">
                  <span v-if="item.workOrderId">
                    <i class="bi bi-file-earmark-text me-1"></i>工單編號：#{{ item.workOrderId }}
                  </span>
                  <span v-if="item.senderId">
                    <i class="bi bi-person me-1"></i>發送人 ID: {{ item.senderId }}
                  </span>
                </div>

                <div class="d-flex align-items-center gap-2">
                  <router-link 
                    v-if="item.workOrderId" 
                    :to="`/tickets/${item.workOrderId}`"
                    class="btn btn-primary btn-xs rounded-pill px-3"
                  >
                    查看工單 <i class="bi bi-chevron-right ms-1"></i>
                  </router-link>
                  <button 
                    v-if="!item.isRead" 
                    class="btn btn-light btn-xs text-secondary rounded-pill px-2.5"
                    @click="toggleReadStatus(item)"
                    title="標記為已讀"
                  >
                    <i class="bi bi-check-lg me-1"></i>標為已讀
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useNotificationStore } from '@/stores/notification.js'

const notificationStore = useNotificationStore()
const loading = ref(false)
const errorMessage = ref('')
const currentTab = ref('all') // 'all' | 'unread' | 'read'

// 直接使用 Store 的響應式資料與狀態
const notifications = computed(() => notificationStore.notifications)
const unreadCount = computed(() => notificationStore.unreadCount)
const readCount = computed(() => notifications.value.filter(n => n.isRead).length)

const filteredNotifications = computed(() => {
  if (currentTab.value === 'unread') {
    return notifications.value.filter(n => !n.isRead)
  }
  if (currentTab.value === 'read') {
    return notifications.value.filter(n => n.isRead)
  }
  return notifications.value
})

// ---- 1. 從後端 API 重新載入通知 ----
const loadNotifications = async () => {
  loading.value = true
  errorMessage.value = ''
  try {
    await notificationStore.fetchNotifications()
  } catch (error) {
    console.error('載入通知失敗：', error)
    errorMessage.value = '無法取得通知清單，請稍後再試。'
  } finally {
    loading.value = false
  }
}

// ---- 2. 標記單筆已讀 ----
const toggleReadStatus = async (item) => {
  await notificationStore.markAsRead(item)
}

// ---- 3. 全部標為已讀 ----
const markAllAsRead = async () => {
  await notificationStore.markAllAsRead()
}

// ---- 4. 工具與樣式選取 ----
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  return timeStr.replace('T', ' ').substring(0, 16)
}

const formatStatus = (status) => {
  const map = {
    PENDING_REVIEW: '待審核',
    DRAFT: '草稿',
    SUBMITTED: '已送出',
    ASSIGNED: '已派單',
    IN_PROGRESS: '處理中',
    PENDING_USER_ACCEPTANCE: '待使用者驗收',
    PENDING_ADMIN_ACCEPTANCE: '待管理員驗收',
    COMPLETED: '已完成',
    CLOSED: '已結案',
    CANCELLED: '已撤回',
    REJECTED: '已退單'
  }
  return map[status] || status
}

const getNotificationIcon = (item) => {
  if (item.title?.includes('退')) return 'bi bi-arrow-return-left'
  if (item.title?.includes('完成') || item.title?.includes('結案')) return 'bi bi-check-circle-fill'
  if (item.title?.includes('新任務') || item.title?.includes('指派')) return 'bi bi-briefcase-fill'
  return 'bi bi-bell-fill'
}

const getIconBgClass = (item) => {
  if (item.title?.includes('退')) return 'bg-danger-subtle text-danger'
  if (item.title?.includes('完成') || item.title?.includes('結案')) return 'bg-success-subtle text-success'
  if (item.title?.includes('新任務') || item.title?.includes('指派')) return 'bg-primary-subtle text-primary'
  return 'bg-secondary-subtle text-secondary'
}

// 組件掛載後自動載入歷史通知
onMounted(() => {
  loadNotifications()
})
</script>

<style scoped>
.notifications-page {
  width: 100%;
}

.extra-small {
  font-size: 0.75rem;
}

.btn-xs {
  padding: 0.25rem 0.65rem;
  font-size: 0.75rem;
}

.shadow-2xs {
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.05);
}

.icon-avatar {
  width: 42px;
  height: 42px;
  font-size: 1.15rem;
}

.notification-card {
  border-left: 4px solid transparent !important;
  background-color: #ffffff;
  transition: all 0.2s ease;
}

.notification-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

/* 未讀通知卡片醒目提示 */
.notification-card.unread-card {
  border-left-color: #0d6efd !important;
  background-color: #f8fafc;
}

.custom-pills .nav-link {
  font-weight: 500;
  color: #64748b;
  background-color: transparent;
  transition: all 0.2s ease;
}

.custom-pills .nav-link.active {
  color: #0d6efd;
  background-color: #eff6ff;
  font-weight: 600;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.spin {
  animation: spin 1s linear infinite;
}
</style>
