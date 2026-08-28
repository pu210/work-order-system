<template>
  <div
    class="auth-layout d-flex align-items-center justify-content-center position-relative"
  >
    <!-- 中央登入卡片 -->
    <div
      :class="['auth-card', { 'auth-card--glass': route.name === 'Login' }]"
      class="p-4 p-sm-5 w-100 position-relative z-1"
    >
      <!-- 標題區 -->
      <div class="auth-brand text-center mb-4">
        <img
          src="/favicon.ico"
          alt="GongXia 蝦子吉祥物"
          class="auth-brand-logo"
        />
        <h1 class="auth-brand-name m-0">GongXia</h1>
        <p class="auth-brand-subtitle extra-small mt-1 mb-0">
          企業級設備與工單管理系統
        </p>
      </div>

      <!-- 登入頁面內容 -->
      <router-view />
    </div>
  </div>
</template>

<script setup>
import { useRoute } from "vue-router";

const route = useRoute();
</script>

<style scoped>
.auth-layout {
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100dvh;
  padding: 24px;
  box-sizing: border-box;
  overflow-y: auto;
  background-color: #f3f4f6;
}

.auth-layout::before {
  position: fixed;
  inset: 0;
  content: "";
  background-image: url("/shrimp-pattern.svg");
  background-repeat: repeat;
  background-position: center;
  background-size: 160px 106px;
  opacity: 0.1;
  filter: grayscale(15%) saturate(75%);
  pointer-events: none;
  user-select: none;
}

.auth-card {
  max-width: 420px;
  background: #fff;
  border: 1px solid #dfe3e8;
  border-radius: 10px;
  box-shadow: 0 4px 14px rgba(20, 33, 61, 0.08);
}

.auth-card--glass {
  background:
    linear-gradient(
      135deg,
      rgba(255, 255, 255, 0.24) 0%,
      rgba(255, 255, 255, 0.17) 48%,
      rgba(255, 255, 255, 0.1) 100%
    );
  border: 1px solid rgba(255, 255, 255, 0.72);
  border-radius: 18px;
  box-shadow:
    0 24px 60px rgba(30, 41, 59, 0.18),
    0 4px 14px rgba(30, 41, 59, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 1),
    inset 0 -1px 0 rgba(255, 255, 255, 0.32);
  backdrop-filter: blur(5px) saturate(135%);
  -webkit-backdrop-filter: blur(5px) saturate(135%);
}

@supports not ((backdrop-filter: blur(1px)) or (-webkit-backdrop-filter: blur(1px))) {
  .auth-card--glass {
    background: rgba(255, 255, 255, 0.94);
  }
}

.extra-small {
  font-size: 0.8rem;
}

.auth-brand-logo {
  display: block;
  width: 68px;
  height: 68px;
  margin: -4px auto 6px;
  object-fit: contain;
  filter: drop-shadow(0 8px 10px rgba(249, 82, 47, 0.16));
}

.auth-brand-name {
  color: #f04c2f;
  font-size: clamp(2rem, 7vw, 2.5rem);
  font-weight: 800;
  line-height: 1.2;
  padding-bottom: 0.08em;
  letter-spacing: 1.5px;
  background: linear-gradient(120deg, #ff6b35 12%, #e9342f 55%, #d92355 100%);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.auth-brand-subtitle {
  color: #64748b;
  font-weight: 500;
  letter-spacing: 0.08em;
}

@media (max-width: 575.98px) {
  .auth-layout {
    padding: 16px;
  }

  .auth-layout::before {
    background-size: 149px 99px;
    opacity: 0.08;
  }
}

</style>
