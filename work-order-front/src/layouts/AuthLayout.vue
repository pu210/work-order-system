<template>
  <!-- 最外層全白背景 + 絕對水平垂直置中 -->
  <div class="layout-wrapper">
    <!-- 中央白色卡片外框 -->
    <main class="card main-card">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
/* 絕對置中容器 */
.layout-wrapper {
  background-color: #ffffff !important;
  width: 100%;
  min-height: 100vh;
  display: flex !important;
  justify-content: center !important; /* 水平置中 */
  align-items: center !important; /* 垂直置中 */
  padding: 20px;
  box-sizing: border-box;
}

/* 質感卡片外框 */
.main-card {
  width: 100%;
  max-width: 420px;
  border-radius: 20px !important;
  background-color: #ffffff;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.04) !important;
  border: 1px solid #eaeaea !important;
  padding: 2.5rem 2rem;
  margin: 0 auto; /* 確保 margin 雙重保險置中 */
}
</style>
