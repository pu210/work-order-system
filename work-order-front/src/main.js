import { createApp } from "vue";
import axios from "axios";
import App from "./App.vue";
import { createPinia } from "pinia";
import router from "./router/router.js";
import piniaPluginPersistedstate from "pinia-plugin-persistedstate";
import { clearAuth } from "@/utils/auth.js";
import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap/dist/js/bootstrap.bundle.min.js";
import "bootstrap-icons/font/bootstrap-icons.css";
import "./assets/styles/design-system.css";

const DEV_SERVER_SESSION_KEY = "dev_server_session_id";

async function bootstrap() {
  const logoutOnStartup =
    import.meta.env.DEV && import.meta.env.VITE_LOGOUT_ON_STARTUP === "true";

  const previousSessionId = localStorage.getItem(DEV_SERVER_SESSION_KEY);

  const devServerRestarted =
    logoutOnStartup && previousSessionId !== __DEV_SERVER_SESSION_ID__;

  if (devServerRestarted) {
    const baseURL = import.meta.env.VITE_API_URL ?? "http://localhost:8080";

    try {
      await axios.post(`${baseURL}/api/auth/logout`, null, {
        withCredentials: true,
      });

      localStorage.setItem(DEV_SERVER_SESSION_KEY, __DEV_SERVER_SESSION_ID__);
    } catch (error) {
      console.warn("開發環境啟動登出失敗", error);
    } finally {
      clearAuth();
    }
  }

  const pinia = createPinia().use(piniaPluginPersistedstate);

  createApp(App).use(router).use(pinia).mount("#app");
}

bootstrap();
