import axios from "axios";
import { getToken, saveAuth, clearAuth } from "@/utils/auth.js";
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
      } else if (config.forceForbiddenRedirect) {
        if (window.location.pathname !== "/forbidden") {
          window.location.replace("/forbidden");
        }
      } else if (!config.skipGlobalError) {
        const msg = error.response?.data?.message || getErrorMessage(error) || "您無權限執行此操作";
        notify.error(msg);
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
