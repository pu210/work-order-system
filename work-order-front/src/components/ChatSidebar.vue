 <!-- src/components/ChatSidebar.vue -->
<template>
  <aside 
    class="chat-sidebar bg-white border-start shadow-sm d-flex flex-column"
    :class="{ 'is-open': isOpen }"
  >
    <!-- 標頭 -->
    <div class="p-3 border-bottom d-flex align-items-center justify-content-between bg-light">
      <div class="d-flex align-items-center gap-2">
        <i class="bi bi-chat-dots-fill text-primary"></i>
        <h6 class="m-0 fw-semibold">即時對話討論</h6>
      </div>
      <button type="button" class="btn-close text-reset" @click="$emit('close')"></button>
    </div>

    <!-- 訊息內容區 -->
    <div class="flex-grow-1 p-3 overflow-auto d-flex flex-column gap-3">
      <div class="text-center text-muted small my-2">-- 今天 10:30 --</div>

      <div class="d-flex gap-2 align-items-start">
        <div class="avatar bg-secondary text-white rounded-circle small-avatar">王</div>
        <div class="bg-light p-2.5 rounded-3 border max-w-80">
          <div class="fw-bold small text-secondary">王大明 (設備組)</div>
          <div class="small mt-1">A區冷卻塔幫浦聲音異常，需要安排預防性維修嗎？</div>
        </div>
      </div>

      <div class="d-flex gap-2 align-items-start justify-content-end">
        <div class="bg-primary text-white p-2.5 rounded-3 max-w-80">
          <div class="small">已開立工單 #WO-20260805，稍後請張技師過去檢查。</div>
        </div>
      </div>
    </div>

    <!-- 輸入區 -->
    <div class="p-3 border-top bg-light">
      <div class="input-group">
        <input 
          type="text" 
          class="form-control form-control-sm border-end-0" 
          placeholder="輸入訊息..." 
        />
        <button class="btn btn-primary btn-sm px-3" type="button">
          <i class="bi bi-send-fill"></i>
        </button>
      </div>
    </div>
  </aside>
</template>

<script setup>
defineProps({
  isOpen: Boolean
})

defineEmits(['close'])
</script>

<style scoped>
.chat-sidebar {
  width: 320px;
  position: absolute;
  top: 0;
  right: -320px;
  bottom: 0;
  z-index: 1040;
  transition: right 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.chat-sidebar.is-open {
  right: 0;
  position: relative; /* 展開時擠壓/並排 content，不遮擋內容 */
}

.small-avatar {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}

.max-w-80 {
  max-width: 80%;
}
</style>