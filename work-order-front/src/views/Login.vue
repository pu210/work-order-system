<template>
  <div class="login-view">
    <!-- 標題 -->
    <h2 class="text-center fw-bold mb-4 page-title">工單系統登入</h2>

    <form>
      <!-- 帳號區塊 (人頭圖示) -->
      <div class="mb-3">
        <label class="form-label text-dark small fw-medium mb-1">帳號</label>
        <div class="input-group custom-input-group">
          <span class="input-group-text bg-white border-end-0 text-secondary ps-3">
            <i class="bi bi-person fs-5"></i>
          </span>
          <input 
            type="text" 
            class="form-control border-start-0 ps-2" 
            placeholder="請輸入帳號"
          >
        </div>
      </div>

      <!-- 密碼區塊 (鑰匙圖示) -->
      <div class="mb-2">
        <label class="form-label text-dark small fw-medium mb-1">密碼</label>
        <div class="input-group custom-input-group">
          <span class="input-group-text bg-white border-end-0 text-secondary ps-3">
            <i class="bi bi-key fs-5"></i>
          </span>
          <input 
            type="password" 
            class="form-control border-start-0 border-end-0 ps-2" 
            placeholder="請輸入密碼"
          >
          <span class="input-group-text bg-white border-start-0 text-secondary pe-3">
            <i class="bi bi-eye fs-5"></i>
          </span>
        </div>
      </div>

      <!-- 忘記密碼？ -->
      <div class="text-end mb-4">
        <router-link to="/forgot-password" class="text-decoration-none small text-primary fw-medium">
          忘記密碼？
        </router-link>
      </div>

      <!-- 紅色登入按鈕 -->
      <button type="submit" class="btn btn-red w-100 py-2.5 fw-bold mb-3">
        登入
      </button>

      <!-- 「或」分隔線 -->
      <div class="d-flex align-items-center my-3">
        <hr class="flex-grow-1 m-0 text-secondary opacity-25">
        <span class="px-3 text-secondary small">或</span>
        <hr class="flex-grow-1 m-0 text-secondary opacity-25">
      </div>

      <!-- 註冊按鈕 -->
      <button type="button" class="btn btn-outline-custom w-100 py-2.5 fw-bold mb-3">
        註冊
      </button>

      <!-- Google 登入按鈕 -->
      <a 
        href="https://accounts.google.com/o/oauth2/v2/auth" 
        class="btn btn-google w-100 py-2.5 fw-medium d-flex align-items-center justify-content-center gap-2 text-decoration-none"
      >
        <i class="bi bi-google fs-5"></i>
        <span>使用 Google 帳號登入</span>
      </a>
    </form>
  </div>
</template>

<style scoped>
.page-title {
  letter-spacing: 1.5px;
  font-size: 1.75rem;
  color: #1f2937;
}

/* 輸入框樣式 */
.custom-input-group .form-control,
.custom-input-group .input-group-text {
  border-color: #dcdfe6;
  padding-top: 0.65rem;
  padding-bottom: 0.65rem;
}

.custom-input-group .form-control:focus {
  box-shadow: none;
  border-color: #dcdfe6;
}

/* 鮮紅色按鈕 */
.btn-red {
  background-color: #dc2626;
  border-color: #dc2626;
  color: #ffffff;
  border-radius: 8px;
  font-size: 1rem;
}

.btn-red:hover {
  background-color: #b91c1c;
  border-color: #b91c1c;
  color: #ffffff;
}

/* 灰色邊框註冊按鈕 */
.btn-outline-custom {
  border-color: #dcdfe6;
  color: #374151;
  border-radius: 8px;
  background-color: #ffffff;
  font-size: 1rem;
}

.btn-outline-custom:hover {
  background-color: #f9fafb;
  border-color: #c0c4cc;
  color: #111827;
}

/* Google 登入按鈕 */
.btn-google {
  border-color: #dcdfe6;
  color: #374151;
  border-radius: 8px;
  background-color: #ffffff;
  font-size: 0.95rem;
  transition: all 0.2s ease;
}

.btn-google:hover {
  background-color: #f8fafc;
  border-color: #cbd5e1;
  color: #0f172a;
}

</style>