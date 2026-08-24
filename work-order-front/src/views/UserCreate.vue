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
        <h4 class="fw-bold text-slate-800 m-0">新增使用者</h4>
      </div>
    </div>

    <!-- 表單卡片 -->
    <div class="card border-0 rounded-4 shadow-sm bg-white overflow-hidden p-4">
      <form @submit.prevent="handleSubmit">
        <div class="row g-3 mb-4">
          <!-- 帳號 -->
          <div class="col-md-6">
            <label
              class="form-label extra-small fw-semibold text-secondary mb-1"
            >
              帳號 <span class="text-danger">*</span>
            </label>
            <input
              v-model.trim="form.account"
              type="text"
              class="form-control extra-small"
              placeholder="請輸入英文或數字帳號"
              pattern="[A-Za-z0-9]+"
              maxlength="50"
              title="帳號只能輸入英文字母與數字"
              autocomplete="username"
              required
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
          <div class="col-md-6">
            <label
              class="form-label extra-small fw-semibold text-secondary mb-1"
            >
              使用者角色 <span class="text-danger">*</span>
            </label>

            <div class="role-options border rounded-2 px-3 py-2">
              <label
                v-for="role in roleOptions"
                :key="role.value"
                class="form-check mb-2 last-option"
              >
                <input
                  v-model="form.roleCodes"
                  class="form-check-input"
                  type="checkbox"
                  :value="role.value"
                />
                <span class="form-check-label extra-small">
                  {{ role.label }}
                </span>
              </label>
            </div>
            <div class="form-text extra-small">可選擇一個或多個角色</div>
          </div>
          <!-- 預設密碼 -->
          <div class="col-md-6">
            <label
              class="form-label extra-small fw-semibold text-secondary mb-1"
            >
              預設密碼 <span class="text-danger">*</span>
            </label>
            <div class="input-group">
              <input
                type="text"
                v-model="form.password"
                class="form-control extra-small"
                placeholder="請設定預設密碼"
                required
              />
              <button
                type="button"
                class="btn btn-outline-secondary extra-small"
                @click="generateRandomPassword"
              >
                隨機產生
              </button>
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
            <i class="bi bi-check-lg me-1"></i> 建立
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { createUser } from "@/api/user.js";

const router = useRouter();

const roleOptions = [
  { value: "EMPLOYEE", label: "一般員工" },
  { value: "HANDLER", label: "維修人員" },
  { value: "ADMIN", label: "管理員" },
];

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
  roleCodes: [],
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
const handleSubmit = async () => {
  const accountPattern = /^[A-Za-z0-9]+$/;

  if (!accountPattern.test(form.value.account)) {
    alert("帳號只能輸入英文字母與數字");
    return;
  }

  if (form.value.roleCodes.length === 0) {
    alert("請至少選擇一個使用者角色");
    return;
  }

  try {
    const payload = {
      account: form.value.account.trim(),
      name: form.value.name.trim(),
      email: form.value.email.trim(),
      phone: form.value.phone.trim() || null,
      password: form.value.password,
      roleCodes: form.value.roleCodes,
    };

    await createUser(payload);

    alert("使用者新增成功！");
    router.push({ name: "user-management" });
  } catch (error) {
    console.error("新增失敗：", error);

    alert(
      error.response?.data?.message || "建立失敗，請確認欄位是否填寫正確！",
    );
  }
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

.role-options {
  background-color: #fff;
}

.role-options .last-option:last-child {
  margin-bottom: 0 !important;
}
</style>
