<template>
  <div class="settings-container">
    <h2 class="page-title">系統基礎設定</h2>

    <!-- 頁籤切換按鈕 -->
    <div class="action-bar" style="margin-bottom: 24px;">
      <div style="display: flex; gap: 10px; flex-wrap: wrap;">
        <button 
          class="btn-search" 
          :style="activeTab === 'category' ? '' : 'background-color: #f3f4f6; color: #4b5563; border-color: #d1d5db;'"
          @click="activeTab = 'category'"
        >
          報修大類管理
        </button>
        <button 
          class="btn-search" 
          :style="activeTab === 'subCategory' ? '' : 'background-color: #f3f4f6; color: #4b5563; border-color: #d1d5db;'"
          @click="activeTab = 'subCategory'"
        >
          報修子類管理
        </button>
        <button 
          class="btn-search" 
          :style="activeTab === 'priority' ? '' : 'background-color: #f3f4f6; color: #4b5563; border-color: #d1d5db;'"
          @click="activeTab = 'priority'"
        >
          優先級管理
        </button>
        <button 
          class="btn-search" 
          :style="activeTab === 'target' ? '' : 'background-color: #f3f4f6; color: #4b5563; border-color: #d1d5db;'"
          @click="activeTab = 'target'"
        >
          報修設備管理
        </button>
      </div>
    </div>

    <!-- 1. 報修大類管理內容 -->
    <div v-if="activeTab === 'category'">
      <SettingCategory />
    </div>

    <!-- 2. 報修子類管理內容 -->
    <div v-if="activeTab === 'subCategory'">
      <SettingSubCategory />
    </div>

    <!-- 3. 優先級管理內容 -->
    <div v-if="activeTab === 'priority'">
      <SettingPriority />
    </div>

    <!-- 4. 報修設備管理內容 -->
    <div v-if="activeTab === 'target'">
      <SettingTarget />
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import SettingCategory from "@/views/SettingCategory.vue";
import SettingSubCategory from "@/views/SettingSubCategory.vue";
import SettingPriority from "@/views/SettingPriority.vue";
import SettingTarget from "@/views/SettingTarget.vue";

const activeTab = ref("category");
</script>

<style scoped>
.settings-container {
  padding: 24px;
  background-color: #f8fafc;
  min-height: calc(100vh - 64px);
  font-family: system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 20px;
}

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
}

.btn-search {
  background-color: #2563eb;
  color: #ffffff;
  border: 1px solid #2563eb;
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 500;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.btn-search:hover {
  background-color: #1d4ed8;
}
</style>