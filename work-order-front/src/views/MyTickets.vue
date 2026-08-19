<template>
  <div class="mt-page">
    <div class="mt-page-header">
      <div>
        <span class="mt-eyebrow">MY TICKETS</span>
        <h1 class="mt-title">我的工單</h1>
        <p class="mt-subtitle">您所提交過的所有報修工單</p>
      </div>
      <router-link to="/ticket-create" class="mt-btn mt-btn-primary">+ 建立工單</router-link>
    </div>

    <div class="mt-card">
      <div class="mt-toolbar">
        <div class="mt-pill-tabs">
          <button
            type="button"
            class="mt-pill-tab"
            :class="{ active: statusFilter === '' }"
            @click="selectStatus('')"
          >
            全部狀態
          </button>
          <button
            v-for="s in STATUS_OPTIONS"
            :key="s.value"
            type="button"
            class="mt-pill-tab"
            :class="{ active: statusFilter === s.value }"
            @click="selectStatus(s.value)"
          >
            {{ s.label }}
          </button>
        </div>
        <div class="mt-search">
          <input
            v-model.trim="keyword"
            type="text"
            class="mt-input"
            placeholder="搜尋標題"
            @keyup.enter="reload"
          />
          <button type="button" class="mt-btn mt-btn-secondary" @click="reload">搜尋</button>
        </div>
      </div>

      <div v-if="errorMessage" class="mt-alert-danger">{{ errorMessage }}</div>

      <div v-else-if="loading" class="mt-loading">載入中…</div>

      <div v-else-if="tickets.length === 0" class="mt-empty-state">
        <div class="mt-empty-icon">📋</div>
        <h3>目前沒有符合的工單</h3>
        <p>試著建立您的第一張工單，或調整篩選條件</p>
      </div>

      <div v-else class="mt-table-wrap">
        <table class="mt-table">
          <thead>
            <tr>
              <th>工單編號</th>
              <th>標題</th>
              <th>類別</th>
              <th v-if="showPriority">優先級</th>
              <th>狀態</th>
              <th>處理人</th>
              <th>建立時間</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="t in tickets"
              :key="t.workOrderId"
              role="button"
              @click="router.push({ name: 'ticket-detail', params: { id: t.workOrderId }, query: { from: 'my-tickets' } })"
            >
              <td class="mt-mono">{{ t.workOrderNo }}</td>
              <td>{{ t.title }}</td>
              <td>{{ t.categoryName }}</td>
              <td v-if="showPriority">{{ t.priorityName }}</td>
              <td><span :class="['mt-badge', statusBadgeClass(t.status)]">{{ statusLabel(t.status) }}</span></td>
              <td>{{ t.assignedHandlerName || '—' }}</td>
              <td>{{ formatTime(t.createdTime) }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <nav v-if="totalPages > 1" class="mt-pagination">
        <button
          type="button"
          class="mt-page-btn"
          :disabled="page === 0"
          @click="goToPage(page - 1)"
        >
          上一頁
        </button>
        <span class="mt-page-info">第 {{ page + 1 }} / {{ totalPages }} 頁</span>
        <button
          type="button"
          class="mt-page-btn"
          :disabled="page + 1 >= totalPages"
          @click="goToPage(page + 1)"
        >
          下一頁
        </button>
      </nav>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMySubmissions } from '@/api/workOrder.js'
import { useAuthStore } from '@/stores/auth.js'

const router = useRouter()
const authStore = useAuthStore()

// 純 EMPLOYEE（沒有 ADMIN/HANDLER 角色）不顯示優先級欄位
const showPriority = computed(() => authStore.hasRole('ADMIN') || authStore.hasRole('HANDLER'))

const STATUS_OPTIONS = [
  { value: 'PENDING_REVIEW', label: '待審核' },
  { value: 'IN_PROGRESS', label: '進行中' },
  { value: 'PENDING_USER_ACCEPTANCE', label: '待使用者驗收' },
  { value: 'PENDING_ADMIN_ACCEPTANCE', label: '待管理員驗收' },
  { value: 'COMPLETED', label: '已完成' },
  { value: 'CANCELLED', label: '已取消' },
]

const STATUS_LABEL_MAP = Object.fromEntries(STATUS_OPTIONS.map((s) => [s.value, s.label]))

const STATUS_BADGE_MAP = {
  PENDING_REVIEW: 'mt-badge-neutral',
  IN_PROGRESS: 'mt-badge-primary',
  PENDING_USER_ACCEPTANCE: 'mt-badge-warning',
  PENDING_ADMIN_ACCEPTANCE: 'mt-badge-warning',
  COMPLETED: 'mt-badge-success',
  CANCELLED: 'mt-badge-neutral',
}

const tickets = ref([])
const keyword = ref('')
const statusFilter = ref('')
const page = ref(0)
const totalPages = ref(0)
const loading = ref(false)
const errorMessage = ref('')

function statusLabel(status) {
  return STATUS_LABEL_MAP[status] || status
}

function statusBadgeClass(status) {
  return STATUS_BADGE_MAP[status] || 'mt-badge-neutral'
}

function formatTime(value) {
  if (!value) return '—'
  return value.replace('T', ' ').slice(0, 16)
}

async function fetchTickets() {
  loading.value = true
  errorMessage.value = ''
  try {
    const result = await getMySubmissions({
      keyword: keyword.value || undefined,
      status: statusFilter.value || undefined,
      page: page.value,
    })
    tickets.value = result.content
    totalPages.value = result.totalPages
  } catch (error) {
    errorMessage.value = '無法載入工單列表，請確認後端已啟動'
  } finally {
    loading.value = false
  }
}

function reload() {
  page.value = 0
  fetchTickets()
}

function selectStatus(value) {
  statusFilter.value = value
  reload()
}

function goToPage(target) {
  page.value = target
  fetchTickets()
}

onMounted(fetchTickets)
</script>

<style scoped>
.mt-page {
  max-width: 1240px;
  margin: 0 auto;
}

/* ---------------------------------------------------------------------- */
/* 頁首 */
/* ---------------------------------------------------------------------- */
.mt-page-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 22px;
  flex-wrap: wrap;
}
.mt-eyebrow {
  display: block;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.08em;
  color: var(--color-primary);
  text-transform: uppercase;
  margin-bottom: 6px;
}
.mt-title {
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 24px;
  color: var(--color-ink);
  margin: 0;
}
.mt-subtitle {
  margin: 6px 0 0;
  color: var(--color-text-muted);
  font-size: 13.5px;
}

/* ---------------------------------------------------------------------- */
/* 卡片容器 */
/* ---------------------------------------------------------------------- */
.mt-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: 0 1px 2px rgba(20, 33, 61, 0.05), 0 2px 8px rgba(20, 33, 61, 0.06);
  padding: 20px 22px;
}

/* ---------------------------------------------------------------------- */
/* 工具列：篩選 pill-tabs + 搜尋 */
/* ---------------------------------------------------------------------- */
.mt-toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}
.mt-pill-tabs {
  display: flex;
  gap: 6px;
  background: #eceef2;
  padding: 4px;
  border-radius: 999px;
  flex-wrap: wrap;
}
.mt-pill-tab {
  padding: 6px 14px;
  border-radius: 999px;
  font-size: 12.5px;
  font-weight: 600;
  color: var(--color-text-muted);
  cursor: pointer;
  border: none;
  background: transparent;
  font-family: var(--font-body);
}
.mt-pill-tab.active {
  background: #fff;
  color: var(--color-ink);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.08);
}
.mt-search {
  display: flex;
  gap: 8px;
  margin-left: auto;
}
.mt-input {
  padding: 8px 12px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--color-border);
  font-size: 13.5px;
  font-family: var(--font-body);
  background: #fff;
  color: var(--color-text);
  min-width: 200px;
}
.mt-input:focus {
  border-color: var(--color-primary);
  outline: none;
  box-shadow: 0 0 0 3px var(--color-primary-soft);
}

/* ---------------------------------------------------------------------- */
/* 按鈕 */
/* ---------------------------------------------------------------------- */
.mt-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 13.5px;
  font-weight: 600;
  padding: 8px 16px;
  border-radius: var(--radius-sm);
  border: 1px solid transparent;
  cursor: pointer;
  transition: all 0.15s;
  font-family: var(--font-body);
  text-decoration: none;
}
.mt-btn-primary {
  background: var(--color-primary);
  color: #fff;
}
.mt-btn-primary:hover {
  background: var(--color-primary-dark);
  color: #fff;
}
.mt-btn-secondary {
  background: #fff;
  color: var(--color-text);
  border-color: var(--color-border);
}
.mt-btn-secondary:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

/* ---------------------------------------------------------------------- */
/* 表格 */
/* ---------------------------------------------------------------------- */
.mt-table-wrap {
  overflow-x: auto;
}
.mt-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}
.mt-table th {
  text-align: left;
  font-size: 11.5px;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: var(--color-text-faint);
  font-weight: 700;
  padding: 10px 14px;
  border-bottom: 1px solid var(--color-border);
  white-space: nowrap;
}
.mt-table td {
  padding: 13px 14px;
  border-bottom: 1px solid var(--color-border);
  vertical-align: middle;
}
.mt-table tbody tr {
  transition: background 0.12s;
  cursor: pointer;
}
.mt-table tbody tr:hover {
  background: var(--color-bg);
}
.mt-table tbody tr:last-child td {
  border-bottom: none;
}
.mt-mono {
  font-family: var(--font-mono, monospace);
  font-weight: 600;
  color: var(--color-ink);
}

/* ---------------------------------------------------------------------- */
/* 徽章 */
/* ---------------------------------------------------------------------- */
.mt-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 11.5px;
  font-weight: 700;
  line-height: 1.6;
}
.mt-badge-primary {
  background: var(--color-primary-soft);
  color: var(--color-primary-dark);
}
.mt-badge-success {
  background: var(--color-success-soft);
  color: var(--color-success);
}
.mt-badge-warning {
  background: var(--color-warning-soft);
  color: #92600f;
}
.mt-badge-danger {
  background: var(--color-danger-soft);
  color: var(--color-danger);
}
.mt-badge-neutral {
  background: #eef0f4;
  color: var(--color-text-muted);
}

/* ---------------------------------------------------------------------- */
/* 載入中 / 錯誤 / 空狀態 */
/* ---------------------------------------------------------------------- */
.mt-loading {
  padding: 32px 0;
  text-align: center;
  color: var(--color-text-muted);
  font-size: 13.5px;
}
.mt-alert-danger {
  background: var(--color-danger-soft);
  color: var(--color-danger);
  border-radius: var(--radius-sm);
  padding: 10px 14px;
  font-size: 13.5px;
}
.mt-empty-state {
  text-align: center;
  padding: 48px 20px;
  color: var(--color-text-muted);
}
.mt-empty-icon {
  font-size: 32px;
  margin-bottom: 10px;
}
.mt-empty-state h3 {
  font-size: 15px;
  color: var(--color-text);
  margin-bottom: 4px;
  font-family: var(--font-display);
}
.mt-empty-state p {
  font-size: 12.5px;
  margin: 0;
}

/* ---------------------------------------------------------------------- */
/* 分頁 */
/* ---------------------------------------------------------------------- */
.mt-pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-top: 18px;
}
.mt-page-btn {
  padding: 6px 14px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--color-border);
  background: #fff;
  cursor: pointer;
  font-size: 12.5px;
  color: var(--color-text-muted);
  font-family: var(--font-body);
}
.mt-page-btn:hover:not(:disabled) {
  border-color: var(--color-primary);
  color: var(--color-primary);
}
.mt-page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.mt-page-info {
  font-size: 12.5px;
  color: var(--color-text-muted);
}

@media (max-width: 700px) {
  .mt-toolbar {
    flex-direction: column;
    align-items: stretch;
  }
  .mt-search {
    margin-left: 0;
  }
}
</style>
