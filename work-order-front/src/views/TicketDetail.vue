<template>
  <div class="ticket-detail-view">
    <RouterLink :to="{ name: 'my-tickets' }" class="d-inline-block mb-3 small">← 返回工單列表</RouterLink>

    <div v-if="loading" class="text-muted">載入中…</div>
    <div v-else-if="errorMessage" class="alert alert-danger py-2">{{ errorMessage }}</div>

    <div v-else-if="ticket">
      <div class="d-flex justify-content-between align-items-start mb-3">
        <div>
          <div class="font-monospace text-muted small">{{ ticket.workOrderNo }}</div>
          <h3 class="mb-0">{{ ticket.title }}</h3>
        </div>
        <span :class="['badge', statusBadgeClass(ticket.status)]">{{ statusLabel(ticket.status) }}</span>
      </div>

      <dl class="row">
        <dt class="col-sm-3">類別</dt>
        <dd class="col-sm-9">{{ ticket.categoryName }} / {{ ticket.subCategoryName }}</dd>

        <dt class="col-sm-3">位置</dt>
        <dd class="col-sm-9">{{ ticket.locationDetail }}</dd>

        <dt class="col-sm-3">聯絡電話</dt>
        <dd class="col-sm-9">{{ ticket.contactPhone || '—' }}</dd>

        <dt class="col-sm-3">描述</dt>
        <dd class="col-sm-9" style="white-space: pre-wrap;">{{ ticket.description || '—' }}</dd>

        <dt class="col-sm-3">報修人</dt>
        <dd class="col-sm-9">{{ ticket.creatorName }}</dd>

        <dt class="col-sm-3">建立時間</dt>
        <dd class="col-sm-9">{{ formatTime(ticket.createdTime) }}</dd>
      </dl>

      <div v-if="actionMessage" class="alert alert-success py-2">{{ actionMessage }}</div>
      <div v-if="actionError" class="alert alert-danger py-2">{{ actionError }}</div>

      <button
        v-if="canAccept"
        class="btn btn-primary"
        :disabled="accepting"
        @click="handleAccept"
      >
        {{ accepting ? '處理中…' : '驗收工單' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getWorkOrderById, acceptWorkOrder } from '@/api/workOrder.js'
import { useAuthStore } from '@/stores/auth.js'

const route = useRoute()
const authStore = useAuthStore()
const ticket = ref(null)
const loading = ref(true)
const errorMessage = ref('')
const accepting = ref(false)
const actionMessage = ref('')
const actionError = ref('')

const STATUS_LABEL_MAP = {
  PENDING_REVIEW: '待審核',
  IN_PROGRESS: '進行中',
  PENDING_USER_ACCEPTANCE: '待使用者驗收',
  PENDING_ADMIN_ACCEPTANCE: '待管理員驗收',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
}

const STATUS_BADGE_MAP = {
  PENDING_REVIEW: 'text-bg-secondary',
  IN_PROGRESS: 'text-bg-primary',
  PENDING_USER_ACCEPTANCE: 'text-bg-warning',
  PENDING_ADMIN_ACCEPTANCE: 'text-bg-warning',
  COMPLETED: 'text-bg-success',
  CANCELLED: 'text-bg-light',
}

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

const canAccept = computed(() =>
  ticket.value
  && ticket.value.creatorUserId === authStore.userId
  && ticket.value.status === 'PENDING_USER_ACCEPTANCE'
)

async function loadTicket() {
  ticket.value = await getWorkOrderById(route.params.id)
}

onMounted(async () => {
  try {
    await loadTicket()
  } catch (error) {
    errorMessage.value = error.response?.data?.message || '找不到這張工單，或無法載入'
  } finally {
    loading.value = false
  }
})

async function handleAccept() {
  accepting.value = true
  actionMessage.value = ''
  actionError.value = ''
  try {
    await acceptWorkOrder(route.params.id, authStore.userId)
    await loadTicket()
    actionMessage.value = '工單已驗收完成'
  } catch (error) {
    actionError.value = error.response?.data?.message || '驗收失敗，請稍後再試'
  } finally {
    accepting.value = false
  }
}
</script>
