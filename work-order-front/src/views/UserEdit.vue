<template>
  <div class="user-create-page max-w-4xl mx-auto">
    <!-- 上方 Breadcrumb & 返回按鈕 -->
    <div class="d-flex align-items-center justify-content-between mb-4">
      <div>
        <button
          type="button"
          @click="goBack"
          class="btn btn-sm btn-outline-secondary rounded-2 extra-small mb-2 d-inline-flex align-items-center gap-1"
        >
          <i class="bi bi-arrow-left"></i> 返回帳號列表
        </button>
        <h4 class="fw-bold text-slate-800 m-0">編輯使用者</h4>
      </div>
    </div>

    <!-- 表單卡片 -->
    <div class="card border-0 rounded-4 shadow-sm bg-white overflow-hidden p-4">
      <form @submit.prevent="handleSubmit">
        <h6 class="fw-bold text-primary border-bottom pb-2 mb-3 extra-small">
          <i class="bi bi-person-vcard me-1"></i> 基本資料
        </h6>

        <div class="row g-3 mb-4">
          <!-- 帳號不能修改 -->
          <div class="col-md-6">
            <label
              class="form-label extra-small fw-semibold text-secondary mb-1"
            >
              帳號 <span class="text-danger">*</span>
            </label>
            <input
              type="text"
              v-model="form.account"
              class="form-control extra-small bg-light"
              disabled
            />
          </div>

          <!-- 使用者姓名 -->
          <div class="col-md-6">
            <label
              class="form-label extra-small fw-semibold text-secondary mb-1"
            >
              使用者姓名 <span class="text-danger">*</span>
            </label>
            <input
              type="text"
              v-model="form.name"
              class="form-control extra-small"
              placeholder="請輸入真實姓名"
              required
            />
          </div>

          <!-- 電子郵件 -->
          <div class="col-md-6">
            <label
              class="form-label extra-small fw-semibold text-secondary mb-1"
            >
              電子郵件信箱 <span class="text-danger">*</span>
            </label>
            <input
              type="email"
              v-model="form.email"
              class="form-control extra-small"
              placeholder="name@example.com"
              required
            />
          </div>

          <!-- 聯絡電話 -->
          <div class="col-md-6">
            <label
              class="form-label extra-small fw-semibold text-secondary mb-1"
              >聯絡電話</label
            >
            <input
              type="tel"
              v-model="form.phone"
              class="form-control extra-small"
              placeholder="例如：0912345678"
            />
          </div>
        </div>

        <div class="row g-3 mb-4">
          <h6 class="fw-bold text-primary border-bottom pb-2 mb-3 extra-small">
            <i class="bi bi-shield-lock me-1"></i> 安全性與權限設定
          </h6>

          <div class="row g-3 mb-4">
            <!-- 1. 帳號狀態 -->
            <div class="col-md-6">
              <label
                class="form-label extra-small fw-semibold text-secondary mb-1"
                >帳號狀態</label
              >
              <select v-model="form.status" class="form-select extra-small">
                <option :value="1">啟用 (正常使用)</option>
                <option :value="0">停用</option>
                <option :value="2">待審核</option>
              </select>
            </div>

            <!-- 2. 重設密碼：改為寄送信件按鈕 (🎯 核心修改) -->
            <div class="col-md-6">
              <label
                class="form-label extra-small fw-semibold text-secondary mb-1"
                >重設密碼</label
              >
              <div>
                <!-- 尚未寄信 / 冷卻結束時顯示 -->
                <button
                  v-if="cooldown === 0"
                  type="button"
                  class="btn btn-outline-primary extra-small w-100 d-flex align-items-center justify-content-center gap-2 py-2 rounded-3"
                  :disabled="isSendingMail"
                  @click="sendResetPasswordEmail"
                >
                  <span
                    v-if="isSendingMail"
                    class="spinner-border spinner-border-sm"
                    role="status"
                  ></span>
                  <i v-else class="bi bi-envelope-at-fill"></i>
                  <span>{{
                    isSendingMail ? "信件寄送中..." : "寄發密碼重設信件"
                  }}</span>
                </button>

                <!-- 寄信成功後的提示與倒數 -->
                <div
                  v-else
                  class="alert alert-success extra-small m-0 py-1.5 px-3 d-flex align-items-center justify-content-between rounded-3"
                >
                  <span class="d-flex align-items-center gap-1.5">
                    <i class="bi bi-check-circle-fill text-success"></i>
                    <span>已寄出重設信至 {{ form.email }}</span>
                  </span>
                  <span
                    class="badge bg-success-subtle text-success border border-success-subtle"
                  >
                    {{ cooldown }}s 後可重發
                  </span>
                </div>
              </div>
            </div>

            <!-- 3. 強制修改密碼 Checkbox -->
            <div class="col-12 mt-2">
              <div class="form-check">
                <input
                  type="checkbox"
                  v-model="form.must_change_password"
                  class="form-check-input"
                  id="mustChangePassword"
                />
                <label
                  class="form-check-label extra-small text-dark"
                  for="mustChangePassword"
                >
                  要求使用者下次登入時必須強制修改密碼
                </label>
              </div>
            </div>
          </div>
        </div>

        <!-- 按鈕動作區 -->
        <div class="d-flex justify-content-end gap-2 pt-3 border-top">
          <button
            type="button"
            @click="goBack"
            class="btn btn-light border extra-small px-4 py-2 rounded-3"
          >
            取消
          </button>
          <button
            type="submit"
            class="btn btn-primary extra-small px-4 py-2 rounded-3 shadow-2xs fw-semibold"
          >
            <i class="bi bi-check-lg me-1"></i> 儲存變更
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRoute, useRouter } from "vue-router";

const route = useRoute();
const router = useRouter();
const userId = route.params.id;

const goBack = () => {
  if (window.history.state?.back) {
    router.back();
    return;
  }

  router.push({ name: "user-management" });
};

// 依照 DB 設計的表單 reactive 資料
const form = ref({
  account: "",
  name: "",
  email: "",
  phone: "",
  password: "User1234!", // 預設密碼
  status: 1, // Default 啟用
  must_change_password: true, // Default true
});

// 產生隨機密碼小工具
const generateRandomPassword = () => {
  const chars =
    "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#";
  let pass = "";
  for (let i = 0; i < 10; i++) {
    pass += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  form.value.password = pass;
};

// 送出表單處理
const handleSubmit = () => {
  // 這裡之後串接 API 打到後端 POST /api/users
  console.log("送出的 User 資料：", form.value);
  alert("使用者編輯功！");
  router.push({ name: "user-management" });
};
</script>

<style scoped>
.extra-small {
  font-size: 0.82rem;
}

.max-w-4xl {
  max-width: 900px;
}

.text-slate-800 {
  color: #1e293b;
}

.shadow-2xs {
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
}
</style>
