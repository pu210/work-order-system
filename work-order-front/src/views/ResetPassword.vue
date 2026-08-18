<template>
  <div class="reset-password-view">
    <h4 class="fw-bold mb-2">重設密碼</h4>

    <p class="text-muted small mb-4">請輸入並確認您的新密碼。</p>

    <div v-if="!token" class="alert alert-danger" role="alert">
      重設連結無效或缺少 Token，請重新申請密碼重設。
    </div>

    <form v-else @submit.prevent="handleSubmit">
      <div class="mb-3">
        <label for="password" class="form-label">新密碼</label>

        <input
          id="password"
          v-model="password"
          type="password"
          class="form-control"
          placeholder="請輸入新密碼"
          autocomplete="new-password"
          required
        />
      </div>

      <div class="mb-3">
        <label for="confirmPassword" class="form-label"> 確認新密碼 </label>

        <input
          id="confirmPassword"
          v-model="confirmPassword"
          type="password"
          class="form-control"
          placeholder="請再次輸入新密碼"
          autocomplete="new-password"
          required
        />
      </div>

      <div
        v-if="successMessage"
        class="alert alert-success py-2 small"
        role="alert"
      >
        {{ successMessage }}
      </div>

      <div
        v-if="errorMessage"
        class="alert alert-danger py-2 small"
        role="alert"
      >
        {{ errorMessage }}
      </div>

      <button
        type="submit"
        class="btn btn-primary w-100"
        :disabled="isSubmitting || Boolean(successMessage)"
      >
        <span
          v-if="isSubmitting"
          class="spinner-border spinner-border-sm me-2"
        ></span>

        {{ isSubmitting ? "重設中…" : "確認重設密碼" }}
      </button>
    </form>

    <router-link
      to="/auth/login"
      class="btn btn-link w-100 mt-2 text-decoration-none"
    >
      返回登入
    </router-link>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import axios from "@/plugins/axios.js";

const route = useRoute();
const router = useRouter();

const token = typeof route.query.token === "string" ? route.query.token : "";

const password = ref("");
const confirmPassword = ref("");
const successMessage = ref("");
const errorMessage = ref("");
const isSubmitting = ref(false);

async function handleSubmit() {
  successMessage.value = "";
  errorMessage.value = "";

  if (!password.value || !confirmPassword.value) {
    errorMessage.value = "請輸入並確認新密碼";
    return;
  }

  if (password.value !== confirmPassword.value) {
    errorMessage.value = "兩次輸入的密碼不一致";
    return;
  }

  isSubmitting.value = true;

  try {
    const response = await axios.post(
      "/auth/reset-password",
      {
        token,
        password: password.value,
        confirmPassword: confirmPassword.value,
      },
      {
        skipAuthRedirect: true,
      },
    );

    successMessage.value = response.data?.message || "密碼重設成功";

    window.setTimeout(() => {
      router.replace("/auth/login");
    }, 1500);
  } catch (error) {
    errorMessage.value =
      error.response?.data?.message || "密碼重設失敗，連結可能無效或已過期";
  } finally {
    isSubmitting.value = false;
  }
}
</script>
