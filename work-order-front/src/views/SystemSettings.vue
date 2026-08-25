<template>
  <div class="mt-page">
    <div class="mt-page-header">
      <div>
        <span class="mt-eyebrow">SYSTEM SETTINGS</span>
        <h1 class="mt-title">系統基礎設定</h1>
        <p class="mt-subtitle">管理報修設備、優先級以及相關類別選項</p>
      </div>
    </div>

    <div class="mt-card">
      <!-- 頁籤切換：使用第一份的 mt-pill-tabs 樣式 -->
      <div class="mt-toolbar">
        <div class="mt-pill-tabs">
          <button
            type="button"
            class="mt-pill-tab"
            :class="{ active: activeTab === 'target' }"
            @click="activeTab = 'target'"
          >
            報修設備管理
          </button>
          <button
            type="button"
            class="mt-pill-tab"
            :class="{ active: activeTab === 'priority' }"
            @click="activeTab = 'priority'"
          >
            優先級管理
          </button>
          <button
            type="button"
            class="mt-pill-tab"
            :class="{ active: activeTab === 'category' }"
            @click="activeTab = 'category'"
          >
            報修大類管理
          </button>
          <button
            type="button"
            class="mt-pill-tab"
            :class="{ active: activeTab === 'subCategory' }"
            @click="activeTab = 'subCategory'"
          >
            報修細項管理
          </button>
        </div>
      </div>

      <!-- 手機版下拉式選單（對應第一份的 mt-mobile-filter 概念） -->
      <div class="mt-mobile-filter">
        <select
          v-model="activeTab"
          class="mt-mobile-control"
          aria-label="選擇設定分頁"
        >
          <option value="target">報修設備管理</option>
          <option value="priority">優先級管理</option>
          <option value="category">報修大類管理</option>
          <option value="subCategory">報修細項管理</option>
        </select>
      </div>

      <!-- 內容區塊（加上 <transition> 實現絲滑切換動畫） -->
      <div class="mt-setting-content">
        <transition name="fade" mode="out-in">
          <div v-if="activeTab === 'target'" key="target">
            <SettingTarget />
          </div>
          <div v-else-if="activeTab === 'priority'" key="priority">
            <SettingPriority />
          </div>
          <div v-else-if="activeTab === 'category'" key="category">
            <SettingCategory />
          </div>
          <div v-else-if="activeTab === 'subCategory'" key="subCategory">
            <SettingSubCategory />
          </div>
        </transition>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import SettingCategory from "@/views/SettingCategory.vue";
import SettingSubCategory from "@/views/SettingSubCategory.vue";
import SettingPriority from "@/views/SettingPriority.vue";
import SettingTarget from "@/views/SettingTarget.vue";

// 預設開啟報修設備管理
const activeTab = ref("target");
</script>

<style scoped>
.mt-page {
  max-width: 1240px;
  margin: 0 auto;
}

/* ---------------------------------------------------------------------- */
/* 頁首 */
/* ---------------------------------------------------------------------- */
.mt-page-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 22px;
  flex-wrap: wrap;
}
.mt-eyebrow {
  display: block;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.08em;
  color: var(--color-primary);
  text-transform: uppercase;
  margin-bottom: 6px;
}
.mt-title {
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 24px;
  color: var(--color-ink);
  margin: 0;
}
.mt-subtitle {
  margin: 6px 0 0;
  color: var(--color-text-muted);
  font-size: 13.5px;
}

/* ---------------------------------------------------------------------- */
/* 卡片容器 */
/* ---------------------------------------------------------------------- */
.mt-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: 0 1px 2px rgba(20, 33, 61, 0.05), 0 2px 8px rgba(20, 33, 61, 0.06);
  padding: 20px 22px;
}

/* ---------------------------------------------------------------------- */
/* 工具列與頁籤 (Pill Tabs) */
/* ---------------------------------------------------------------------- */
.mt-toolbar {
  display: flex;
  align-items: center;
  gap: 24px;
  flex-wrap: wrap;
  margin-bottom: 20px;
}
.mt-pill-tabs {
  display: flex;
  align-items: stretch;
  gap: 4px;
  min-height: 40px;
  padding: 0;
  background: transparent;
  border-bottom: 1px solid var(--color-border);
  border-radius: 0;
  flex-wrap: wrap;
  width: 100%;
}
.mt-pill-tab {
  display: inline-flex;
  align-items: center;
  min-height: 40px;
  padding: 0 16px;
  border-radius: 0;
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-muted);
  cursor: pointer;
  border: none;
  background: transparent;
  font-family: var(--font-body);
  transition: color 0.15s;
}
.mt-pill-tab:hover {
  color: var(--color-primary);
}
.mt-pill-tab.active {
  background: transparent;
  color: var(--color-primary);
  box-shadow: inset 0 -2px 0 var(--color-primary);
}

.mt-mobile-filter {
  display: none;
}

.mt-setting-content {
  margin-top: 10px;
}

/* ---------------------------------------------------------------------- */
/* 分頁切換絲滑動畫 (Fade & Slide) */
/* ---------------------------------------------------------------------- */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.fade-enter-from {
  opacity: 0;
  transform: translateY(6px);
}

.fade-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}

/* ---------------------------------------------------------------------- */
/* 響應式調整 (對應第一份的斷點設計) */
/* ---------------------------------------------------------------------- */
@media (max-width: 850px) {
  .mt-page-header {
    align-items: center;
    margin-bottom: 16px;
  }
  .mt-card {
    padding: 14px 12px;
  }
  .mt-toolbar {
    display: none;
  }
  .mt-mobile-filter {
    display: grid;
    gap: 10px;
    margin-bottom: 16px;
  }
  .mt-mobile-control {
    box-sizing: border-box;
    width: 100%;
    min-height: 44px;
    padding: 0 14px;
    border: 1px solid var(--color-border);
    border-radius: var(--radius-sm);
    background: #fff;
    color: var(--color-text);
    font-family: var(--font-body);
    font-size: 14px;
  }
  .mt-mobile-control:focus {
    border-color: var(--color-primary);
    outline: none;
    box-shadow: 0 0 0 3px var(--color-primary-soft);
  }
}
</style>