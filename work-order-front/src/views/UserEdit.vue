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
      <div
        v-if="errorMessage"
        class="alert alert-danger extra-small mb-3"
        role="alert"
      >
        {{ errorMessage }}
      </div>

      <div
        v-if="isLoading"
        class="d-flex justify-content-center align-items-center py-5"
      >
        <div class="text-center text-muted">
          <div class="spinner-border text-primary mb-2" role="status">
            <span class="visually-hidden">載入中</span>
          </div>
          <div class="extra-small">正在載入使用者資料...</div>
        </div>
      </div>

      <form v-else @submit.prevent="handleSubmit">
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
              v-model.trim="form.email"
              class="form-control extra-small"
              placeholder="name@example.com"
              title="請輸入正確的電子郵件格式"
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
              v-model.trim="form.phone"
              class="form-control extra-small"
              placeholder="例如：0912345678"
              pattern="[0-9]{10}"
              maxlength="10"
              inputmode="numeric"
              title="聯絡電話需為 10 碼數字"
            />
          </div>

          <!-- 使用者角色 -->
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
                  :disabled="
                    role.value === 'ADMIN' &&
                    isLastActiveAdmin &&
                    form.roleCodes.includes('ADMIN')
                  "
                />

                <span class="form-check-label extra-small">
                  {{ role.label }}
                </span>
              </label>
            </div>
            <div
              v-if="isLastActiveAdmin"
              class="form-text text-warning extra-small"
            >
              <i class="bi bi-exclamation-triangle me-1"></i>
              此帳號是目前最後一位啟用中的管理員，無法移除管理員角色。
            </div>

            <div v-else class="form-text extra-small">可選擇一個或多個角色</div>
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
            :disabled="isSubmitting || isLoading"
          >
            <span
              v-if="isSubmitting"
              class="spinner-border spinner-border-sm me-1"
              role="status"
            ></span>
            <i v-else class="bi bi-check-lg me-1"></i>

            {{ isSubmitting ? "儲存中..." : "儲存變更" }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { getUser, updateUser } from "@/api/user.js";
import { getErrorMessage } from "@/utils/apiError.js";
import { useAuthStore } from "@/stores/auth.js";
import { notify } from "@/plugins/notify.js";

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const userId = route.params.id;
const isLoading = ref(false);
const errorMessage = ref("");
const isSubmitting = ref(false);
const isLastActiveAdmin = ref(false);

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
  status: 1, // Default 啟用
  must_change_password: true, // Default true
  roleCodes: [],
});

const loadUser = async () => {
  isLoading.value = true;
  errorMessage.value = "";

  try {
    const user = await getUser(userId);

    form.value.account = user.account;
    form.value.name = user.name;
    form.value.email = user.email;
    form.value.phone = user.phone ?? "";
    form.value.status = user.status;
    form.value.must_change_password = user.mustChangePassword;
    form.value.roleCodes = [...(user.roleCodes ?? [])];
    isLastActiveAdmin.value = user.lastActiveAdmin === true;
  } catch (error) {
    errorMessage.value = getErrorMessage(error, "使用者資料載入失敗");
  } finally {
    isLoading.value = false;
  }
};

onMounted(loadUser);

// 送出表單處理
const handleSubmit = async () => {
  if (isSubmitting.value) return;

  errorMessage.value = "";

  const email = form.value.email.trim();
  const phone = form.value.phone.trim();
  const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  const phonePattern = /^\d{10}$/;

  if (!emailPattern.test(email)) {
    errorMessage.value = "請輸入正確的電子郵件格式";
    notify.error(errorMessage.value);
    return;
  }

  if (phone && !phonePattern.test(phone)) {
    errorMessage.value = "聯絡電話需為 10 碼數字";
    notify.error(errorMessage.value);
    return;
  }

  if (form.value.roleCodes.length === 0) {
    errorMessage.value = "請至少選擇一個使用者角色";
    notify.error(errorMessage.value);
    return;
  }
  const isEditingSelf = Number(userId) === Number(authStore.userId);
  const isRemovingOwnAdmin =
    isEditingSelf &&
    authStore.roleCodes.includes("ADMIN") &&
    !form.value.roleCodes.includes("ADMIN");

  if (isRemovingOwnAdmin) {
    const result = await notify.confirm({
      title: "確定要移除自己的管理員角色？",
      text: "儲存後將立即失去管理權限，且無法自行恢復，必須由其他管理員重新指派。",
      confirmButtonText: "確定",
      cancelButtonText: "取消",
    });

    if (!result.isConfirmed) {
      return;
    }
  }
  isSubmitting.value = true;

  try {
    const payload = {
      name: form.value.name.trim(),
      email,
      phone,
      status: form.value.status,
      roleCodes: form.value.roleCodes,
    };

    const updatedUser = await updateUser(userId, payload);

    const isEditingSelf = updatedUser.userId === authStore.userId;

    if (isEditingSelf) {
      authStore.syncProfile({
        name: updatedUser.name,
        email: updatedUser.email,
        roleCodes: updatedUser.roleCodes,
      });
    }

    notify.success("使用者資料更新成功！");

    if (isEditingSelf && !updatedUser.roleCodes.includes("ADMIN")) {
      router.replace({ name: "Dashboard" });
    } else {
      router.push({ name: "user-management" });
    }
  } catch (error) {
    errorMessage.value = getErrorMessage(error, "使用者資料更新失敗");
    notify.error(errorMessage.value);
  } finally {
    isSubmitting.value = false;
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
