<template>
  <div class="ticket-detail-view">
    <RouterLink :to="{ name: backTarget }" class="d-inline-block mb-3 small"
      >← 返回工單列表</RouterLink
    >

    <div v-if="loading" class="text-muted">載入中…</div>
    <div v-else-if="errorMessage" class="alert alert-danger py-2">
      {{ errorMessage }}
    </div>

    <div v-else-if="ticket">
      <div class="row g-4 gx-5">
        <!-- 左側主要內容 -->
        <div class="col-12 ticket-main-column">
          <div class="card shadow-sm">
            <div class="card-body">
              <div class="mb-3">
                <div>
                  <div
                    class="d-flex justify-content-between align-items-center gap-3 mb-2"
                  >
                    <div class="font-monospace text-muted small">
                      {{ ticket.workOrderNo }}
                    </div>
                    <div class="small text-muted text-md-end">
                      建立於：{{ formatTime(ticket.createdTime) }}
                    </div>
                  </div>

                  <h3 class="mb-2">{{ ticket.title }}</h3>

                  <span
                    :class="[
                      'badge',
                      'ticket-chip',
                      statusBadgeClass(ticket.status),
                    ]"
                  >
                    <i class="bi bi-circle-fill me-1 ticket-chip-dot"></i>
                    {{ statusLabel(ticket.status) }}
                  </span>

                  <div class="d-flex flex-wrap gap-2 mt-2 small text-muted">
                    <span>{{ ticket.categoryName || "未分類" }}</span>

                    <span v-if="canViewPriority">
                      <span aria-hidden="true">｜</span>
                      優先級：{{ ticket.priorityName || "未設定" }}
                    </span>
                  </div>
                </div>
              </div>

              <section class="border-top border-bottom py-3 mb-3">
                <!-- 報修地點 -->
                <div class="mb-3">
                  <div class="small text-muted mb-1">報修地點</div>
                  <div class="fw-semibold">
                    {{ ticket.locationDetail || "未提供" }}
                  </div>
                </div>

                <!-- 問題描述 -->
                <div
                  class="ticket-description rounded p-3 mb-2"
                  style="white-space: pre-wrap"
                >
                  <div class="text-muted mb-1">問題描述</div>
                  <div class="small">
                    {{ ticket.description || "無" }}
                  </div>
                </div>

                <div
                  v-if="progressNotice"
                  :class="[
                    'alert',
                    'd-flex',
                    'align-items-start',
                    'gap-3',
                    'mb-3',
                    progressNotice.alertClass,
                  ]"
                  role="status"
                >
                  <i
                    :class="['bi', progressNotice.iconClass, 'fs-5']"
                    aria-hidden="true"
                  ></i>
                  <div>
                    <div class="fw-semibold">
                      {{ progressNotice.title }}
                    </div>
                    <div class="small mt-1">{{ progressNotice.message }}</div>
                  </div>
                </div>
              </section>

              <div class="mb-3">
                <h5>附件</h5>
                <div v-if="attachmentsLoading" class="text-muted small">
                  載入附件中…
                </div>
                <div v-else-if="!attachments.length" class="text-muted small">
                  無附件
                </div>
                <div v-else class="d-flex flex-wrap gap-3">
                  <div
                    v-for="att in attachments"
                    :key="att.attachmentId"
                    class="text-center"
                    style="width: 120px"
                  >
                    <button
                      v-if="previewUrls[att.attachmentId]"
                      type="button"
                      class="btn p-0 border-0 attachment-preview-button"
                      data-bs-toggle="modal"
                      data-bs-target="#imagePreviewModal"
                      :aria-label="`放大查看 ${att.originalFileName}`"
                      @click="
                        openImagePreview(
                          previewUrls[att.attachmentId],
                          att.originalFileName
                        )
                      "
                    >
                      <img
                        :src="previewUrls[att.attachmentId]"
                        :alt="att.originalFileName"
                        class="img-thumbnail"
                        style="width: 120px; height: 120px; object-fit: cover"
                      />
                    </button>

                    <div
                      v-else
                      class="border rounded d-flex align-items-center justify-content-center text-muted small"
                      style="width: 120px; height: 120px"
                    >
                      載入中…
                    </div>
                    <div
                      class="small text-truncate mt-1"
                      :title="att.originalFileName"
                    >
                      {{ att.originalFileName }}
                    </div>
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

              <div v-if="actionMessage" class="alert alert-success py-2">
                {{ actionMessage }}
              </div>
              <div v-if="actionError" class="alert alert-danger py-2">
                {{ actionError }}
              </div>
            </div>
          </div>
        </div>
        <!-- 右側案件資訊 -->
        <aside class="col-12 ticket-info-column">
          <div class="card shadow-sm">
            <div class="card-body">
              <h6 class="card-title mb-3">案件資訊</h6>

              <div>
                <!-- 負責工程師 -->
                <div
                  class="d-flex justify-content-between align-items-center gap-3 py-3 border-top small"
                >
                  <div class="text-muted">負責工程師</div>
                  <div class="fw-semibold">
                    {{ ticket.assignedHandlerName || "尚未指派" }}
                  </div>
                  <div
                    v-if="ticket.assignedHandlerDepartment"
                    class="small text-muted mt-1"
                  >
                    {{ ticket.assignedHandlerDepartment }}
                  </div>
                </div>
                <!-- 預計完成時間 -->
                <div
                  class="d-flex justify-content-between align-items-center gap-3 py-3 border-top small"
                >
                  <div class="text-muted text-nowrap">預計完成時間</div>
                  <div class="fw-semibold text-end text-nowrap">
                    {{ progressNoticeFormatTime(ticket.dueTime) }}
                  </div>
                </div>
                <!-- 報修人 -->
                <div
                  class="d-flex justify-content-between align-items-center gap-3 py-3 border-top small"
                >
                  <div class="text-muted">報修人</div>
                  <div class="fw-semibold">
                    {{ ticket.creatorName }}
                  </div>
                </div>
                <!-- 報修人電話 -->
                <div
                  class="d-flex justify-content-between align-items-center gap-3 py-3 border-top small"
                >
                  <div class="text-muted">聯絡電話</div>
                  <div class="fw-semibold">
                    {{ ticket.contactPhone || "—" }}
                  </div>
                </div>
                <!-- 驗收按鈕 -->
                <div v-if="canAccept" class="border-top pt-3">
                  <button
                    type="button"
                    class="btn btn-primary w-100 fw-semibold"
                    :disabled="accepting"
                    @click="handleAccept"
                  >
                    {{ accepting ? "處理中…" : "確認驗收" }}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </aside>
      </div>
    </div>

    <!-- 報修圖片與留言圖片共用的放大預覽視窗 -->
    <div
      id="imagePreviewModal"
      class="modal fade"
      tabindex="-1"
      aria-labelledby="imagePreviewModalLabel"
      aria-hidden="true"
    >
      <div class="modal-dialog modal-dialog-centered modal-xl">
        <div class="modal-content">
          <div class="modal-header">
            <h5 id="imagePreviewModalLabel" class="modal-title">
              {{ selectedImageName || "圖片預覽" }}
            </h5>
            <button
              type="button"
              class="btn-close"
              data-bs-dismiss="modal"
              aria-label="關閉圖片預覽"
            ></button>
          </div>
          <div class="modal-body text-center bg-dark-subtle">
            <img
              v-if="selectedImageUrl"
              :src="selectedImageUrl"
              :alt="selectedImageName || '放大圖片'"
              class="image-preview-large"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue";
import { useRoute } from "vue-router";
import {
  userCheckAccept,
  getAttachments,
  getAttachmentPreview,
  deleteAttachment,
} from "@/api/workOrder.js";
import { useAuthStore } from "@/stores/auth.js";
import { getWorkOrderDetail } from "@/api/workOrderDetail.js";
import { statusBadgeClass, statusLabel } from "@/constants/workOrderStatus.js";

const route = useRoute();
const authStore = useAuthStore();
const ticket = ref(null);
const loading = ref(true);
const errorMessage = ref("");
const accepting = ref(false);
const actionMessage = ref("");
const actionError = ref("");
const attachments = ref([]);
const attachmentsLoading = ref(false);
const previewUrls = ref({});
const selectedImageUrl = ref("");
const selectedImageName = ref("");

const VALID_BACK_TARGETS = ["ticket-list", "my-tickets"];
const backTarget = computed(() =>
  VALID_BACK_TARGETS.includes(route.query.from)
    ? route.query.from
    : "ticket-list"
);

// 狀態提示訊息
const STATUS_NOTICE_MAP = {
  // 待審核
  PENDING_REVIEW: {
    title: "工單已送出，等待審核",
    message: "管理員審核後，將安排負責工程師。",
    alertClass: "alert-secondary",
    iconClass: "bi-hourglass-split",
  },
  // 進行中
  IN_PROGRESS: {
    title: "工程師處理中",
    message: "案件目前正在維修中，請留意後續進度。",
    alertClass: "alert-primary",
    iconClass: "bi-tools",
  },

  // 待使用者驗收
  PENDING_USER_ACCEPTANCE: {
    title: "維修已完成，等待使用者驗收",
    message: "等待報修人確認設備是否已恢復正常。",
    alertClass: "alert-warning",
    iconClass: "bi-clipboard-check",
  },

  // 待管理員驗收
  PENDING_ADMIN_ACCEPTANCE: {
    title: "等待管理員驗收",
    message: "案件已由使用者驗收，等待管理員確認。",
    alertClass: "alert-warning",
    iconClass: "bi-shield-check",
  },

  // 取消
  CANCELLED: {
    title: "案件已取消",
    alertClass: "alert-danger",
    iconClass: "bi-x-circle-fill",
  },

  // 完成
  COMPLETED: {
    title: "案件已完成",
    alertClass: "alert-success",
    iconClass: "bi-check-circle-fill",
  },
};

const progressNotice = computed(() => {
  if (!ticket.value) return null;

  return STATUS_NOTICE_MAP[ticket.value.status] || null;
});

function formatTime(value) {
  if (!value) return "—";
  return value.replace("T", " ").slice(0, 19);
}

function progressNoticeFormatTime(value) {
  if (!value) return "—";
  return value.replace("T", " ").slice(0, 16);
}

const canAccept = computed(
  () =>
    ticket.value &&
    ticket.value.creatorUserId === authStore.userId &&
    ticket.value.status === "PENDING_USER_ACCEPTANCE"
);

const canViewPriority = computed(
  () => authStore.hasRole("ADMIN") || authStore.hasRole("HANDLER")
);

async function loadTicket() {
  ticket.value = await getWorkOrderDetail(route.params.id);
}

function revokePreviewUrls() {
  Object.values(previewUrls.value).forEach((url) => URL.revokeObjectURL(url));
  previewUrls.value = {};
}

async function loadAttachments() {
  attachmentsLoading.value = true;
  revokePreviewUrls();
  try {
    attachments.value = await getAttachments(route.params.id);
    await Promise.all(
      attachments.value.map(async (att) => {
        const blob = await getAttachmentPreview(att.attachmentId);
        previewUrls.value[att.attachmentId] = URL.createObjectURL(blob);
      })
    );
  } catch (error) {
    // 附件載入失敗不擋主要頁面顯示，僅該區塊維持「無附件」
    attachments.value = [];
  } finally {
    attachmentsLoading.value = false;
  }
}

async function handleDeleteAttachment(attachmentId) {
  try {
    await deleteAttachment(attachmentId);
    await loadAttachments();
  } catch (error) {
    actionError.value =
      error.response?.data?.message || "附件刪除失敗，請稍後再試";
  }
}

function openImagePreview(imageUrl, imageName) {
  selectedImageUrl.value = imageUrl;
  selectedImageName.value = imageName;
}

onMounted(async () => {
  try {
    await loadTicket();
    await loadAttachments();
  } catch (error) {
    errorMessage.value =
      error.response?.data?.message || "找不到這張工單，或無法載入";
  } finally {
    loading.value = false;
  }
});

onUnmounted(() => {
  revokePreviewUrls();
});

async function handleAccept() {
  accepting.value = true;
  actionMessage.value = "";
  actionError.value = "";
  try {
    await userCheckAccept(route.params.id, { feedback: null });
    await loadTicket();
    actionMessage.value = "工單已驗收完成";
  } catch (error) {
    actionError.value = error.response?.data?.message || "驗收失敗，請稍後再試";
  } finally {
    accepting.value = false;
  }
}
</script>
<style scoped>
.attachment-preview-button {
  cursor: zoom-in;
}

.attachment-preview-button:focus-visible {
  outline: 3px solid rgba(var(--bs-primary-rgb), 0.35);
  outline-offset: 2px;
}

.image-preview-large {
  display: block;
  width: auto;
  max-width: 100%;
  max-height: 80vh;
  margin: 0 auto;
  object-fit: contain;
}

.ticket-description {
  background-color: #f5f6f8;
}

@media (min-width: 1200px) {
  .ticket-main-column {
    flex: 1 1 0;
    width: auto;
    min-width: 0;
  }

  .ticket-info-column {
    flex: 0 0 380px;
    width: 380px;
  }
}
</style>
