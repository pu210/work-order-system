<template>
  <WorkOrderDialogShell
    :title="isReReview ? '重新審查工單' : '審查與派工'"
    :busy="submitting"
    title-id="admin-review-dialog-title"
    @close="$emit('close')"
  >
    <div v-if="loadingOptions" class="py-4 text-center text-muted">
      <span class="spinner-border spinner-border-sm me-2"></span>正在載入派工選項…
    </div>

    <form v-else id="admin-review-form" @submit.prevent="submitAccept">
      <div v-if="errorMessage" class="alert alert-danger py-2" role="alert">
        {{ errorMessage }}
      </div>

      <div class="mb-3">
        <label for="review-priority" class="form-label">優先級</label>
        <select id="review-priority" v-model="form.priorityId" class="form-select" required>
          <option value="" disabled>請選擇優先級</option>
          <option
            v-for="priority in priorities"
            :key="priority.prioritiesId"
            :value="priority.prioritiesId"
          >
            {{ priority.name }}
          </option>
        </select>
      </div>

      <div class="mb-3">
        <label for="review-handler" class="form-label">指派工程師</label>
        <select id="review-handler" v-model="form.assignedHandlerId" class="form-select" required>
          <option value="" disabled>請選擇工程師</option>
          <option v-for="handler in handlers" :key="handler.userId" :value="handler.userId">
            {{ handler.name }}
          </option>
        </select>
      </div>

      <div class="mb-3">
        <label for="review-due-time" class="form-label">預計完成時間</label>
        <input
          id="review-due-time"
          v-model="form.dueTime"
          type="datetime-local"
          class="form-control"
          required
        />
      </div>

      <div>
        <label for="review-feedback" class="form-label">
          審查意見
          <span class="text-muted fw-normal">（通過時可不填，退回時必填）</span>
        </label>
        <textarea
          id="review-feedback"
          v-model.trim="form.feedback"
          class="form-control"
          rows="4"
          maxlength="1000"
          placeholder="請輸入審查意見"
        ></textarea>
      </div>
    </form>

    <template #footer>
      <button type="button" class="btn btn-outline-secondary" :disabled="submitting" @click="$emit('close')">
        取消
      </button>
      <button
        type="button"
        class="btn btn-outline-danger"
        :disabled="loadingOptions || submitting"
        @click="submitReject"
      >
        {{ submittingAction === 'reject' ? '駁回中…' : '駁回工單' }}
      </button>
      <button
        type="submit"
        form="admin-review-form"
        class="btn btn-primary"
        :disabled="loadingOptions || submitting"
      >
        {{ submittingAction === 'accept' ? '送出中…' : '通過並派工' }}
      </button>
    </template>
  </WorkOrderDialogShell>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { getPriorities } from "@/api/priority.js";
import { getUsers } from "@/api/user.js";
import { reviewAccept, reviewReject } from "@/api/workOrder.js";
import WorkOrderDialogShell from "./WorkOrderDialogShell.vue";

const props = defineProps({
  workOrder: { type: Object, required: true },
  sessionToken: { type: String, default: null },
  isReReview: { type: Boolean, default: false },
});

const emit = defineEmits(["close", "completed"]);
const priorities = ref([]);
const handlers = ref([]);
const loadingOptions = ref(true);
const submitting = ref(false);
const submittingAction = ref("");
const errorMessage = ref("");
const form = reactive({
  priorityId: "",
  assignedHandlerId: props.workOrder.assignedHandlerId ?? "",
  dueTime: toDateTimeLocal(props.workOrder.dueTime),
  feedback: "",
});

function toDateTimeLocal(value) {
  if (!value) return "";
  return String(value).replace(" ", "T").slice(0, 16);
}

function normalizeList(response) {
  if (Array.isArray(response)) return response;
  if (Array.isArray(response?.data)) return response.data;
  return [];
}

function errorText(error, fallback) {
  return error.response?.data?.message || fallback;
}

async function loadOptions() {
  loadingOptions.value = true;
  errorMessage.value = "";
  try {
    const [priorityResponse, handlerResponse] = await Promise.all([
      getPriorities(),
      getUsers({ roleCode: "HANDLER", status: 1, size: 100 }),
    ]);
    priorities.value = normalizeList(priorityResponse);
    handlers.value = handlerResponse?.content ?? [];
    const currentPriority = priorities.value.find(
      (priority) => priority.name === props.workOrder.priorityName,
    );
    if (currentPriority) form.priorityId = currentPriority.prioritiesId;
  } catch (error) {
    errorMessage.value = errorText(error, "無法載入優先級或工程師選項");
  } finally {
    loadingOptions.value = false;
  }
}

function validateAccept() {
  if (!form.priorityId || !form.assignedHandlerId || !form.dueTime) {
    errorMessage.value = "請完整填寫優先級、指派工程師與預計完成時間";
    return false;
  }
  return true;
}

async function submitAccept() {
  if (!validateAccept()) return;
  submitting.value = true;
  submittingAction.value = "accept";
  errorMessage.value = "";
  try {
    await reviewAccept(
      props.workOrder.workOrderId,
      {
        priorityId: Number(form.priorityId),
        assignedHandlerId: Number(form.assignedHandlerId),
        dueTime: form.dueTime,
        feedback: form.feedback || null,
      },
      props.sessionToken,
    );
    emit("completed", props.isReReview ? "重新審查完成" : "工單已審查並完成派工");
  } catch (error) {
    errorMessage.value = errorText(error, "審查送出失敗，請稍後再試");
  } finally {
    submitting.value = false;
    submittingAction.value = "";
  }
}

async function submitReject() {
  if (!form.feedback) {
    errorMessage.value = "退回工單時必須填寫審查意見";
    return;
  }
  submitting.value = true;
  submittingAction.value = "reject";
  errorMessage.value = "";
  try {
    await reviewReject(
      props.workOrder.workOrderId,
      { feedback: form.feedback },
      props.sessionToken,
    );
    emit("completed", "工單已駁回並取消");
  } catch (error) {
    errorMessage.value = errorText(error, "退回工單失敗，請稍後再試");
  } finally {
    submitting.value = false;
    submittingAction.value = "";
  }
}

onMounted(loadOptions);
</script>
