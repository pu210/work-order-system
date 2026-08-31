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
                <h6>附件</h6>
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
                  </div>
                </div>
              </div>

              <!-- 聯繫紀錄 -->
              <section class="border-top py-3 mb-3">
                <div class="d-flex align-items-center gap-2">
                  <h6 class="mb-0">聯繫紀錄</h6>
                  <span
                    class="badge bg-light border text-secondary fw-normal contact-count-badge"
                    >共 {{ contactRecords.length }} 則</span
                  >
                </div>
                <p
                  v-if="contactRecordsLoading"
                  class="mb-0 mt-3 small text-muted"
                >
                  聯繫紀錄載入中...
                </p>
                <p
                  v-else-if="contactRecordsError"
                  class="mb-0 mt-3 small text-danger"
                >
                  {{ contactRecordsError }}
                </p>
                <p
                  v-else-if="!contactRecords.length"
                  class="mb-0 mt-3 small text-muted"
                >
                  尚無聯繫紀錄
                </p>
                <div v-else class="mt-3">
                  <div v-if="hasMoreContactRecords" class="text-center mb-2">
                    <button
                      type="button"
                      class="btn btn-sm btn-outline-secondary"
                      @click="showMoreContactRecords"
                    >
                      <i
                        class="bi bi-clock-history me-1"
                        aria-hidden="true"
                      ></i>
                      顯示更早留言
                    </button>
                  </div>
                  <article
                    v-for="record in visibleContactRecords"
                    :key="record.recordId"
                    class="contact-record-card border rounded p-3 mb-2"
                  >
                    <div
                      class="d-flex justify-content-between align-items-start gap-3"
                    >
                      <div class="d-flex align-items-center flex-wrap gap-2">
                        <span class="fw-semibold small">
                          {{ record.authorUserName }}
                        </span>

                        <span
                          v-for="roleCode in record.authorRoleCodes || []"
                          :key="`${record.recordId}-${roleCode}`"
                          class="badge rounded-pill text-bg-light border text-secondary fw-normal contact-role-badge"
                        >
                          {{ userRoleLabel(roleCode) }}
                        </span>
                      </div>

                      <time class="small text-muted text-nowrap">
                        {{ formatDateTimeToMinute(record.createdTime) }}
                      </time>
                    </div>

                    <p
                      v-if="record.content"
                      class="mb-0 mt-2 small contact-record-content"
                    >
                      {{ record.content }}
                    </p>

                    <!-- 留言附件縮圖 -->
                    <div
                      v-if="record.attachments?.length"
                      class="d-flex flex-wrap gap-2 mt-2"
                    >
                      <div
                        v-for="attachment in record.attachments"
                        :key="attachment.attachmentId"
                      >
                        <button
                          v-if="
                            commentPreviewUrls[
                              commentPreviewKey(
                                record.recordId,
                                attachment.attachmentId
                              )
                            ]
                          "
                          type="button"
                          class="btn p-0 border-0 attachment-preview-button"
                          data-bs-toggle="modal"
                          data-bs-target="#imagePreviewModal"
                          :aria-label="`放大查看 ${attachment.originalFileName}`"
                          @click="
                            openImagePreview(
                              commentPreviewUrls[
                                commentPreviewKey(
                                  record.recordId,
                                  attachment.attachmentId
                                )
                              ],
                              attachment.originalFileName
                            )
                          "
                        >
                          <img
                            :src="
                              commentPreviewUrls[
                                commentPreviewKey(
                                  record.recordId,
                                  attachment.attachmentId
                                )
                              ]
                            "
                            :alt="attachment.originalFileName"
                            class="img-thumbnail comment-attachment-thumbnail"
                          />
                        </button>
                      </div>
                    </div>
                  </article>
                </div>
              </section>

              <form
                class="comment-form border rounded p-3 mb-3"
                @submit.prevent="handleCommentSubmit"
              >
                <h6 class="fw-semibold mb-2">問題尚未解決？</h6>

                <p class="small text-muted mb-3">
                  可補充目前狀況或附上照片，相關人員會持續協助您。
                </p>

                <textarea
                  aria-label="留言內容"
                  id="commentContent"
                  v-model="commentContent"
                  class="form-control"
                  rows="4"
                  maxlength="500"
                  placeholder="例如：設備仍無法正常運作，或描述目前遇到的情況…"
                  :disabled="commentSubmitting"
                ></textarea>

                <div class="small text-muted text-end mt-1">
                  {{ commentContent.length }} / 500
                </div>

                <!-- 留言圖片上傳 -->
                <div
                  class="d-flex flex-wrap justify-content-between align-items-center gap-3 mt-3"
                >
                  <div class="d-flex flex-wrap align-items-center gap-2">
                    <label
                      for="commentFiles"
                      class="btn btn-sm btn-outline-primary"
                      :class="{ disabled: commentSubmitting }"
                    >
                      <i class="bi bi-plus-circle me-1" aria-hidden="true"></i>
                      加入圖片
                    </label>

                    <input
                      id="commentFiles"
                      type="file"
                      class="d-none"
                      accept="image/*"
                      multiple
                      :disabled="commentSubmitting"
                      @change="handleCommentFilesChange"
                    />

                    <span class="small text-muted">
                      已選 {{ commentFiles.length }} / 5 張
                    </span>
                  </div>
                  <button
                    type="submit"
                    class="btn btn-sm btn-primary fw-semibold"
                    :disabled="commentSubmitting"
                  >
                    <span
                      v-if="commentSubmitting"
                      class="spinner-border spinner-border-sm me-1"
                      aria-hidden="true"
                    ></span>

                    {{ commentSubmitting ? "送出中…" : "送出留言" }}
                  </button>
                </div>
                <div
                  v-if="commentFiles.length"
                  class="d-flex flex-column gap-2 mt-3"
                >
                  <div
                    v-for="(file, index) in commentFiles"
                    :key="`${file.name}-${file.size}-${file.lastModified}-${index}`"
                    class="d-flex justify-content-between align-items-center gap-3 border rounded bg-white px-2 py-2 small"
                  >
                    <div class="comment-file-info text-truncate">
                      <i
                        class="bi bi-image me-1 text-muted"
                        aria-hidden="true"
                      ></i>
                      {{ file.name }}
                      <span class="text-muted">
                        （{{ (file.size / 1024 / 1024).toFixed(2) }} MB）
                      </span>
                    </div>

                    <button
                      type="button"
                      class="btn btn-sm btn-link text-danger text-decoration-none p-0"
                      :disabled="commentSubmitting"
                      @click="removeCommentFile(index)"
                    >
                      移除
                    </button>
                  </div>
                </div>

                <div v-if="commentSubmitError" class="small text-danger mt-2">
                  {{ commentSubmitError }}
                </div>
              </form>

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
                    {{ formatDateTimeToMinute(ticket.dueTime) }}
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
                <WorkOrderActionPanel
                  :work-order="ticket"
                  @updated="handleWorkflowUpdated"
                />
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
    <!-- 返回頁面頂端 -->
    <button
      v-if="showScrollTopButton"
      type="button"
      class="btn btn-primary rounded-circle shadow scroll-top-button"
      aria-label="返回頁面頂端"
      title="返回頂端"
      @click="scrollToTop"
    >
      <i class="bi bi-arrow-up scroll-top-icon" aria-hidden="true"></i>
    </button>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from "vue";
import { useRoute } from "vue-router";
import { getAttachments, getAttachmentPreview } from "@/api/workOrder.js";
import { useAuthStore } from "@/stores/auth.js";
import { getWorkOrderDetail } from "@/api/workOrderDetail.js";
import WorkOrderActionPanel from "@/components/work-order/WorkOrderActionPanel.vue";
import { statusBadgeClass, statusLabel } from "@/constants/workOrderStatus.js";
import { userRoleLabel } from "@/constants/userRole.js";
import {
  getContactRecords,
  createContactRecord,
  getContactRecordAttachmentPreview,
} from "@/api/contactRecord.js";

const route = useRoute();
const authStore = useAuthStore();
const ticket = ref(null);
const loading = ref(true);
const errorMessage = ref("");
const attachments = ref([]);
const attachmentsLoading = ref(false);
const previewUrls = ref({});
const commentPreviewUrls = ref({});
const selectedImageUrl = ref("");
const selectedImageName = ref("");
const commentContent = ref("");
const commentFiles = ref([]);
const commentSubmitting = ref(false);
const commentSubmitError = ref("");
const contactRecords = ref([]);
const contactRecordsLoading = ref(false);
const contactRecordsError = ref("");
const visibleContactRecordCount = ref(5);
const visibleContactRecords = computed(() =>
  contactRecords.value.slice(-visibleContactRecordCount.value)
);
const hasMoreContactRecords = computed(
  () => contactRecords.value.length > visibleContactRecordCount.value
);
const VALID_BACK_TARGETS = [
  "ticket-list",
  "my-tickets",
  "ticket-assign",
  "handler-workbench",
];
const backTarget = computed(() =>
  VALID_BACK_TARGETS.includes(route.query.from)
    ? route.query.from
    : "ticket-list"
);
const MAX_COMMENT_IMAGE_COUNT = 5;
const MAX_COMMENT_IMAGE_SIZE = 10 * 1024 * 1024;
const showScrollTopButton = ref(false);
let scrollContainer = null;

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

function formatDateTimeToMinute(value) {
  if (!value) return "—";
  return value.replace("T", " ").slice(0, 16);
}

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

// 釋放留言附件產生的圖片預覽網址，避免重新載入留言時持續占用瀏覽器記憶體
function revokeCommentPreviewUrls() {
  Object.values(commentPreviewUrls.value).forEach((url) => {
    URL.revokeObjectURL(url);
  });

  commentPreviewUrls.value = {};
}

// 建立留言與附件的唯一索引，避免不同留言的附件編號互相混淆
function commentPreviewKey(recordId, attachmentId) {
  return `${recordId}-${attachmentId}`;
}

// 載入所有留言附件，並轉換成瀏覽器可顯示的圖片預覽網址
async function loadCommentPreviewUrls(records) {
  revokeCommentPreviewUrls();

  const loadTasks = records.flatMap((record) =>
    (record.attachments || []).map(async (attachment) => {
      const blob = await getContactRecordAttachmentPreview(
        route.params.id,
        record.recordId,
        attachment.attachmentId
      );

      const key = commentPreviewKey(record.recordId, attachment.attachmentId);

      commentPreviewUrls.value[key] = URL.createObjectURL(blob);
    })
  );

  await Promise.allSettled(loadTasks);
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

// 留言載入
async function loadContactRecords() {
  contactRecordsLoading.value = true;
  contactRecordsError.value = "";

  try {
    contactRecords.value = await getContactRecords(route.params.id);

    await loadCommentPreviewUrls(contactRecords.value);
  } catch (error) {
    contactRecords.value = [];
    revokeCommentPreviewUrls();

    contactRecordsError.value =
      error.response?.data?.message || "留言載入失敗，請稍後再試";
  } finally {
    contactRecordsLoading.value = false;
  }
}

// 每次增加顯示 5 則較早的聯繫紀錄
function showMoreContactRecords() {
  visibleContactRecordCount.value += 5;
}

function openImagePreview(imageUrl, imageName) {
  selectedImageUrl.value = imageUrl;
  selectedImageName.value = imageName;
}

// 頁面向下捲動超過 400px 時，顯示返回頂部按鈕
function handleWindowScroll() {
  showScrollTopButton.value = (scrollContainer?.scrollTop ?? 0) > 400;
}

// 平滑捲動返回頁面頂端
function scrollToTop() {
  scrollContainer?.scrollTo({
    top: 0,
    behavior: "smooth",
  });
}

onMounted(async () => {
  scrollContainer = document.querySelector(".main-content");
  scrollContainer?.addEventListener("scroll", handleWindowScroll, {
    passive: true,
  });
  try {
    await loadTicket();
    await loadAttachments();
    await loadContactRecords();
  } catch (error) {
    errorMessage.value =
      error.response?.data?.message || "找不到這張工單，或無法載入";
  } finally {
    loading.value = false;
  }
});

// 當路由工單 ID 發生變化時 (例如點擊通知彈窗切換工單)，自動重新載入工單詳情
watch(
  () => route.params.id,
  async (newId) => {
    if (newId) {
      loading.value = true;
      errorMessage.value = "";
      try {
        await loadTicket();
        await loadAttachments();
        await loadContactRecords();
      } catch (error) {
        errorMessage.value =
          error.response?.data?.message || "找不到這張工單，或無法載入";
      } finally {
        loading.value = false;
      }
    }
  }
);

onUnmounted(() => {
  scrollContainer?.removeEventListener("scroll", handleWindowScroll);
  scrollContainer = null;

  revokePreviewUrls();
  revokeCommentPreviewUrls();
});

async function handleWorkflowUpdated() {
  await loadTicket();
}
// 選擇並驗證尚未送出的留言圖片
function handleCommentFilesChange(event) {
  const selectedFiles = Array.from(event.target.files || []);
  commentSubmitError.value = "";

  if (!selectedFiles.length) {
    return;
  }

  if (
    commentFiles.value.length + selectedFiles.length >
    MAX_COMMENT_IMAGE_COUNT
  ) {
    commentSubmitError.value = "每次留言最多上傳 5 張圖片";
    event.target.value = "";
    return;
  }

  const invalidTypeFile = selectedFiles.find(
    (file) => !file.type.startsWith("image/")
  );

  if (invalidTypeFile) {
    commentSubmitError.value = `只允許上傳圖片：${invalidTypeFile.name}`;
    event.target.value = "";
    return;
  }

  const oversizedFile = selectedFiles.find(
    (file) => file.size > MAX_COMMENT_IMAGE_SIZE
  );

  if (oversizedFile) {
    commentSubmitError.value = `圖片大小不可超過 10MB：${oversizedFile.name}`;
    event.target.value = "";
    return;
  }

  commentFiles.value = [...commentFiles.value, ...selectedFiles];

  event.target.value = "";
}

// 移除尚未送出的留言圖片
function removeCommentFile(fileIndex) {
  commentFiles.value = commentFiles.value.filter(
    (_, index) => index !== fileIndex
  );

  commentSubmitError.value = "";
}

// 送出留言
async function handleCommentSubmit() {
  const normalizedContent = commentContent.value.trim();

  if (!normalizedContent && !commentFiles.value.length) {
    commentSubmitError.value = "請輸入留言內容或至少加入一張圖片";
    return;
  }

  commentSubmitting.value = true;
  commentSubmitError.value = "";

  try {
    const formData = new FormData();

    if (normalizedContent) {
      formData.append("content", normalizedContent);
    }

    commentFiles.value.forEach((file) => {
      formData.append("files", file);
    });

    await createContactRecord(route.params.id, formData);

    commentContent.value = "";
    commentFiles.value = [];

    await loadContactRecords();
  } catch (error) {
    commentSubmitError.value =
      error.response?.data?.message || "留言送出失敗，請稍後再試";
  } finally {
    commentSubmitting.value = false;
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

.ticket-chip-dot {
  font-size: 0.55rem;
}

/* 留言輸入框底色 */
.comment-form {
  background-color: #f8f9fa;
}

.comment-form textarea::placeholder {
  color: #7f8489;
  opacity: 1;
  font-size: 0.9rem;
}

/* 留言聯繫 角色小方框 */
.contact-role-badge {
  padding: 0.1rem 0.4rem;
  font-size: 0.7rem;
  line-height: 1.2;
}

/* 單則聯繫紀錄卡片 */
.contact-record-card {
  background-color: #fbfcfd;
  border-color: #e2e6ea !important;
}

.contact-record-content {
  white-space: pre-wrap;
  overflow-wrap: anywhere;
}

.comment-file-info {
  min-width: 0;
}

/* 留言附件縮圖，點擊後可使用既有彈窗放大查看 */
.comment-attachment-thumbnail {
  width: 96px;
  height: 96px;
  object-fit: cover;
}

/* 返回頁面頂端懸浮按鈕 */
.ticket-detail-view .scroll-top-button.btn {
  position: fixed;
  right: 1.5rem;
  bottom: 1.5rem;
  z-index: 1030;
  display: grid;
  place-items: center;
  width: 44px;
  height: 44px;
  padding: 0;
  border-radius: 50% !important;
}
/* 返回頂端箭頭粗細 */
.scroll-top-icon {
  font-size: 1.1rem;
  -webkit-text-stroke: 0.6px currentColor;
}

/* 聯繫紀錄 總數小框 */
.contact-count-badge {
  padding: 0.25rem 0.45rem;
  font-size: 0.7rem;
  line-height: 1;
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

/* 手機版懸浮按鈕 */
@media (max-width: 575.98px) {
  .scroll-top-button {
    right: 1rem;
    bottom: 1rem;
    width: 40px;
    height: 40px;
  }
}
</style>
