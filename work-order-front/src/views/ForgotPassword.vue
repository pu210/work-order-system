<template>
  <div class="forgot-password-view">
    <h4 class="fw-bold mb-2">忘記密碼</h4>

    <p class="text-muted small mb-4">
      請輸入註冊時使用的 Email，我們會寄送密碼重設連結。
    </p>

    <form @submit.prevent="handleSubmit">
      <div class="mb-3">
        <label for="email" class="form-label">電子郵件</label>

        <input
          id="email"
          v-model.trim="email"
          type="email"
          class="form-control"
          placeholder="name@example.com"
          autocomplete="email"
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
        :disabled="isSubmitting"
      >
        <span
          v-if="isSubmitting"
          class="spinner-border spinner-border-sm me-2"
        ></span>

        {{ isSubmitting ? "寄送中…" : "寄送重設信件" }}
      </button>

      <router-link
        to="/auth/login"
        class="btn btn-link w-100 mt-2 text-decoration-none"
      >
        返回登入
      </router-link>
    </form>
  </div>
</template>

<script setup>
import { ref } from "vue";
import axios from "@/plugins/axios.js";

const email = ref("");
const successMessage = ref("");
const errorMessage = ref("");
const isSubmitting = ref(false);

async function handleSubmit() {
  successMessage.value = "";
  errorMessage.value = "";

  if (!email.value) {
    errorMessage.value = "請輸入 Email";
    return;
  }

  isSubmitting.value = true;

  try {
    const response = await axios.post(
      "/api/auth/forgot-password",
      {
        email: email.value,
      },
      {
        skipAuthRedirect: true,
      },
    );

    successMessage.value =
      response.data?.message || "重設密碼信件已寄出，請檢查信箱";
  } catch (error) {
    errorMessage.value =
      error.response?.data?.message || "寄送失敗，請稍後再試";
  } finally {
    isSubmitting.value = false;
  }
}
</script>
