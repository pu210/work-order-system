import axios from "axios";
import { getToken, getCurrentUser, saveAuth, clearAuth } from "@/utils/auth.js";
import { notify } from "@/plugins/notify.js";
import { getErrorMessage } from "@/utils/apiError.js";

// const baseURL = import.meta.env.VITE_API_URL || "http://localhost:8080";

// Docker 化：改用 ?? 而非 ||，讓 VITE_API_URL 可以被明確設成空字串（代表走相對路徑，
// 由 nginx 反向代理轉發給後端），「||」會把空字串也當成沒設定，錯誤地退回寫死的 localhost:8080
const baseURL = import.meta.env.VITE_API_URL ?? "http://localhost:8080";

const instance = axios.create({
  baseURL,
  withCredentials: true,
});

let refreshPromise = null;
let handlingPermissionChange = false;

export async function refreshAccessToken() {
  if (!refreshPromise) {
    refreshPromise = axios
      .post(`${baseURL}/api/auth/refresh`, null, {
        withCredentials: true,
      })
      .then((response) => {
        const data = response.data?.data;

        if (!data?.token) {
          throw new Error("刷新回應缺少 Access Token");
        }

        saveAuth(data);

        return data.token;
      })
      .finally(() => {
        refreshPromise = null;
      });
  }

  return refreshPromise;
}

function redirectToLogin() {
  clearAuth();

  const returnUrl = `${window.location.pathname}${window.location.search}`;

  window.location.assign(
    `/auth/login?returnUrl=${encodeURIComponent(returnUrl)}`,
  );
}

instance.interceptors.request.use((config) => {
  const token = getToken();
  // 只有在未提供自訂 Authorization 且為內部 API 請求時，才注入系統 JWT Token
  if (
    token &&
    !config.headers.Authorization &&
    !config.url?.startsWith("http")
  ) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

instance.interceptors.response.use(
  (response) => response,
  async (error) => {
    const status = error.response?.status;
    const code = error.response?.data?.code;
    const config = error.config || {};

    // 對外部 API (例如 Google API) 的 401 錯誤不引發系統登出轉址
    const isExternalUrl =
      config.url?.startsWith("http://") || config.url?.startsWith("https://");

    if (status === 401 && !config.skipAuthRedirect && !isExternalUrl) {
      // 判定是否嘗試重刷 Token (未重試過才嘗試)
      const shouldTryRefresh = !config._retry;

      if (shouldTryRefresh) {
        config._retry = true;

        try {
          const newAccessToken = await refreshAccessToken();

          config.headers = config.headers || {};
          config.headers.Authorization = `Bearer ${newAccessToken}`;

          return instance(config);
        } catch (refreshError) {
          redirectToLogin();
          return Promise.reject(refreshError);
        }
      } else {
        redirectToLogin();
        return Promise.reject(error);
      }
    }

    if (status === 403) {
      if (code === "PASSWORD_CHANGE_REQUIRED") {
        if (window.location.pathname !== "/account/initial-password") {
          window.location.replace("/account/initial-password");
        }

        return Promise.reject(error);
      }

      const shouldCheckRoleChange =
        config.checkRoleChangeOnForbidden || !config.skipForbiddenRedirect;

      if (!shouldCheckRoleChange) {
        return Promise.reject(error);
      }

      if (handlingPermissionChange) {
        return Promise.reject(error);
      }

      handlingPermissionChange = true;

      try {
        const previousRoleCodes = getCurrentUser()?.roleCodes ?? [];

        await refreshAccessToken();

        const currentRoleCodes = getCurrentUser()?.roleCodes ?? [];

        const rolesChanged =
          JSON.stringify([...previousRoleCodes].sort()) !==
          JSON.stringify([...currentRoleCodes].sort());

        if (rolesChanged) {
          sessionStorage.setItem(
            "permission_changed_message",
            "你的使用權限已變更，已為你更新可使用的功能。",
          );

          window.location.replace("/dashboard");
        } else if (!config.skipForbiddenRedirect) {
          // 一般未略過全域轉址的 403，維持原本行為。
          window.location.replace("/forbidden");
        } else {
          // 角色沒有改變，而且呼叫端要自行處理這個 403。
          handlingPermissionChange = false;
        }
      } catch {
        redirectToLogin();
      }

      return Promise.reject(error);
    }

    if (!config.skipGlobalError) {
      if (!error.response || status >= 500) {
        await notify.error(getErrorMessage(error));
      }
    }

    return Promise.reject(error);
  },
);

export default instance;
