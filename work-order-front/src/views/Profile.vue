<template>
  <div class="profile-page mx-auto">
    <div class="mb-4">
      <h3 class="fw-bold mb-1">個人設定</h3>
    </div>

    <div v-if="loadError" class="alert alert-danger" role="alert">
      {{ loadError }}
    </div>

    <div v-if="isLoading" class="card p-5 text-center">
      <div class="spinner-border text-primary mx-auto mb-2"></div>
      <div class="text-muted small">正在載入個人資料...</div>
    </div>

    <div v-else class="row g-4 align-items-stretch">
      <!-- 基本資料 -->
      <div class="col-lg-6">
        <div class="card bg-white p-4 h-100 d-flex flex-column">
          <h5 class="fw-bold border-bottom pb-3 mb-4">
            <i class="bi bi-person-vcard me-2"></i>
            基本資料
          </h5>

          <form
            class="d-flex flex-column flex-grow-1"
            @submit.prevent="handleProfileSubmit"
          >
            <div class="row g-3">
              <!-- 帳號 -->
              <div class="col-md-6">
                <label
                  for="account"
                  class="form-label extra-small fw-semibold text-secondary mb-1"
                >
                  帳號
                </label>
                <input
                  id="account"
                  v-model="profileForm.account"
                  type="text"
                  class="form-control extra-small bg-light"
                  disabled
                />
              </div>

              <!-- 角色 -->
              <div class="col-md-6">
                <label
                  class="form-label extra-small fw-semibold text-secondary mb-1"
                >
                  角色
                </label>

                <input
                  :value="roleLabel"
                  type="text"
                  class="form-control extra-small bg-light"
                  disabled
                />
              </div>

              <!-- 姓名 -->
              <div class="col-md-6">
                <label
                  for="name"
                  class="form-label extra-small fw-semibold text-secondary mb-1"
                >
                  姓名
                  <span class="text-danger">*</span>
                </label>

                <input
                  id="name"
                  v-model="profileForm.name"
                  type="text"
                  class="form-control extra-small"
                  maxlength="50"
                  required
                />
              </div>

              <!-- 電話 -->
              <div class="col-md-6">
                <label
                  for="phone"
                  class="form-label extra-small fw-semibold text-secondary mb-1"
                >
                  聯絡電話
                </label>

                <input
                  id="phone"
                  v-model="profileForm.phone"
                  type="tel"
                  class="form-control extra-small"
                  maxlength="10"
                  inputmode="numeric"
                  placeholder="例如：0912345678"
                />
              </div>

              <!-- Email -->
              <div class="col-12">
                <label
                  for="email"
                  class="form-label extra-small fw-semibold text-secondary mb-1"
                >
                  電子郵件
                  <span class="text-danger">*</span>
                </label>

                <input
                  id="email"
                  v-model="profileForm.email"
                  type="email"
                  class="form-control extra-small"
                  required
                />
              </div>
            </div>

            <div
              v-if="profileError"
              class="alert alert-danger py-2 mt-3 mb-0"
              role="alert"
            >
              {{ profileError }}
            </div>

            <div class="d-flex justify-content-end mt-auto pt-4">
              <button
                type="submit"
                class="btn btn-primary px-4"
                :disabled="isSavingProfile"
              >
                <span
                  v-if="isSavingProfile"
                  class="spinner-border spinner-border-sm me-1"
                ></span>

                {{ isSavingProfile ? "儲存中..." : "儲存基本資料" }}
              </button>
            </div>
          </form>
        </div>
      </div>

      <!-- 修改密碼 -->
      <div class="col-lg-6">
        <div class="card bg-white p-4 h-100 d-flex flex-column">
          <h5 class="fw-bold border-bottom pb-3 mb-4">
            <i class="bi bi-shield-lock me-2"></i>
            修改密碼
          </h5>

          <form
            class="d-flex flex-column flex-grow-1"
            @submit.prevent="handlePasswordSubmit"
          >
            <div class="row g-3">
              <div class="col-12">
                <label
                  for="currentPassword"
                  class="form-label extra-small fw-semibold text-secondary mb-1"
                >
                  目前密碼
                </label>

                <input
                  id="currentPassword"
                  v-model="passwordForm.currentPassword"
                  type="password"
                  class="form-control extra-small"
                  autocomplete="current-password"
                  required
                />
              </div>

              <div class="col-md-6">
                <label
                  for="newPassword"
                  class="form-label extra-small fw-semibold text-secondary mb-1"
                >
                  新密碼
                </label>

                <input
                  id="newPassword"
                  v-model="passwordForm.newPassword"
                  type="password"
                  class="form-control extra-small"
                  autocomplete="new-password"
                  minlength="8"
                  required
                />

                <div class="form-text extra-small">
                  新密碼至少需要 8 個字元。
                </div>
              </div>

              <div class="col-md-6">
                <label
                  for="confirmPassword"
                  class="form-label extra-small fw-semibold text-secondary mb-1"
                >
                  確認新密碼
                </label>

                <input
                  id="confirmPassword"
                  v-model="passwordForm.confirmPassword"
                  type="password"
                  class="form-control extra-small"
                  autocomplete="new-password"
                  minlength="8"
                  required
                />
              </div>
            </div>

            <div
              v-if="passwordError"
              class="alert alert-danger py-2 mt-3 mb-0"
              role="alert"
            >
              {{ passwordError }}
            </div>

            <div class="d-flex justify-content-end mt-auto pt-4">
              <button
                type="submit"
                class="btn btn-primary px-4"
                :disabled="isChangingPassword"
              >
                <span
                  v-if="isChangingPassword"
                  class="spinner-border spinner-border-sm me-1"
                ></span>

                {{ isChangingPassword ? "修改中..." : "修改密碼" }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { getProfile, updateProfile, changePassword } from "@/api/account.js";
import { useAuthStore } from "@/stores/auth.js";
import { getErrorMessage } from "@/utils/apiError.js";
import { notify } from "@/plugins/notify.js";
import { userRoleLabel } from "@/constants/userRole.js";

const authStore = useAuthStore();

const isLoading = ref(false);
const isSavingProfile = ref(false);
const isChangingPassword = ref(false);

const loadError = ref("");
const profileError = ref("");
const passwordError = ref("");

const profileForm = reactive({
  account: "",
  name: "",
  email: "",
  phone: "",
  status: null,
  roleCodes: [],
});

const passwordForm = reactive({
  currentPassword: "",
  newPassword: "",
  confirmPassword: "",
});

const roleLabel = computed(() => {
  if (profileForm.roleCodes.length === 0) {
    return "尚未設定";
  }

  return profileForm.roleCodes.map(userRoleLabel).join("、");
});

async function loadProfile() {
  isLoading.value = true;
  loadError.value = "";

  try {
    const profile = await getProfile();

    profileForm.account = profile.account;
    profileForm.name = profile.name;
    profileForm.email = profile.email;
    profileForm.phone = profile.phone ?? "";
    profileForm.status = profile.status;
    profileForm.roleCodes = [...(profile.roleCodes ?? [])];
  } catch (error) {
    loadError.value = getErrorMessage(error, "個人資料載入失敗");
  } finally {
    isLoading.value = false;
  }
}

async function handleProfileSubmit() {
  if (isSavingProfile.value) return;

  profileError.value = "";

  const name = profileForm.name.trim();
  const email = profileForm.email.trim();
  const phone = profileForm.phone.trim();

  const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  const phonePattern = /^\d{10}$/;

  if (!name) {
    profileError.value = "請輸入姓名";
    return;
  }

  if (!emailPattern.test(email)) {
    profileError.value = "請輸入正確的電子郵件格式";
    return;
  }

  if (phone && !phonePattern.test(phone)) {
    profileError.value = "聯絡電話需為 10 碼數字";
    return;
  }

  isSavingProfile.value = true;

  try {
    const updatedProfile = await updateProfile({
      name,
      email,
      phone,
    });

    profileForm.name = updatedProfile.name;
    profileForm.email = updatedProfile.email;
    profileForm.phone = updatedProfile.phone ?? "";

    // 同步導覽列顯示的姓名與信箱
    authStore.syncProfile({
      name: updatedProfile.name,
      email: updatedProfile.email,
    });

    notify.success("個人資料更新成功");
  } catch (error) {
    notify.error(getErrorMessage(error, "個人資料更新失敗"));
  } finally {
    isSavingProfile.value = false;
  }
}

async function handlePasswordSubmit() {
  if (isChangingPassword.value) return;

  passwordError.value = "";

  if (passwordForm.newPassword.length < 8) {
    passwordError.value = "新密碼至少需要 8 個字元";
    return;
  }

  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    passwordError.value = "兩次輸入的新密碼不一致";
    return;
  }

  if (passwordForm.currentPassword === passwordForm.newPassword) {
    passwordError.value = "新密碼不可與目前密碼相同";
    return;
  }

  isChangingPassword.value = true;

  try {
    await changePassword({
      currentPassword: passwordForm.currentPassword,
      newPassword: passwordForm.newPassword,
      confirmPassword: passwordForm.confirmPassword,
    });

    passwordForm.currentPassword = "";
    passwordForm.newPassword = "";
    passwordForm.confirmPassword = "";

    notify.success("密碼修改成功");
  } catch (error) {
    notify.error(getErrorMessage(error, "密碼修改失敗"));
  } finally {
    isChangingPassword.value = false;
  }
}

onMounted(loadProfile);
</script>

<style scoped>
.extra-small {
  font-size: 0.82rem;
}

.profile-page {
  max-width: 1200px;
}

.card {
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
}
</style>
