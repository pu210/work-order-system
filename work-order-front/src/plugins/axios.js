import axios from "axios";
import { getToken, saveAuth, clearAuth } from "@/utils/auth.js";
import { notify } from "@/plugins/notify.js";
import { getErrorMessage } from "@/utils/apiError.js";

const baseURL = import.meta.env.VITE_API_URL || "http://localhost:8080";

const instance = axios.create({
  baseURL,
  withCredentials: true,
});

let refreshPromise = null;

export async function refreshAccessToken() {
  if (!refreshPromise) {
    refreshPromise = axios
      .post(`${baseURL}/auth/refresh`, null, {
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

  if (token) {
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

    const shouldTryRefresh =
      status === 401 && !config.skipAuthRedirect && !config._retry;

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
    }

    if (status === 401 && !config.skipAuthRedirect) {
      redirectToLogin();
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
