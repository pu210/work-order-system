<template>
  <div class="register-view">
    <h4 class="fw-bold mb-2">建立新帳號</h4>
    <p class="text-muted small mb-4">填寫資料送出後，帳號需經管理員審核才能登入。</p>

    <form v-if="!successMessage" @submit.prevent="handleSubmit">
      <div class="row g-3">
        <div class="col-sm-6">
          <label for="account" class="form-label">帳號</label>
          <input
            id="account"
            v-model.trim="form.account"
            type="text"
            class="form-control"
            autocomplete="username"
            required
          />
        </div>

        <div class="col-sm-6">
          <label for="name" class="form-label">姓名</label>
          <input
            id="name"
            v-model.trim="form.name"
            type="text"
            class="form-control"
            autocomplete="name"
            required
          />
        </div>

        <div class="col-12">
          <label for="email" class="form-label">電子郵件</label>
          <input
            id="email"
            v-model.trim="form.email"
            type="email"
            class="form-control"
            placeholder="name@example.com"
            autocomplete="email"
            required
          />
        </div>

        <div class="col-12">
          <label for="phone" class="form-label">電話（選填）</label>
          <input
            id="phone"
            v-model.trim="form.phone"
            type="tel"
            class="form-control"
            autocomplete="tel"
          />
        </div>

        <div class="col-sm-6">
          <label for="password" class="form-label">密碼</label>
          <input
            id="password"
            v-model="form.password"
            type="password"
            class="form-control"
            minlength="8"
            autocomplete="new-password"
            required
          />
          <div class="form-text">至少 8 個字元</div>
        </div>

        <div class="col-sm-6">
          <label for="confirm-password" class="form-label">確認密碼</label>
          <input
            id="confirm-password"
            v-model="form.confirmPassword"
            type="password"
            class="form-control"
            minlength="8"
            autocomplete="new-password"
            required
          />
        </div>
      </div>

      <div v-if="errorMessage" class="alert alert-danger py-2 small mt-3 mb-0" role="alert">
        {{ errorMessage }}
      </div>

      <button type="submit" class="btn btn-primary w-100 mt-4" :disabled="isSubmitting">
        <span
          v-if="isSubmitting"
          class="spinner-border spinner-border-sm me-2"
          aria-hidden="true"
        ></span>
        {{ isSubmitting ? "送出中…" : "送出註冊申請" }}
      </button>

      <router-link to="/auth/login" class="btn btn-link w-100 mt-2 text-decoration-none">
        返回登入
      </router-link>
    </form>

    <div v-else>
      <div class="alert alert-success small" role="status">
        <i class="bi bi-check-circle-fill me-2" aria-hidden="true"></i>
        {{ successMessage }}
      </div>
      <router-link to="/auth/login" class="btn btn-primary w-100">返回登入</router-link>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import axios from "@/plugins/axios.js";

const form = reactive({
  account: "",
  name: "",
  email: "",
  phone: "",
  password: "",
  confirmPassword: "",
});
const errorMessage = ref("");
const successMessage = ref("");
const isSubmitting = ref(false);

async function handleSubmit() {
  errorMessage.value = "";

  if (form.password.length < 8) {
    errorMessage.value = "密碼至少需要 8 個字元";
    return;
  }

  if (form.password !== form.confirmPassword) {
    errorMessage.value = "兩次輸入的密碼不一致";
    return;
  }

  isSubmitting.value = true;
  try {
    const response = await axios.post("/auth/register", form, {
      skipAuthRedirect: true,
    });
    successMessage.value =
      response.data?.message || "註冊成功，請等待管理員審核";
  } catch (error) {
    errorMessage.value =
      error.response?.data?.message || "註冊失敗，請稍後再試";
  } finally {
    isSubmitting.value = false;
  }
}
</script>

<style scoped>
.form-label {
  margin-bottom: 0.35rem;
  color: #475569;
  font-size: 0.85rem;
  font-weight: 600;
}
</style>
