<!-- src/layouts/MainLayout.vue -->
<template>
  <div class="main-layout d-flex flex-column vh-100 overflow-hidden bg-light">
    <!-- 上方固定導覽列 -->
    <AppNav
      @toggle-chat="isChatOpen = !isChatOpen"
      :is-chat-open="isChatOpen"
    />

    <!-- 下方主體：包含中間畫面與右側聊天室 -->
    <div class="d-flex flex-grow-1 overflow-hidden position-relative">
      <!-- 切換頁面的主要內容區 -->
      <main class="flex-grow-1 overflow-auto p-4 main-content">
        <div class="container-fluid max-w-7xl">
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </div>
      </main>

      <!-- 右側即時聊天側邊欄 -->
      <ChatSidebar :is-open="isChatOpen" @close="isChatOpen = false" />
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import AppNav from "@/components/AppNav.vue";
import ChatSidebar from "@/components/ChatSidebar.vue";

const isChatOpen = ref(false);
</script>

<style scoped>
.main-layout {
  width: 100%;
  min-height: 100vh;
  min-height: 100dvh;
  height: 100dvh !important;
  font-family:
    -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue",
    Arial, sans-serif;
  color: #333333;
}

.main-content {
  min-width: 0;
  background-color: #f8f9fa;
  scroll-behavior: smooth;
}

/* 簡約淡入淡出過場 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.max-w-7xl {
  max-width: 1400px;
  margin: 0 auto;
}
</style>
