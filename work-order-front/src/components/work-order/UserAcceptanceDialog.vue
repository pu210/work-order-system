<template>
  <WorkOrderDialogShell
    title="使用者驗收"
    :busy="submitting"
    title-id="user-acceptance-dialog-title"
    @close="$emit('close')"
  >
    <form id="user-acceptance-form" @submit.prevent="submitAccept">
      <div v-if="errorMessage" class="alert alert-danger py-2" role="alert">
        {{ errorMessage }}
      </div>
      <p class="text-muted">請確認設備或服務是否已恢復正常。</p>
      <label for="user-acceptance-feedback" class="form-label">驗收意見（選填）</label>
      <textarea
        id="user-acceptance-feedback"
        v-model.trim="feedback"
        class="form-control"
        rows="5"
        maxlength="1000"
        placeholder="可以填寫驗收結果或補充說明"
      ></textarea>
    </form>

    <template #footer>
      <button type="button" class="btn btn-outline-secondary" :disabled="submitting" @click="$emit('close')">
        取消
      </button>
      <button type="submit" form="user-acceptance-form" class="btn btn-primary" :disabled="submitting">
        {{ submitting ? '送出中…' : '確認驗收' }}
      </button>
    </template>
  </WorkOrderDialogShell>
</template>

<script setup>
import { ref } from "vue";
import { userCheckAccept } from "@/api/workOrder.js";
import WorkOrderDialogShell from "./WorkOrderDialogShell.vue";

const props = defineProps({
  workOrder: { type: Object, required: true },
});

const emit = defineEmits(["close", "completed"]);
const feedback = ref("");
const submitting = ref(false);
const errorMessage = ref("");

async function submitAccept() {
  submitting.value = true;
  errorMessage.value = "";
  try {
    await userCheckAccept(props.workOrder.workOrderId, { feedback: feedback.value || null });
    emit("completed", "使用者驗收完成，等待管理員驗收");
  } catch (error) {
    errorMessage.value = error.response?.data?.message || "驗收失敗，請稍後再試";
  } finally {
    submitting.value = false;
  }
}
</script>
