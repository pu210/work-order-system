<template>
  <div class="login-view">
    <form @submit.prevent="handleLogin">
      <!-- 帳號區塊 -->
      <div class="mb-3">
        <label class="form-label text-secondary extra-small fw-semibold mb-1"
          >帳號 / 電子郵件</label
        >
        <div class="input-group custom-input-group">
          <span class="input-group-text bg-light border-end-0 text-muted ps-3">
            <i class="bi bi-person fs-6"></i>
          </span>
          <input
            v-model="account"
            type="text"
            class="form-control border-start-0 ps-2"
            placeholder="請輸入帳號"
            autocomplete="username"
            required
          />
        </div>
      </div>

      <!-- 密碼區塊 -->
      <div class="mb-2">
        <label class="form-label text-secondary extra-small fw-semibold mb-1"
          >密碼</label
        >
        <div class="input-group custom-input-group">
          <span class="input-group-text bg-light border-end-0 text-muted ps-3">
            <i class="bi bi-lock fs-6"></i>
          </span>
          <input
            v-model="password"
            :type="showPassword ? 'text' : 'password'"
            class="form-control border-start-0 border-end-0 ps-2"
            placeholder="請輸入密碼"
            autocomplete="current-password"
            required
          />
          <button
            type="button"
            class="input-group-text bg-light border-start-0 text-muted pe-3 btn-eye"
            @click="showPassword = !showPassword"
          >
            <i
              :class="['bi', showPassword ? 'bi-eye-slash' : 'bi-eye', 'fs-6']"
            ></i>
          </button>
        </div>
      </div>

      <!-- 忘記密碼 -->
      <div class="text-end mb-4">
        <router-link
          to="/auth/forgot/password"
          class="text-decoration-none extra-small text-primary fw-medium"
        >
          忘記密碼？
        </router-link>
      </div>

      <div
        v-if="errorMessage"
        class="alert alert-danger py-2 small"
        role="alert"
      >
        {{ errorMessage }}
      </div>

      <!-- 登入按鈕 (改為系統主色藍) -->
      <button
        type="submit"
        class="btn btn-primary w-100 py-2.5 fw-semibold mb-3 rounded-3 shadow-2xs"
        :disabled="isSubmitting"
      >
        <span
          v-if="isSubmitting"
          class="spinner-border spinner-border-sm me-2"
          aria-hidden="true"
        ></span>
        {{ isSubmitting ? "登入中…" : "登入系統" }}
      </button>

      <!-- 「或」分隔線 -->
      <div class="d-flex align-items-center my-3">
        <hr class="flex-grow-1 m-0 text-secondary opacity-25" />
        <span class="px-3 text-muted extra-small">或</span>
        <hr class="flex-grow-1 m-0 text-secondary opacity-25" />
      </div>

      <!-- 註冊與 Google 登入 (雙欄/次要按鈕風格) -->
      <div class="d-flex flex-column gap-2">
        <a
          :href="googleLoginUrl"
          class="btn btn-white w-100 py-2 fw-medium d-flex align-items-center justify-content-center gap-2 text-decoration-none rounded-3 border extra-small"
        >
          <i class="bi bi-google text-danger"></i>
          <span>使用 Google 帳號登入</span>
        </a>

        <router-link
          to="/auth/register"
          class="btn btn-light w-100 py-2 fw-medium rounded-3 border extra-small text-secondary"
        >
          建立新帳號
        </router-link>
      </div>
    </form>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import axios from "@/plugins/axios.js";
import { useAuthStore } from "@/stores/auth.js";

const showPassword = ref(false);
const account = ref("");
const password = ref("");
const errorMessage = ref("");
const isSubmitting = ref(false);
const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const apiBaseUrl = import.meta.env.VITE_API_URL || "http://localhost:8080";

const googleLoginUrl = `${apiBaseUrl.replace(/\/$/, "")}/oauth2/authorization/google`;

onMounted(async () => {
  const oauthResult = route.query.oauth;

  if (oauthResult === "failed") {
    errorMessage.value =
      "Google 登入失敗，請確認 Google Email 已建立系統帳號且帳號已啟用";
    return;
  }

  if (oauthResult !== "success") {
    return;
  }

  isSubmitting.value = true;
  errorMessage.value = "";

  try {
    const response = await axios.get("/auth/oauth2/session", {
      withCredentials: true,
      skipAuthRedirect: true,
    });

    const data = response.data?.data;

    if (!data?.token) {
      throw new Error("OAuth 登入回應缺少 Token");
    }

    authStore.login(data);

    await router.replace("/dashboard");
  } catch (error) {
    errorMessage.value =
      error.response?.data?.message || "無法取得 Google 登入結果，請重新登入";
  } finally {
    isSubmitting.value = false;
  }
});

async function handleLogin() {
  errorMessage.value = "";

  if (!account.value || !password.value) {
    errorMessage.value = "請輸入帳號與密碼";
    return;
  }

  isSubmitting.value = true;
  try {
    const response = await axios.post(
      "/auth/login",
      { account: account.value, password: password.value },
      {
        skipAuthRedirect: true,
        withCredentials: true,
      },
    );
    const data = response.data?.data;

    if (!data?.token) {
      throw new Error("登入回應缺少 Token");
    }

    authStore.login(data);
    if (data.mustChangePassword) {
      await router.replace({ name: "initial-password" });
      return;
    }
    const returnUrl =
      typeof route.query.returnUrl === "string" &&
      route.query.returnUrl.startsWith("/")
        ? route.query.returnUrl
        : "/dashboard";
    await router.replace(returnUrl);
  } catch (error) {
    errorMessage.value =
      error.response?.data?.message || "無法登入，請確認後端服務與帳號密碼";
  } finally {
    isSubmitting.value = false;
  }
}
</script>

<style scoped>
.extra-small {
  font-size: 0.8rem;
}

.shadow-2xs {
  box-shadow: 0 1px 2px 0 rgba(13, 110, 253, 0.25);
}

/* 輸入框質感調校 */
.custom-input-group .form-control,
.custom-input-group .input-group-text {
  border-color: #dcdfe6;
  padding-top: 0.65rem;
  padding-bottom: 0.65rem;
}

.custom-input-group .form-control:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.btn-eye {
  cursor: pointer;
}
.btn-eye:hover {
  color: #1e293b !important;
}

/* 白色按鈕微調 */
.btn-white {
  background-color: #ffffff;
  border-color: #e2e8f0;
  color: #334155;
  transition: all 0.2s ease;
}

.btn-white:hover {
  background-color: #f8fafc;
  border-color: #cbd5e1;
  color: #0f172a;
}
</style>
