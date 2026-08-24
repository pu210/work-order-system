<template>
  <div class="ticket-detail-view">
    <RouterLink :to="{ name: backTarget }" class="d-inline-block mb-3 small">← 返回工單列表</RouterLink>

    <div v-if="loading" class="text-muted">載入中…</div>
    <div v-else-if="errorMessage" class="alert alert-danger py-2">{{ errorMessage }}</div>

    <div v-else-if="ticket">
      <div class="d-flex justify-content-between align-items-start mb-3">
        <div>
          <div class="font-monospace text-muted small">{{ ticket.workOrderNo }}</div>
          <h3 class="mb-0">{{ ticket.title }}</h3>
        </div>
        <div class="d-flex align-items-center gap-2">
          <span :class="['badge', statusBadgeClass(ticket.status)]">{{ statusLabel(ticket.status) }}</span>
          <button
            v-if="canEngineerEdit"
            type="button"
            class="btn btn-outline-primary btn-sm"
            @click="openEngineerEditor"
          >
            <i class="bi bi-pencil-square me-1"></i>編輯處理結果
          </button>
          <button
            v-if="canAdminReview"
            type="button"
            class="btn btn-outline-primary btn-sm"
            :disabled="reviewAccessLoading"
            @click="openReviewEditor"
          >
            <span
              v-if="reviewAccessLoading"
              class="spinner-border spinner-border-sm me-1"
            ></span>
            <i v-else class="bi bi-person-check me-1"></i>{{ isReReview ? '重新審查與派工' : '審核與派工' }}
          </button>
          <button
            v-if="canAdminCheck"
            type="button"
            class="btn btn-outline-success btn-sm"
            @click="openAdminCheckEditor"
          >
            <i class="bi bi-clipboard-check me-1"></i>管理員驗收
          </button>
        </div>
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

        <dt class="col-sm-3">負責管理員</dt>
        <dd class="col-sm-9">{{ ticket.adminName || '尚未指定' }}</dd>

        <dt class="col-sm-3">完成期限</dt>
        <dd class="col-sm-9">{{ ticket.dueTime ? formatTime(ticket.dueTime) : '尚未設定' }}</dd>

        <dt class="col-sm-3">逾期狀態</dt>
        <dd class="col-sm-9">
          <span :class="['badge', overdueBadgeClass(ticket)]">
            {{ overdueLabel(ticket) }}
          </span>
        </dd>

        <dt class="col-sm-3">建立時間</dt>
        <dd class="col-sm-9">{{ formatTime(ticket.createdTime) }}</dd>
      </dl>

      <div class="mb-3">
        <h5>附件</h5>
        <div v-if="attachmentsLoading" class="text-muted small">載入附件中…</div>
        <div v-else-if="!attachments.length" class="text-muted small">無附件</div>
        <div v-else class="d-flex flex-wrap gap-3">
          <div v-for="att in attachments" :key="att.attachmentId" class="text-center" style="width: 120px;">
            <img
              v-if="previewUrls[att.attachmentId]"
              :src="previewUrls[att.attachmentId]"
              :alt="att.originalFileName"
              class="img-thumbnail"
              style="width: 120px; height: 120px; object-fit: cover;"
            />
            <div v-else class="border rounded d-flex align-items-center justify-content-center text-muted small" style="width: 120px; height: 120px;">
              載入中…
            </div>
            <div class="small text-truncate mt-1" :title="att.originalFileName">{{ att.originalFileName }}</div>
            <button
              v-if="att.uploadedUserId === authStore.userId"
              type="button"
              class="btn btn-sm btn-link text-danger p-0"
              @click="handleDeleteAttachment(att.attachmentId)"
            >
              刪除
            </button>
          </div>
        </div>
      </div>

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

    <div v-if="engineerEditorOpen" class="modal-backdrop fade show"></div>
    <div
      v-if="engineerEditorOpen"
      class="modal fade show d-block"
      tabindex="-1"
      role="dialog"
      aria-modal="true"
      aria-labelledby="engineer-editor-title"
      @click.self="closeEngineerEditor"
    >
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 rounded-4 shadow-lg">
          <div class="modal-header px-4 py-3">
            <div>
              <h5 id="engineer-editor-title" class="modal-title fw-bold">工程處理回報</h5>
              <div class="small text-muted mt-1">
                {{ ticket?.workOrderNo }}・{{ ticket?.title }}
              </div>
            </div>
            <button
              type="button"
              class="btn-close"
              aria-label="關閉"
              :disabled="engineerSubmitting !== ''"
              @click="closeEngineerEditor"
            ></button>
          </div>

          <div class="modal-body px-4 py-4">
            <div class="mb-3">
              <label for="engineer-target-no" class="form-label fw-semibold">
                設備編號 <span class="text-danger">*</span>
              </label>
              <input
                id="engineer-target-no"
                v-model.trim="engineerTargetNo"
                type="text"
                class="form-control"
                maxlength="100"
                placeholder="請輸入設備編號，例如 EQ-001"
                :disabled="engineerSubmitting !== ''"
              />
              <div class="form-text">完成維修時必填；退回管理員時可留空。</div>
            </div>

            <div>
              <label for="engineer-feedback" class="form-label fw-semibold">處理反饋</label>
              <textarea
                id="engineer-feedback"
                v-model.trim="engineerFeedback"
                class="form-control"
                rows="5"
                maxlength="1000"
                placeholder="請填寫處理結果；若要退回管理員，請說明原因"
                :disabled="engineerSubmitting !== ''"
              ></textarea>
              <div class="form-text">退回管理員時必填。</div>
            </div>

            <div v-if="engineerEditorError" class="alert alert-danger py-2 mt-3 mb-0">
              {{ engineerEditorError }}
            </div>
          </div>

          <div class="modal-footer px-4 py-3">
            <button
              type="button"
              class="btn btn-outline-secondary me-auto"
              :disabled="engineerSubmitting !== ''"
              @click="closeEngineerEditor"
            >
              取消
            </button>
            <button
              type="button"
              class="btn btn-outline-danger"
              :disabled="engineerSubmitting !== ''"
              @click="handleEngineerReject"
            >
              <span
                v-if="engineerSubmitting === 'reject'"
                class="spinner-border spinner-border-sm me-1"
              ></span>
              退回管理員
            </button>
            <button
              type="button"
              class="btn btn-primary"
              :disabled="engineerSubmitting !== ''"
              @click="handleEngineerComplete"
            >
              <span
                v-if="engineerSubmitting === 'complete'"
                class="spinner-border spinner-border-sm me-1"
              ></span>
              完成維修
            </button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="reviewEditorOpen" class="modal-backdrop fade show"></div>
    <div
      v-if="reviewEditorOpen"
      class="modal fade show d-block"
      tabindex="-1"
      role="dialog"
      aria-modal="true"
      aria-labelledby="review-editor-title"
      @click.self="closeReviewEditor"
    >
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content border-0 rounded-4 shadow-lg">
          <div class="modal-header px-4 py-3">
            <div>
              <h5 id="review-editor-title" class="modal-title fw-bold">
                {{ isReReview ? '工單重新審查與派工' : '工單審核與派工' }}
              </h5>
              <div class="small text-muted mt-1">{{ ticket?.workOrderNo }}・{{ ticket?.title }}</div>
            </div>
            <button
              type="button"
              class="btn-close"
              aria-label="關閉"
              :disabled="reviewSubmitting !== '' || reviewEditorLoading"
              @click="closeReviewEditor"
            ></button>
          </div>

          <div class="modal-body px-4 py-4">
            <div v-if="reviewEditorLoading" class="text-muted py-4 text-center">
              <span class="spinner-border spinner-border-sm me-2"></span>正在取得審核權限與表單資料…
            </div>

            <div v-else class="row g-3">
              <div class="col-12 col-md-6">
                <label for="review-priority" class="form-label fw-semibold">優先級</label>
                <select
                  id="review-priority"
                  v-model="reviewForm.priorityId"
                  class="form-select"
                  :disabled="reviewSubmitting !== ''"
                >
                  <option value="">請選擇優先級</option>
                  <option
                    v-for="priority in priorities"
                    :key="priority.prioritiesId"
                    :value="priority.prioritiesId"
                  >
                    {{ priority.name }}
                  </option>
                </select>
              </div>

              <div class="col-12 col-md-6">
                <label for="review-handler" class="form-label fw-semibold">指派工程師</label>
                <select
                  id="review-handler"
                  v-model="reviewForm.assignedHandlerId"
                  class="form-select"
                  :disabled="reviewSubmitting !== ''"
                >
                  <option value="">請選擇工程師</option>
                  <option v-for="handler in handlers" :key="handler.userId" :value="handler.userId">
                    {{ handler.name }}
                  </option>
                </select>
              </div>

              <div class="col-12">
                <label for="review-due-time" class="form-label fw-semibold">預計完成時間</label>
                <input
                  id="review-due-time"
                  v-model="reviewForm.dueTime"
                  type="datetime-local"
                  class="form-control"
                  :disabled="reviewSubmitting !== ''"
                />
              </div>

              <div class="col-12">
                <label for="review-feedback" class="form-label fw-semibold">審核反饋</label>
                <textarea
                  id="review-feedback"
                  v-model.trim="reviewForm.feedback"
                  class="form-control"
                  rows="4"
                  maxlength="1000"
                  placeholder="可填寫派工說明；駁回工單時必填"
                  :disabled="reviewSubmitting !== ''"
                ></textarea>
              </div>
            </div>

            <div v-if="reviewEditorError" class="alert alert-danger py-2 mt-3 mb-0">
              {{ reviewEditorError }}
            </div>
          </div>

          <div class="modal-footer px-4 py-3">
            <button
              type="button"
              class="btn btn-outline-secondary me-auto"
              :disabled="reviewSubmitting !== '' || reviewEditorLoading"
              @click="closeReviewEditor"
            >
              取消
            </button>
            <button
              type="button"
              class="btn btn-outline-danger"
              :disabled="reviewSubmitting !== '' || reviewEditorLoading"
              @click="handleReviewReject"
            >
              <span v-if="reviewSubmitting === 'reject'" class="spinner-border spinner-border-sm me-1"></span>
              駁回工單
            </button>
            <button
              type="button"
              class="btn btn-primary"
              :disabled="reviewSubmitting !== '' || reviewEditorLoading"
              @click="handleReviewAccept"
            >
              <span v-if="reviewSubmitting === 'accept'" class="spinner-border spinner-border-sm me-1"></span>
              通過並派工
            </button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="adminCheckEditorOpen" class="modal-backdrop fade show"></div>
    <div
      v-if="adminCheckEditorOpen"
      class="modal fade show d-block"
      tabindex="-1"
      role="dialog"
      aria-modal="true"
      aria-labelledby="admin-check-title"
      @click.self="closeAdminCheckEditor"
    >
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 rounded-4 shadow-lg">
          <div class="modal-header px-4 py-3">
            <div>
              <h5 id="admin-check-title" class="modal-title fw-bold">管理員最終驗收</h5>
              <div class="small text-muted mt-1">{{ ticket?.workOrderNo }}・{{ ticket?.title }}</div>
            </div>
            <button
              type="button"
              class="btn-close"
              aria-label="關閉"
              :disabled="adminCheckSubmitting !== ''"
              @click="closeAdminCheckEditor"
            ></button>
          </div>

          <div class="modal-body px-4 py-4">
            <label for="admin-check-feedback" class="form-label fw-semibold">驗收反饋</label>
            <textarea
              id="admin-check-feedback"
              v-model.trim="adminCheckFeedback"
              class="form-control"
              rows="5"
              maxlength="1000"
              placeholder="驗收通過時可留空；退回工程師時請填寫原因"
              :disabled="adminCheckSubmitting !== ''"
            ></textarea>
            <div class="form-text">退回工程師時必填。</div>

            <div v-if="adminCheckError" class="alert alert-danger py-2 mt-3 mb-0">
              {{ adminCheckError }}
            </div>
          </div>

          <div class="modal-footer px-4 py-3">
            <button
              type="button"
              class="btn btn-outline-secondary me-auto"
              :disabled="adminCheckSubmitting !== ''"
              @click="closeAdminCheckEditor"
            >
              取消
            </button>
            <button
              type="button"
              class="btn btn-outline-danger"
              :disabled="adminCheckSubmitting !== ''"
              @click="handleAdminCheckReject"
            >
              <span v-if="adminCheckSubmitting === 'reject'" class="spinner-border spinner-border-sm me-1"></span>
              退回工程師
            </button>
            <button
              type="button"
              class="btn btn-success"
              :disabled="adminCheckSubmitting !== ''"
              @click="handleAdminCheckAccept"
            >
              <span v-if="adminCheckSubmitting === 'accept'" class="spinner-border spinner-border-sm me-1"></span>
              確認完成
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import {
  getWorkOrderById,
  userCheckAccept,
  getAttachments,
  getAttachmentPreview,
  deleteAttachment,
  progressAccept,
  progressReject,
  startEditSession,
  editSessionHeartbeat,
  releaseEditSession,
  reviewAccept,
  reviewReject,
  adminCheckAccept,
  adminCheckReject,
} from '@/api/workOrder.js'
import { useAuthStore } from '@/stores/auth.js'
import { getUsers } from '@/api/user.js'
import { getPriorities } from '@/api/priority.js'
import { notify } from '@/plugins/notify.js'

const route = useRoute()
const authStore = useAuthStore()
const ticket = ref(null)
const loading = ref(true)
const errorMessage = ref('')
const accepting = ref(false)
const actionMessage = ref('')
const actionError = ref('')
const attachments = ref([])
const attachmentsLoading = ref(false)
const previewUrls = ref({})
const engineerEditorOpen = ref(false)
const engineerTargetNo = ref('')
const engineerFeedback = ref('')
const engineerEditorError = ref('')
const engineerSubmitting = ref('')
const reviewEditorOpen = ref(false)
const reviewAccessLoading = ref(false)
const reviewEditorLoading = ref(false)
const reviewEditorError = ref('')
const reviewSubmitting = ref('')
const reviewSessionToken = ref(null)
const reviewHeartbeatTimer = ref(null)
const handlers = ref([])
const priorities = ref([])
const reviewForm = reactive({
  priorityId: '',
  assignedHandlerId: '',
  dueTime: '',
  feedback: '',
})
const adminCheckEditorOpen = ref(false)
const adminCheckFeedback = ref('')
const adminCheckError = ref('')
const adminCheckSubmitting = ref('')

const VALID_BACK_TARGETS = ['ticket-list', 'my-tickets', 'handler-workbench', 'ticket-assign']
const backTarget = computed(() =>
  VALID_BACK_TARGETS.includes(route.query.from) ? route.query.from : 'ticket-list'
)

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

function overdueLabel(currentTicket) {
  if (['COMPLETED', 'CANCELLED'].includes(currentTicket.status)) return '不適用'
  if (!currentTicket.dueTime) return '尚未設定'
  return currentTicket.isOverdue ? '已逾期' : '未逾期'
}

function overdueBadgeClass(currentTicket) {
  if (['COMPLETED', 'CANCELLED'].includes(currentTicket.status) || !currentTicket.dueTime) {
    return 'text-bg-secondary'
  }
  return currentTicket.isOverdue ? 'text-bg-danger' : 'text-bg-success'
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

const canEngineerEdit = computed(() =>
  ticket.value
  && ticket.value.status === 'IN_PROGRESS'
  && authStore.hasRole('HANDLER')
)

function isSameUser(userId) {
  if (userId == null || authStore.userId == null) return false
  return Number(userId) === Number(authStore.userId)
}

const isFirstReview = computed(() =>
  ticket.value
  && ticket.value.status === 'PENDING_REVIEW'
  && ticket.value.adminUserId == null
)

const isReReview = computed(() =>
  ticket.value
  && ticket.value.status === 'PENDING_REVIEW'
  && isSameUser(ticket.value.adminUserId)
)

const canAdminReview = computed(() =>
  ticket.value
  && ticket.value.status === 'PENDING_REVIEW'
  && authStore.hasRole('ADMIN')
  && (isFirstReview.value || isReReview.value)
)

const canAdminCheck = computed(() =>
  ticket.value
  && ticket.value.status === 'PENDING_ADMIN_ACCEPTANCE'
  && authStore.hasRole('ADMIN')
  && isSameUser(ticket.value.adminUserId)
)

async function loadTicket() {
  ticket.value = await getWorkOrderById(route.params.id)
}

function revokePreviewUrls() {
  Object.values(previewUrls.value).forEach((url) => URL.revokeObjectURL(url))
  previewUrls.value = {}
}

async function loadAttachments() {
  attachmentsLoading.value = true
  revokePreviewUrls()
  try {
    attachments.value = await getAttachments(route.params.id)
    await Promise.all(
      attachments.value.map(async (att) => {
        const blob = await getAttachmentPreview(att.attachmentId)
        previewUrls.value[att.attachmentId] = URL.createObjectURL(blob)
      })
    )
  } catch (error) {
    // 附件載入失敗不擋主要頁面顯示，僅該區塊維持「無附件」
    attachments.value = []
  } finally {
    attachmentsLoading.value = false
  }
}

async function handleDeleteAttachment(attachmentId) {
  try {
    await deleteAttachment(attachmentId)
    await loadAttachments()
  } catch (error) {
    actionError.value = error.response?.data?.message || '附件刪除失敗，請稍後再試'
  }
}

onMounted(async () => {
  try {
    await loadTicket()
    await loadAttachments()
  } catch (error) {
    errorMessage.value = error.response?.data?.message || '找不到這張工單，或無法載入'
  } finally {
    loading.value = false
  }
})

onUnmounted(() => {
  revokePreviewUrls()
  stopReviewHeartbeat()
  void releaseReviewLock()
})

async function handleAccept() {
  accepting.value = true
  actionMessage.value = ''
  actionError.value = ''
  try {
    await userCheckAccept(route.params.id, { feedback: '' })
    await loadTicket()
    actionMessage.value = '工單已驗收完成'
  } catch (error) {
    actionError.value = error.response?.data?.message || '驗收失敗，請稍後再試'
  } finally {
    accepting.value = false
  }
}

function openEngineerEditor() {
  engineerTargetNo.value = ''
  engineerFeedback.value = ''
  engineerEditorError.value = ''
  engineerEditorOpen.value = true
}

function closeEngineerEditor() {
  if (engineerSubmitting.value) return
  engineerEditorOpen.value = false
  engineerTargetNo.value = ''
  engineerFeedback.value = ''
  engineerEditorError.value = ''
}

async function handleEngineerComplete() {
  if (!engineerTargetNo.value) {
    engineerEditorError.value = '完成維修前，請先輸入設備編號。'
    return
  }

  engineerSubmitting.value = 'complete'
  engineerEditorError.value = ''

  try {
    await progressAccept(route.params.id, {
      targetNo: engineerTargetNo.value,
      feedback: engineerFeedback.value || null,
    })

    engineerEditorOpen.value = false
    await loadTicket()
    actionMessage.value = '已回報完成，等待使用者驗收。'
  } catch (error) {
    engineerEditorError.value = error.response?.data?.message || '完成維修失敗，請稍後再試。'
  } finally {
    engineerSubmitting.value = ''
  }
}

async function handleEngineerReject() {
  if (!engineerFeedback.value) {
    engineerEditorError.value = '退回管理員前，請填寫退回原因。'
    return
  }

  engineerSubmitting.value = 'reject'
  engineerEditorError.value = ''

  try {
    await progressReject(route.params.id, {
      feedback: engineerFeedback.value,
    })

    engineerEditorOpen.value = false
    await loadTicket()
    actionMessage.value = '工單已退回管理員重新審查。'
  } catch (error) {
    engineerEditorError.value = error.response?.data?.message || '退回管理員失敗，請稍後再試。'
  } finally {
    engineerSubmitting.value = ''
  }
}

function resetReviewForm() {
  reviewForm.priorityId = ''
  reviewForm.assignedHandlerId = ''
  reviewForm.dueTime = toDateTimeLocal(ticket.value?.dueTime)
  reviewForm.feedback = ''
}

function toDateTimeLocal(value) {
  if (!value) return ''
  return String(value).slice(0, 16)
}

async function openReviewEditor() {
  if (reviewAccessLoading.value) return

  resetReviewForm()
  reviewEditorError.value = ''
  reviewEditorOpen.value = false
  reviewAccessLoading.value = true
  reviewEditorLoading.value = true
  let editSessionAcquired = false

  try {
    if (isFirstReview.value) {
      const session = await startEditSession(route.params.id)
      reviewSessionToken.value = session.sessionToken
      startReviewHeartbeat()
      editSessionAcquired = true
    } else {
      reviewSessionToken.value = null
      stopReviewHeartbeat()
    }

    const [userResult, priorityResult] = await Promise.all([
      getUsers({
        roleCode: 'HANDLER',
        status: 1,
        page: 0,
        size: 100,
      }),
      getPriorities(),
    ])

    handlers.value = userResult?.content ?? []
    priorities.value = priorityResult ?? []
    reviewEditorLoading.value = false
    reviewEditorOpen.value = true
  } catch (error) {
    if (editSessionAcquired) {
      await releaseReviewLock()
    }

    const message = error.response?.data?.message || '無法開始審核，請稍後再試。'
    const isLocked = error.response?.status === 423

    await notify.alert({
      icon: isLocked ? 'warning' : 'error',
      title: isLocked ? message : '無法開始審核',
      text: isLocked ? '' : message,
    })
  } finally {
    reviewEditorLoading.value = false
    reviewAccessLoading.value = false
  }
}

function startReviewHeartbeat() {
  stopReviewHeartbeat()
  reviewHeartbeatTimer.value = window.setInterval(async () => {
    if (!reviewSessionToken.value) return
    try {
      await editSessionHeartbeat(route.params.id, reviewSessionToken.value)
    } catch (error) {
      stopReviewHeartbeat()
      reviewEditorError.value = error.response?.data?.message || '審核編輯權限已失效，請關閉後重新開啟。'
    }
  }, 60000)
}

function stopReviewHeartbeat() {
  if (reviewHeartbeatTimer.value) {
    window.clearInterval(reviewHeartbeatTimer.value)
    reviewHeartbeatTimer.value = null
  }
}

async function releaseReviewLock() {
  stopReviewHeartbeat()
  const token = reviewSessionToken.value
  reviewSessionToken.value = null
  if (!token) return

  try {
    await releaseEditSession(route.params.id, token)
  } catch {
    // 審核成功時後端可能已先釋放編輯鎖，因此忽略重複釋放錯誤。
  }
}

async function closeReviewEditor() {
  if (reviewSubmitting.value || reviewEditorLoading.value) return
  await releaseReviewLock()
  reviewEditorOpen.value = false
  reviewEditorError.value = ''
  resetReviewForm()
}

async function handleReviewAccept() {
  if (isFirstReview.value && !reviewSessionToken.value) {
    reviewEditorError.value = '尚未取得審核編輯權限，請關閉後重新開啟。'
    return
  }
  if (!reviewForm.priorityId || !reviewForm.assignedHandlerId || !reviewForm.dueTime) {
    reviewEditorError.value = '優先級、指派工程師與預計完成時間皆為必填。'
    return
  }

  reviewSubmitting.value = 'accept'
  reviewEditorError.value = ''

  try {
    await reviewAccept(
      route.params.id,
      {
        priorityId: Number(reviewForm.priorityId),
        assignedHandlerId: Number(reviewForm.assignedHandlerId),
        dueTime: reviewForm.dueTime,
        feedback: reviewForm.feedback || null,
      },
      reviewSessionToken.value,
    )

    await releaseReviewLock()
    reviewEditorOpen.value = false
    resetReviewForm()
    await loadTicket()
    actionMessage.value = '工單審核通過並已指派工程師。'
  } catch (error) {
    reviewEditorError.value = error.response?.data?.message || '工單審核失敗，請稍後再試。'
  } finally {
    reviewSubmitting.value = ''
  }
}

async function handleReviewReject() {
  if (isFirstReview.value && !reviewSessionToken.value) {
    reviewEditorError.value = '尚未取得審核編輯權限，請關閉後重新開啟。'
    return
  }
  if (!reviewForm.feedback) {
    reviewEditorError.value = '駁回工單前，請填寫駁回原因。'
    return
  }

  reviewSubmitting.value = 'reject'
  reviewEditorError.value = ''

  try {
    await reviewReject(
      route.params.id,
      { feedback: reviewForm.feedback },
      reviewSessionToken.value,
    )

    await releaseReviewLock()
    reviewEditorOpen.value = false
    resetReviewForm()
    await loadTicket()
    actionMessage.value = '工單已駁回。'
  } catch (error) {
    reviewEditorError.value = error.response?.data?.message || '駁回工單失敗，請稍後再試。'
  } finally {
    reviewSubmitting.value = ''
  }
}

function openAdminCheckEditor() {
  adminCheckFeedback.value = ''
  adminCheckError.value = ''
  adminCheckEditorOpen.value = true
}

function closeAdminCheckEditor() {
  if (adminCheckSubmitting.value) return
  adminCheckEditorOpen.value = false
  adminCheckFeedback.value = ''
  adminCheckError.value = ''
}

async function handleAdminCheckAccept() {
  adminCheckSubmitting.value = 'accept'
  adminCheckError.value = ''

  try {
    await adminCheckAccept(route.params.id, {
      feedback: adminCheckFeedback.value || null,
    })

    adminCheckEditorOpen.value = false
    await loadTicket()
    actionMessage.value = '管理員驗收完成，工單已結案。'
  } catch (error) {
    adminCheckError.value = error.response?.data?.message || '管理員驗收失敗，請稍後再試。'
  } finally {
    adminCheckSubmitting.value = ''
  }
}

async function handleAdminCheckReject() {
  if (!adminCheckFeedback.value) {
    adminCheckError.value = '退回工程師前，請填寫退回原因。'
    return
  }

  adminCheckSubmitting.value = 'reject'
  adminCheckError.value = ''

  try {
    await adminCheckReject(route.params.id, {
      feedback: adminCheckFeedback.value,
    })

    adminCheckEditorOpen.value = false
    await loadTicket()
    actionMessage.value = '工單已退回工程師重新處理。'
  } catch (error) {
    adminCheckError.value = error.response?.data?.message || '退回工程師失敗，請稍後再試。'
  } finally {
    adminCheckSubmitting.value = ''
  }
}
</script>
