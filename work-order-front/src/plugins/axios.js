import axios from "axios";
import { getToken, clearAuth } from "@/utils/auth.js";
import { notify } from "@/plugins/notify.js";
import { getErrorMessage } from "@/utils/apiError.js";

const instance = axios.create({
  baseURL: import.meta.env.VITE_API_URL || "http://localhost:8080",
});
instance.interceptors.request.use((config) => {
  const token = getToken();
  // 只有在未提供自訂 Authorization 且為內部 API 請求時，才注入系統 JWT Token
  if (token && !config.headers.Authorization && !config.url?.startsWith("http")) {
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
    const isExternalUrl = config.url?.startsWith("http://") || config.url?.startsWith("https://");

    if (status === 401 && !config.skipAuthRedirect && !isExternalUrl) {
      clearAuth();

      const returnUrl = `${window.location.pathname}${window.location.search}`;

      window.location.assign(
        `/auth/login?returnUrl=${encodeURIComponent(returnUrl)}`,
      );

      return Promise.reject(error);
    }

    if (status === 403) {
      if (code === "PASSWORD_CHANGE_REQUIRED") {
        window.location.assign("/account/initial-password");
      } else if (!config.skipForbiddenRedirect) {
        window.location.assign("/forbidden");
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
