<template>
  <div>
    <h2>系統基礎設定</h2>

    <!-- 頁籤切換按鈕 -->
    <div>
      <button @click="activeTab = 'category'">
        報修大類管理
      </button>
      <button @click="activeTab = 'subCategory'">
        報修子類管理
      </button>
      <button @click="activeTab = 'priority'">
        優先級管理
      </button>
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
  </div>
</template>

<script setup>
import { ref } from "vue";
import SettingCategory from "@/views/SettingCategory.vue";
import SettingSubCategory from "@/views/SettingSubCategory.vue";
import SettingPriority from "@/views/SettingPriority.vue";

const activeTab = ref("category");
</script>