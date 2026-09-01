<template>
  <WorkOrderDialogShell
    title="管理員驗收"
    :busy="submitting"
    title-id="admin-acceptance-dialog-title"
    @close="$emit('close')"
  >
    <form id="admin-acceptance-form" @submit.prevent="submitAccept">
      <div v-if="errorMessage" class="alert alert-danger py-2" role="alert">
        {{ errorMessage }}
      </div>
      <section class="border rounded bg-body-tertiary p-3 mb-3" aria-labelledby="ai-archive-title">
        <div class="d-flex flex-wrap align-items-start justify-content-between gap-2">
          <div>
            <div id="ai-archive-title" class="fw-semibold">✨ AI 歸檔助手</div>
            <div class="small text-muted mt-1">
              AI 會整理工單內容與歷次反饋，只補入空白欄位；請確認並修改後再歸檔。
            </div>
          </div>
          <button
            type="button"
            class="btn btn-sm btn-outline-primary"
            :disabled="submitting || aiLoading"
            @click="generateSuggestion"
          >
            <span
              v-if="aiLoading"
              class="spinner-border spinner-border-sm me-1"
              aria-hidden="true"
            ></span>
            {{ aiLoading ? "AI 分析中…" : "AI 協助產生" }}
          </button>
        </div>

        <div v-if="aiNotice" class="alert alert-success py-2 mt-3 mb-0" role="status">
          {{ aiNotice }}
        </div>
        <div v-if="aiError" class="alert alert-warning py-2 mt-3 mb-0" role="alert">
          {{ aiError }}
        </div>
        <div v-if="aiInsufficientFields.length" class="alert alert-warning py-2 mt-3 mb-0">
          <div class="fw-semibold">以下資訊在歷史紀錄中不夠明確，請管理員確認：</div>
          <ul class="mb-0 mt-1 ps-4">
            <li v-for="field in aiInsufficientFields" :key="field">
              {{ insufficientMessage(field) }}
            </li>
          </ul>
        </div>
      </section>
      <div class="mb-3">
        <label for="admin-failure-cause" class="form-label">故障原因（必填）</label>
        <textarea
          id="admin-failure-cause"
          v-model.trim="archive.failureCause"
          class="form-control"
          rows="2"
          maxlength="100"
          placeholder="請填寫確認後的設備故障原因"
          required
        ></textarea>
        <div class="form-text text-end" :class="counterClass(archive.failureCause, 100)">
          {{ archive.failureCause.length }} / 100 字
        </div>
      </div>

      <div class="mb-3">
        <label for="admin-repair-action" class="form-label">處理方式（必填）</label>
        <textarea
          id="admin-repair-action"
          v-model.trim="archive.repairAction"
          class="form-control"
          rows="2"
          maxlength="150"
          placeholder="請填寫實際採取的維修或處理方式"
          required
        ></textarea>
        <div class="form-text text-end" :class="counterClass(archive.repairAction, 150)">
          {{ archive.repairAction.length }} / 150 字
        </div>
      </div>

      <div class="mb-3">
        <label for="admin-replaced-parts" class="form-label">
          更換零件
          <span class="text-muted fw-normal">（選填）</span>
        </label>
        <textarea
          id="admin-replaced-parts"
          v-model.trim="archive.replacedParts"
          class="form-control"
          rows="2"
          maxlength="80"
          placeholder="如有更換零件請填寫"
        ></textarea>
        <div class="form-text text-end" :class="counterClass(archive.replacedParts, 80)">
          {{ archive.replacedParts.length }} / 80 字
        </div>
      </div>

      <div class="mb-2">
        <label for="admin-test-result" class="form-label">測試結果（必填）</label>
        <textarea
          id="admin-test-result"
          v-model.trim="archive.testResult"
          class="form-control"
          rows="3"
          maxlength="100"
          placeholder="請填寫維修後的測試方式與確認結果"
          required
        ></textarea>
        <div class="form-text text-end" :class="counterClass(archive.testResult, 100)">
          {{ archive.testResult.length }} / 100 字
        </div>
      </div>

      <hr />

      <label for="admin-reject-feedback" class="form-label">
        退回原因
        <span class="text-muted fw-normal">（退回工程師時必填）</span>
      </label>
      <textarea
        id="admin-reject-feedback"
        v-model.trim="rejectFeedback"
        class="form-control"
        rows="3"
        maxlength="500"
        placeholder="只有退回工程師時需要填寫"
      ></textarea>
      <div class="form-text text-end" :class="counterClass(rejectFeedback, 500)">
        {{ rejectFeedback.length }} / 500 字
      </div>
    </form>

    <template #footer>
      <button type="button" class="btn btn-outline-secondary" :disabled="submitting" @click="$emit('close')">
        取消
      </button>
      <button type="button" class="btn btn-outline-danger" :disabled="submitting || aiLoading" @click="submitReject">
        {{ submittingAction === 'reject' ? '退回中…' : '退回工程師' }}
      </button>
      <button type="submit" form="admin-acceptance-form" class="btn btn-primary" :disabled="submitting || aiLoading">
        {{ submittingAction === 'accept' ? '送出中…' : '驗收通過並歸檔' }}
      </button>
    </template>
  </WorkOrderDialogShell>
</template>

<script setup>
import { reactive, ref } from "vue";
import {
  adminCheckAccept,
  adminCheckReject,
  generateAdminArchiveSuggestion,
} from "@/api/workOrder.js";
import WorkOrderDialogShell from "./WorkOrderDialogShell.vue";

const props = defineProps({
  workOrder: { type: Object, required: true },
});

const emit = defineEmits(["close", "completed"]);
const archive = reactive({
  failureCause: "",
  repairAction: "",
  replacedParts: "",
  testResult: "",
});
const rejectFeedback = ref("");
const submitting = ref(false);
const submittingAction = ref("");
const errorMessage = ref("");
const aiLoading = ref(false);
const aiError = ref("");
const aiNotice = ref("");
const aiInsufficientFields = ref([]);

const archiveFields = [
  "failureCause",
  "repairAction",
  "replacedParts",
  "testResult",
];

function counterClass(value, maxLength) {
  if (value.length >= maxLength) return "text-danger";
  if (value.length >= Math.ceil(maxLength * 0.9)) return "text-warning";
  return "text-muted";
}

function errorText(error, fallback) {
  return error.response?.data?.message || fallback;
}

function insufficientMessage(field) {
  const messages = {
    failureCause: "故障原因：請補上確認後的原因。",
    repairAction: "處理方式：請補上實際維修或處理內容。",
    replacedParts: "更換零件：若確認沒有更換，請填寫「無」。",
    testResult: "測試結果：請補上實際測試方式與結果。",
  };
  return messages[field] || field;
}

async function generateSuggestion() {
  if (aiLoading.value) return;

  aiLoading.value = true;
  aiError.value = "";
  aiNotice.value = "";
  aiInsufficientFields.value = [];
  try {
    const suggestion = await generateAdminArchiveSuggestion(
      props.workOrder.workOrderId,
    );
    let filledCount = 0;
    let preservedCount = 0;

    for (const field of archiveFields) {
      const value =
        typeof suggestion?.[field] === "string" ? suggestion[field].trim() : "";
      if (archive[field].trim()) {
        if (value) preservedCount += 1;
        continue;
      }
      if (value) {
        archive[field] = value;
        filledCount += 1;
      }
    }

    const missingFields = Array.isArray(suggestion?.insufficientFields)
      ? suggestion.insufficientFields
      : [];
    aiInsufficientFields.value = missingFields.filter(
      (field) => archiveFields.includes(field) && !archive[field].trim(),
    );

    aiNotice.value = `AI 已補上 ${filledCount} 個欄位${
      preservedCount ? `；你原本填寫的 ${preservedCount} 個欄位已保留` : ""
    }。`;
  } catch (error) {
    aiError.value = errorText(
      error,
      "AI 暫時無法產生建議，你仍可手動填寫或稍後重試。",
    );
  } finally {
    aiLoading.value = false;
  }
}

async function submitAccept() {
  if (!archive.failureCause || !archive.repairAction || !archive.testResult) {
    errorMessage.value = "請完整填寫故障原因、處理方式與測試結果";
    return;
  }
  submitting.value = true;
  submittingAction.value = "accept";
  errorMessage.value = "";
  try {
    await adminCheckAccept(props.workOrder.workOrderId, {
      failureCause: archive.failureCause,
      repairAction: archive.repairAction,
      replacedParts: archive.replacedParts || null,
      testResult: archive.testResult,
    });
    emit("completed", "管理員驗收完成，工單已結案");
  } catch (error) {
    errorMessage.value = errorText(error, "管理員驗收失敗");
  } finally {
    submitting.value = false;
    submittingAction.value = "";
  }
}

async function submitReject() {
  if (!rejectFeedback.value) {
    errorMessage.value = "退回工程師時必須填寫原因";
    return;
  }
  submitting.value = true;
  submittingAction.value = "reject";
  errorMessage.value = "";
  try {
    await adminCheckReject(props.workOrder.workOrderId, { feedback: rejectFeedback.value });
    emit("completed", "工單已退回工程師處理");
  } catch (error) {
    errorMessage.value = errorText(error, "退回工程師失敗");
  } finally {
    submitting.value = false;
    submittingAction.value = "";
  }
}
</script>
