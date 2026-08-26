<template>
  <div v-if="summaryError && needsAdminSummary" class="border-top pt-3">
    <div class="alert alert-danger py-2 mb-0 small" role="alert">{{ summaryError }}</div>
  </div>

  <div v-if="actionType" class="border-top pt-3">
    <button
      type="button"
      class="btn btn-primary w-100 fw-semibold"
      :disabled="acquiringLock"
      @click="openAction"
    >
      <span v-if="acquiringLock" class="spinner-border spinner-border-sm me-2"></span>
      {{ acquiringLock ? "正在取得編輯權…" : actionLabel }}
    </button>
  </div>

  <AdminReviewDialog
    v-if="activeDialog === 'review'"
    :work-order="effectiveOrder"
    :session-token="sessionToken"
    :is-re-review="effectiveOrder.adminUserId != null"
    @close="closeDialog"
    @completed="handleCompleted"
  />
  <EngineerProgressDialog
    v-if="activeDialog === 'progress'"
    :work-order="effectiveOrder"
    @close="closeDialog"
    @completed="handleCompleted"
  />
  <UserAcceptanceDialog
    v-if="activeDialog === 'user-acceptance'"
    :work-order="effectiveOrder"
    @close="closeDialog"
    @completed="handleCompleted"
  />
  <AdminAcceptanceDialog
    v-if="activeDialog === 'admin-acceptance'"
    :work-order="effectiveOrder"
    @close="closeDialog"
    @completed="handleCompleted"
  />
</template>

<script setup>
import { computed, onBeforeUnmount, ref, watch } from "vue";
import { useAuthStore } from "@/stores/auth.js";
import { notify } from "@/plugins/notify.js";
import {
  editSessionHeartbeat,
  getWorkOrderById,
  releaseEditSession,
  startEditSession,
} from "@/api/workOrder.js";
import AdminReviewDialog from "./AdminReviewDialog.vue";
import EngineerProgressDialog from "./EngineerProgressDialog.vue";
import UserAcceptanceDialog from "./UserAcceptanceDialog.vue";
import AdminAcceptanceDialog from "./AdminAcceptanceDialog.vue";

const props = defineProps({
  workOrder: { type: Object, required: true },
});

const emit = defineEmits(["updated"]);
const authStore = useAuthStore();
const summary = ref(null);
const summaryLoaded = ref(false);
const summaryError = ref("");
const activeDialog = ref("");
const acquiringLock = ref(false);
const sessionToken = ref(null);
const sessionWorkOrderId = ref(null);
let heartbeatTimer = null;

const effectiveOrder = computed(() => {
  const order = { ...props.workOrder };
  if (summaryLoaded.value && summary.value) {
    order.adminUserId = summary.value.adminUserId ?? null;
    order.adminName = summary.value.adminName ?? null;
  }
  return order;
});

const needsAdminSummary = computed(
  () =>
    authStore.hasRole("ADMIN") &&
    ["PENDING_REVIEW", "PENDING_ADMIN_ACCEPTANCE"].includes(props.workOrder.status),
);

function sameUser(left, right) {
  return left != null && right != null && Number(left) === Number(right);
}

const actionType = computed(() => {
  const order = effectiveOrder.value;
  const currentUserId = authStore.userId;

  if (order.status === "PENDING_REVIEW" && authStore.hasRole("ADMIN")) {
    if (!summaryLoaded.value || !summary.value) return "";
    if (order.adminUserId == null || sameUser(order.adminUserId, currentUserId)) return "review";
  }
  if (
    order.status === "IN_PROGRESS" &&
    authStore.hasRole("HANDLER") &&
    sameUser(order.assignedHandlerId, currentUserId)
  ) {
    return "progress";
  }
  if (order.status === "PENDING_USER_ACCEPTANCE" && sameUser(order.creatorUserId, currentUserId)) {
    return "user-acceptance";
  }
  if (
    order.status === "PENDING_ADMIN_ACCEPTANCE" &&
    authStore.hasRole("ADMIN") &&
    summaryLoaded.value &&
    summary.value &&
    sameUser(order.adminUserId, currentUserId)
  ) {
    return "admin-acceptance";
  }
  return "";
});

const actionLabel = computed(() => {
  if (actionType.value === "review") {
    return effectiveOrder.value.adminUserId == null ? "審查與派工" : "重新審查";
  }
  if (actionType.value === "progress") return "編輯處理結果";
  if (actionType.value === "user-acceptance") return "使用者驗收";
  if (actionType.value === "admin-acceptance") return "管理員驗收";
  return "處理工單";
});

function errorText(error, fallback) {
  return error.response?.data?.message || fallback;
}

async function loadSummary() {
  summaryLoaded.value = false;
  summaryError.value = "";
  summary.value = null;
  try {
    summary.value = await getWorkOrderById(props.workOrder.workOrderId);
    summaryLoaded.value = true;
  } catch (error) {
    summaryError.value = errorText(error, "無法確認管理員操作權限，請重新整理頁面");
  }
}

function stopHeartbeat() {
  if (heartbeatTimer) window.clearInterval(heartbeatTimer);
  heartbeatTimer = null;
}

function startHeartbeat() {
  stopHeartbeat();
  heartbeatTimer = window.setInterval(async () => {
    if (!sessionToken.value) return;
    try {
      await editSessionHeartbeat(sessionWorkOrderId.value, sessionToken.value);
    } catch {
      stopHeartbeat();
      sessionToken.value = null;
      sessionWorkOrderId.value = null;
      activeDialog.value = "";
      await notify.alert({
        title: "編輯權已失效",
        text: "無法續期工單編輯權，請重新點擊審查按鈕。",
        icon: "warning",
      });
    }
  }, 90_000);
}

async function acquireReviewLock() {
  acquiringLock.value = true;
  try {
    const session = await startEditSession(props.workOrder.workOrderId);
    sessionToken.value = session.sessionToken;
    sessionWorkOrderId.value = props.workOrder.workOrderId;
    startHeartbeat();
    return true;
  } catch (error) {
    const message = errorText(error, "目前無法取得工單編輯權");
    if (error.response?.status === 423 || message.includes("編輯中")) {
      await notify.alert({ title: "此工單正在編輯", text: message, icon: "warning" });
    } else {
      notify.error(message);
    }
    return false;
  } finally {
    acquiringLock.value = false;
  }
}

async function openAction() {
  const type = actionType.value;
  if (!type) return;
  if (type === "review" && effectiveOrder.value.adminUserId == null) {
    const acquired = await acquireReviewLock();
    if (!acquired) return;
  }
  activeDialog.value = type;
}

async function releaseReviewLock() {
  const token = sessionToken.value;
  const workOrderId = sessionWorkOrderId.value;
  stopHeartbeat();
  sessionToken.value = null;
  sessionWorkOrderId.value = null;
  if (!token || !workOrderId) return;
  try {
    await releaseEditSession(workOrderId, token);
  } catch (error) {
    notify.warning(errorText(error, "編輯權釋放失敗，系統將在逾時後自動釋放"));
  }
}

async function closeDialog() {
  activeDialog.value = "";
  await releaseReviewLock();
}

async function handleCompleted(message) {
  activeDialog.value = "";
  stopHeartbeat();
  sessionToken.value = null;
  sessionWorkOrderId.value = null;
  notify.success(message);
  emit("updated");
}

watch(
  () => [props.workOrder.workOrderId, props.workOrder.status, needsAdminSummary.value],
  async () => {
    activeDialog.value = "";
    await releaseReviewLock();
    if (needsAdminSummary.value) {
      await loadSummary();
    } else {
      summary.value = null;
      summaryLoaded.value = false;
      summaryError.value = "";
    }
  },
  { immediate: true },
);

onBeforeUnmount(() => {
  stopHeartbeat();
  if (sessionToken.value && sessionWorkOrderId.value) {
    void releaseEditSession(sessionWorkOrderId.value, sessionToken.value).catch(() => {});
  }
});
</script>
