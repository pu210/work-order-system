import { fileURLToPath, URL } from "node:url";

import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import vueDevTools from "vite-plugin-vue-devtools";
import { randomUUID } from "node:crypto";

const devServerSessionId = randomUUID();

// https://vite.dev/config/
export default defineConfig({
  define: {
    __DEV_SERVER_SESSION_ID__: JSON.stringify(devServerSessionId),
  },

  plugins: [vue(), vueDevTools()],

  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url)),
    },
  },

  optimizeDeps: {
    include: [
      "@fullcalendar/core",
      "@fullcalendar/vue3",
      "@fullcalendar/daygrid",
      "@fullcalendar/timegrid",
      "@fullcalendar/interaction",
    ],
  },
});
