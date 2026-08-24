<template>
  <div class="initial-password-view">
    <div class="card border-0 shadow-sm mx-auto">
      <div class="card-body p-4">
        <h4 class="fw-bold mb-2">修改初始密碼</h4>

        <p class="text-muted small mb-4">
          為了帳號安全，首次登入必須先修改密碼才能使用系統。
        </p>

        <form @submit.prevent="handleSubmit">
          <div class="mb-3">
            <label for="currentPassword" class="form-label">目前密碼</label>
            <input
              id="currentPassword"
              v-model="form.currentPassword"
              type="password"
              class="form-control"
              autocomplete="current-password"
              placeholder="請輸入目前使用的初始密碼"
              required
            />
          </div>

          <div class="mb-3">
            <label for="newPassword" class="form-label">新密碼</label>
            <input
              id="newPassword"
              v-model="form.newPassword"
              type="password"
              class="form-control"
              autocomplete="new-password"
              placeholder="請輸入新密碼"
              minlength="8"
              required
            />
          </div>

          <div class="mb-3">
            <label for="confirmPassword" class="form-label">確認新密碼</label>
            <input
              id="confirmPassword"
              v-model="form.confirmPassword"
              type="password"
              class="form-control"
              autocomplete="new-password"
              placeholder="請再次輸入新密碼"
              minlength="8"
              required
            />
          </div>

          <div
            v-if="errorMessage"
            class="alert alert-danger py-2 small"
            role="alert"
          >
            {{ errorMessage }}
          </div>

          <div
            v-if="successMessage"
            class="alert alert-success py-2 small"
            role="alert"
          >
            {{ successMessage }}
          </div>

          <button
            type="submit"
            class="btn btn-primary w-100"
            :disabled="isSubmitting"
          >
            <span
              v-if="isSubmitting"
              class="spinner-border spinner-border-sm me-2"
              aria-hidden="true"
            ></span>

            {{ isSubmitting ? "修改中…" : "確認修改密碼" }}
          </button>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import axios from "@/plugins/axios.js";
import { getCurrentUser } from "@/utils/auth.js";
import { markPasswordChanged } from "@/utils/auth.js";

const router = useRouter();

const form = reactive({
  currentPassword: "",
  newPassword: "",
  confirmPassword: "",
});

const errorMessage = ref("");
const successMessage = ref("");
const isSubmitting = ref(false);

async function handleSubmit() {
  errorMessage.value = "";
  successMessage.value = "";

  if (!form.currentPassword || !form.newPassword || !form.confirmPassword) {
    errorMessage.value = "請完整輸入所有密碼欄位";
    return;
  }

  if (form.newPassword.length < 8) {
    errorMessage.value = "新密碼至少需要 8 個字元";
    return;
  }

  if (form.newPassword !== form.confirmPassword) {
    errorMessage.value = "兩次輸入的新密碼不一致";
    return;
  }

  if (form.currentPassword === form.newPassword) {
    errorMessage.value = "新密碼不能與目前密碼相同";
    return;
  }

  isSubmitting.value = true;

  try {
    const response = await axios.patch("/api/account/initial-password", {
      currentPassword: form.currentPassword,
      newPassword: form.newPassword,
      confirmPassword: form.confirmPassword,
    });

    // 後端修改成功後，同步更新 localStorage 中的使用者狀態。
    markPasswordChanged();
    successMessage.value = response.data?.message || "密碼修改成功";

    window.setTimeout(() => {
      router.replace("/dashboard");
    }, 1000);
  } catch (error) {
    errorMessage.value =
      error.response?.data?.message || "密碼修改失敗，請確認目前密碼";
  } finally {
    isSubmitting.value = false;
  }
}
</script>

<style scoped>
.initial-password-view {
  padding-top: 2rem;
}

.card {
  max-width: 520px;
}
</style>
