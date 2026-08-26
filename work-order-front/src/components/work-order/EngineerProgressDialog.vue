<template>
  <WorkOrderDialogShell
    title="工程師處理回報"
    :busy="submitting"
    title-id="engineer-progress-dialog-title"
    @close="$emit('close')"
  >
    <form id="engineer-progress-form" @submit.prevent="submitComplete">
      <div v-if="errorMessage" class="alert alert-danger py-2" role="alert">
        {{ errorMessage }}
      </div>

      <div class="mb-3">
        <label for="progress-target-no" class="form-label">設備編號</label>
        <input
          id="progress-target-no"
          v-model.trim="form.targetNo"
          type="text"
          class="form-control"
          maxlength="100"
          placeholder="請輸入設備編號"
          required
        />
      </div>

      <div>
        <label for="progress-feedback" class="form-label">
          處理反饋
          <span class="text-muted fw-normal">（回退管理員時必填）</span>
        </label>
        <textarea
          id="progress-feedback"
          v-model.trim="form.feedback"
          class="form-control"
          rows="5"
          maxlength="1000"
          placeholder="請說明處理結果或退回原因"
        ></textarea>
      </div>
    </form>

    <template #footer>
      <button type="button" class="btn btn-outline-secondary" :disabled="submitting" @click="$emit('close')">
        取消
      </button>
      <button type="button" class="btn btn-outline-danger" :disabled="submitting" @click="submitReturn">
        {{ submittingAction === 'return' ? '退回中…' : '退回管理員' }}
      </button>
      <button type="submit" form="engineer-progress-form" class="btn btn-primary" :disabled="submitting">
        {{ submittingAction === 'complete' ? '送出中…' : '回報完成' }}
      </button>
    </template>
  </WorkOrderDialogShell>
</template>

<script setup>
import { reactive, ref } from "vue";
import { progressAccept, progressReject } from "@/api/workOrder.js";
import WorkOrderDialogShell from "./WorkOrderDialogShell.vue";

const props = defineProps({
  workOrder: { type: Object, required: true },
});

const emit = defineEmits(["close", "completed"]);
const form = reactive({ targetNo: "", feedback: "" });
const submitting = ref(false);
const submittingAction = ref("");
const errorMessage = ref("");

function errorText(error, fallback) {
  return error.response?.data?.message || fallback;
}

async function submitComplete() {
  if (!form.targetNo) {
    errorMessage.value = "請填寫設備編號";
    return;
  }
  submitting.value = true;
  submittingAction.value = "complete";
  errorMessage.value = "";
  try {
    await progressAccept(props.workOrder.workOrderId, {
      targetNo: form.targetNo,
      feedback: form.feedback || null,
    });
    emit("completed", "工程處理結果已送出，等待使用者驗收");
  } catch (error) {
    errorMessage.value = errorText(error, "工程處理結果送出失敗");
  } finally {
    submitting.value = false;
    submittingAction.value = "";
  }
}

async function submitReturn() {
  if (!form.feedback) {
    errorMessage.value = "退回管理員時必須填寫原因";
    return;
  }
  submitting.value = true;
  submittingAction.value = "return";
  errorMessage.value = "";
  try {
    await progressReject(props.workOrder.workOrderId, { feedback: form.feedback });
    emit("completed", "工單已退回管理員重新審查");
  } catch (error) {
    errorMessage.value = errorText(error, "退回管理員失敗");
  } finally {
    submitting.value = false;
    submittingAction.value = "";
  }
}
</script>
