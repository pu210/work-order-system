<template>
  <div class="settings-container">
    <h2 class="page-title">報修子類管理</h2>

    <div class="action-bar">
      <div class="search-box">
        <input
          v-model="keyword"
          placeholder="請輸入子類名稱搜尋..."
          @keyup.enter="fetchData"
        />
        <button class="btn-search" @click="fetchData">搜尋</button>
      </div>
      <button class="btn-create" @click="openCreateModal">
        + 新增報修子類
      </button>
    </div>

    <div class="table-card">
      <table class="modern-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>子類名稱</th>
            <th>所屬大類</th>
            <th>有效優先級</th>
            <th>狀態</th>
            <th>建立時間</th>
            <th>更新時間</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in tableData" :key="item.subCategoriesId">
            <td>{{ item.subCategoriesId }}</td>
            <td>
              <span class="badge-name">{{ item.name }}</span>
            </td>
            <td>{{ item.categoryName }} (ID: {{ item.categoryId }})</td>
            <td>{{ item.effectivePriorityName }}</td>
            <td>
              <span
                :class="
                  item.status ? 'status-tag active' : 'status-tag inactive'
                "
              >
                {{ item.status ? "啟用" : "停用" }}
              </span>
            </td>
            <td class="time-text">{{ item.createdTime || "-" }}</td>
            <td class="time-text">{{ item.updatedTime || "-" }}</td>
          </tr>
          <tr v-if="tableData.length === 0">
            <td colspan="7" class="empty-row">目前沒有符合的資料</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 彈跳視窗 -->
    <div
      v-if="isModalOpen"
      class="modal-overlay"
      @click.self="isModalOpen = false"
    >
      <div class="modal-card">
        <div class="modal-header">
          <h3>新增報修子類</h3>
          <button class="close-btn" @click="isModalOpen = false">
            &times;
          </button>
        </div>
        <form @submit.prevent="handleSubmit" class="modal-form">
          <div class="form-group">
            <label>名稱：</label>
            <input v-model="form.name" required placeholder="請輸入名稱" />
          </div>
          <div class="form-group">
            <label>所屬大類 ID：</label>
            <input v-model.number="form.categoryId" type="number" required />
          </div>
          <div class="form-group">
            <label>覆寫優先級 ID (選填)：</label>
            <input
              v-model.number="form.overridePriorityId"
              type="number"
              placeholder="無"
            />
          </div>
          <div class="modal-footer">
            <button
              type="button"
              class="btn-cancel"
              @click="isModalOpen = false"
            >
              取消
            </button>
            <button type="submit" class="btn-submit">確認儲存</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { getRepairCategories, createSubCategory } from "@/api/category.js";

const keyword = ref("");
const tableData = ref([]);
const isModalOpen = ref(false);
const form = ref({ name: "", categoryId: 1, overridePriorityId: null });

const fetchData = async () => {
  try {
    const res = await getRepairCategories(keyword.value); 
    tableData.value = Array.isArray(res) ? res : [];
  } catch (error) {
    tableData.value = [];
  }
};

const openCreateModal = () => {
  form.value = { name: "", categoryId: 1, overridePriorityId: null };
  isModalOpen.value = true;
};

const handleSubmit = async () => {
  try {
    await createSubCategory(form.value);
    alert("新增成功！");
    isModalOpen.value = false;
    fetchData();
  } catch (error) {
    alert("操作失敗");
  }
};

onMounted(() => {
  fetchData();
});
</script>