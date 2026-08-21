import { defineStore } from "pinia";
import { ref, computed } from "vue";
import { getToken, getCurrentUser, saveAuth, clearAuth } from "@/utils/auth.js";
import api from "@/plugins/axios.js";

export const useAuthStore = defineStore("auth", () => {
  // ---- 1. State (狀態：使用 ref) ----
  const user = getCurrentUser();
  const token = ref(getToken());
  const userId = ref(user?.userId ?? null);
  const account = ref(user?.account ?? null);
  const name = ref(user?.name ?? null);
  const email = ref(user?.email ?? null);
  const roleCodes = ref(user?.roleCodes ?? []);
  const mustChangePassword = ref(user?.mustChangePassword ?? false);

  // ---- 2. Getters (計算屬性：使用 computed) ----
  const isAuthenticated = computed(() => !!token.value);
  const hasRole = computed(() => (role) => roleCodes.value.includes(role));

  // ---- 3. Actions (動作：使用函數) ----
  function login(data) {
    saveAuth(data);
    token.value = data.token;
    userId.value = data.userId;
    account.value = data.account;
    name.value = data.name;
    email.value = data.email;
    roleCodes.value = data.roleCodes || [];
    mustChangePassword.value = data.mustChangePassword ?? false;
  }

  async function logout() {
    try {
      await api.post("/auth/logout", null, {
        skipAuthRedirect: true,
        skipGlobalError: true,
      });
    } finally {
      clearAuth();

      token.value = null;
      userId.value = null;
      account.value = null;
      name.value = null;
      email.value = null;
      roleCodes.value = [];
    }
  }

  // ---- 4. Return (對外公開的屬性與函數) ----
  return {
    token,
    userId,
    account,
    name,
    email,
    roleCodes,
    mustChangePassword,
    isAuthenticated,
    hasRole,
    login,
    logout,
  };
});
