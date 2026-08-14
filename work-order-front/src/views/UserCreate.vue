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
        <h6 class="fw-bold text-primary border-bottom pb-2 mb-3 extra-small">
          <i class="bi bi-person-vcard me-1"></i> 基本資料
        </h6>

        <div class="row g-3 mb-4">
          <!-- 帳號 -->
          <div class="col-md-6">
            <label
              class="form-label extra-small fw-semibold text-secondary mb-1"
            >
              帳號 <span class="text-danger">*</span>
            </label>
            <input
              type="text"
              v-model="form.account"
              class="form-control extra-small"
              placeholder="請輸入英文或數字帳號"
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
        </div>

        <h6 class="fw-bold text-primary border-bottom pb-2 mb-3 extra-small">
          <i class="bi bi-shield-lock me-1"></i> 安全性與權限設定
        </h6>

        <div class="row g-3 mb-4">
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

          <!-- 帳號狀態 -->
          <div class="col-md-6">
            <label
              class="form-label extra-small fw-semibold text-secondary mb-1"
              >帳號初始狀態</label
            >
            <select v-model="form.status" class="form-select extra-small">
              <option :value="1">啟用 (直接可登入)</option>
              <option :value="0">停用</option>
              <option :value="2">待審核</option>
            </select>
          </div>

          <!-- 強制修改密碼 Checkbox -->
          <div class="col-12 mt-3">
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
                強制使用者於首次登入時修改密碼 (建議勾選)
              </label>
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
            <i class="bi bi-check-lg me-1"></i> 儲存建立
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();

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
const handleSubmit = async () => {
  try {
    // 串接 API 範例：await axios.post('/api/users', form.value)
    console.log("新增使用者資料：", form.value);

    alert("使用者新增成功！");

    //  按下確定後，自動導回使用者管理頁面
    router.push({ name: "user-management" });
  } catch (error) {
    console.error("新增失敗：", error);
    alert("建立失敗，請確認欄位是否填寫正確！");
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
</style>
