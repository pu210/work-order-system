<template>
  <div
    class="auth-layout d-flex align-items-center justify-content-center position-relative"
  >
    <!-- 中央登入卡片 -->
    <div class="auth-card p-4 p-sm-5 w-100 position-relative z-1">
      <!-- 標題區 -->
      <div class="text-center mb-4">
        <h4 class="fw-bold text-black m-0 tracking-wide">WorkOrder</h4>
        <p class="text-slate-400 extra-small mt-1 mb-0">
          企業級設備與工單管理系統
        </p>
      </div>

      <!-- 登入頁面內容 -->
      <router-view />
    </div>
  </div>
</template>

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

.text-slate-400 {
  color: #090909;
}

.auth-card {
  max-width: 420px;
  background: #fff;
  border: 1px solid #dfe3e8;
  border-radius: 10px;
  box-shadow: 0 4px 14px rgba(20, 33, 61, 0.08);
}

.extra-small {
  font-size: 0.8rem;
}

.tracking-wide {
  letter-spacing: 0.5px;
}

@media (max-width: 575.98px) {
  .auth-layout {
    padding: 16px;
  }
}

.auth-card {
  max-width: 420px;
  background: #fff;
  border: 1px solid #dfe3e8;
  border-radius: 10px;
  box-shadow: 0 4px 14px rgba(20, 33, 61, 0.08);
}

.extra-small {
  font-size: 0.8rem;
}

.tracking-wide {
  letter-spacing: 0.5px;
}

@media (max-width: 575.98px) {
  .auth-layout {
    padding: 16px;
  }
}
</style>
