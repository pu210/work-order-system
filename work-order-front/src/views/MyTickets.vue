<template>
  <div class="my-tickets-view">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h3 class="mb-0">我的工單</h3>
      <router-link to="/ticket-create" class="btn btn-primary btn-sm">+ 建立工單</router-link>
    </div>

    <div class="row g-2 mb-3">
      <div class="col-auto">
        <input
          v-model.trim="keyword"
          type="text"
          class="form-control form-control-sm"
          placeholder="搜尋標題"
          @keyup.enter="reload"
        />
      </div>
      <div class="col-auto">
        <select v-model="statusFilter" class="form-select form-select-sm" @change="reload">
          <option value="">全部狀態</option>
          <option v-for="s in STATUS_OPTIONS" :key="s.value" :value="s.value">{{ s.label }}</option>
        </select>
      </div>
      <div class="col-auto">
        <button class="btn btn-outline-secondary btn-sm" @click="reload">搜尋</button>
      </div>
    </div>

    <div v-if="errorMessage" class="alert alert-danger py-2">{{ errorMessage }}</div>

    <div v-else-if="loading" class="text-muted">載入中…</div>

    <div v-else-if="tickets.length === 0" class="text-muted">目前沒有工單</div>

    <div v-else class="table-responsive">
      <table class="table table-hover align-middle">
        <thead>
          <tr>
            <th>工單編號</th>
            <th>標題</th>
            <th>類別</th>
            <th>優先級</th>
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
            <td class="font-monospace">{{ t.workOrderNo }}</td>
            <td>{{ t.title }}</td>
            <td>{{ t.categoryName }}</td>
            <td>{{ t.priorityName }}</td>
            <td><span :class="['badge', statusBadgeClass(t.status)]">{{ statusLabel(t.status) }}</span></td>
            <td>{{ t.assignedHandlerName || '—' }}</td>
            <td>{{ formatTime(t.createdTime) }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <nav v-if="totalPages > 1" class="d-flex justify-content-center gap-2 mt-3">
      <button
        class="btn btn-sm btn-outline-secondary"
        :disabled="page === 0"
        @click="goToPage(page - 1)"
      >
        上一頁
      </button>
      <span class="align-self-center small text-muted">第 {{ page + 1 }} / {{ totalPages }} 頁</span>
      <button
        class="btn btn-sm btn-outline-secondary"
        :disabled="page + 1 >= totalPages"
        @click="goToPage(page + 1)"
      >
        下一頁
      </button>
    </nav>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMySubmissions } from '@/api/workOrder.js'

const router = useRouter()

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
  PENDING_REVIEW: 'text-bg-secondary',
  IN_PROGRESS: 'text-bg-primary',
  PENDING_USER_ACCEPTANCE: 'text-bg-warning',
  PENDING_ADMIN_ACCEPTANCE: 'text-bg-warning',
  COMPLETED: 'text-bg-success',
  CANCELLED: 'text-bg-light',
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
  return STATUS_BADGE_MAP[status] || 'text-bg-secondary'
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

function goToPage(target) {
  page.value = target
  fetchTickets()
}

onMounted(fetchTickets)
</script>
