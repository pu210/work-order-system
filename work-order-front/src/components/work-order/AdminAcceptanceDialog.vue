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
      <label for="admin-acceptance-feedback" class="form-label">
        驗收意見
        <span class="text-muted fw-normal">（通過時可不填，退回時必填）</span>
      </label>
      <textarea
        id="admin-acceptance-feedback"
        v-model.trim="feedback"
        class="form-control"
        rows="5"
        maxlength="1000"
        placeholder="請填寫驗收結果或退回原因"
      ></textarea>
    </form>

    <template #footer>
      <button type="button" class="btn btn-outline-secondary" :disabled="submitting" @click="$emit('close')">
        取消
      </button>
      <button type="button" class="btn btn-outline-danger" :disabled="submitting" @click="submitReject">
        {{ submittingAction === 'reject' ? '退回中…' : '退回工程師' }}
      </button>
      <button type="submit" form="admin-acceptance-form" class="btn btn-primary" :disabled="submitting">
        {{ submittingAction === 'accept' ? '送出中…' : '確認完成' }}
      </button>
    </template>
  </WorkOrderDialogShell>
</template>

<script setup>
import { ref } from "vue";
import { adminCheckAccept, adminCheckReject } from "@/api/workOrder.js";
import WorkOrderDialogShell from "./WorkOrderDialogShell.vue";

const props = defineProps({
  workOrder: { type: Object, required: true },
});

const emit = defineEmits(["close", "completed"]);
const feedback = ref("");
const submitting = ref(false);
const submittingAction = ref("");
const errorMessage = ref("");

function errorText(error, fallback) {
  return error.response?.data?.message || fallback;
}

async function submitAccept() {
  submitting.value = true;
  submittingAction.value = "accept";
  errorMessage.value = "";
  try {
    await adminCheckAccept(props.workOrder.workOrderId, { feedback: feedback.value || null });
    emit("completed", "管理員驗收完成，工單已結案");
  } catch (error) {
    errorMessage.value = errorText(error, "管理員驗收失敗");
  } finally {
    submitting.value = false;
    submittingAction.value = "";
  }
}

async function submitReject() {
  if (!feedback.value) {
    errorMessage.value = "退回工程師時必須填寫原因";
    return;
  }
  submitting.value = true;
  submittingAction.value = "reject";
  errorMessage.value = "";
  try {
    await adminCheckReject(props.workOrder.workOrderId, { feedback: feedback.value });
    emit("completed", "工單已退回工程師處理");
  } catch (error) {
    errorMessage.value = errorText(error, "退回工程師失敗");
  } finally {
    submitting.value = false;
    submittingAction.value = "";
  }
}
</script>
