<template>
  <div class="settings-container">
    <h2 class="page-title">報修大類管理</h2>

    <div class="action-bar">
      <div class="search-box">
        <input
          v-model="keyword"
          placeholder="請輸入大類名稱搜尋..."
          @keyup.enter="fetchData"
        />
        <button class="btn-search" @click="fetchData">搜尋</button>
      </div>
      <button class="btn-create" @click="openCreateModal">
        + 新增報修大類
      </button>
    </div>

    <div class="table-card">
      <table class="modern-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>大類名稱</th>
            <th>預設優先級</th>
            <th>狀態</th>
            <th>建立時間</th>
            <th>更新時間</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in tableData" :key="item.repairCategoriesId">
            <td>{{ item.repairCategoriesId }}</td>
            <td>
              <span class="badge-name">{{ item.name }}</span>
            </td>
            <td>{{ item.defaultPriorityName || "無" }}</td>
            <td>
              <label class="switch">
                <input
                  type="checkbox"
                  :checked="item.status"
                  @change="handleStatusChange(item)"
                />
                <span class="slider"></span>
              </label>
            </td>
            <td class="time-text">{{ item.createdTime || "-" }}</td>
            <td class="time-text">{{ item.updatedTime || "-" }}</td>
            <td>
              <button class="btn-edit" @click="openEditModal(item)">
                編輯
              </button>
            </td>
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
          <h3>{{ isEditMode ? "編輯報修大類" : "新增報修大類" }}</h3>
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
            <label>預設優先級 ID：</label>
            <input
              v-model.number="form.defaultPriorityId"
              type="number"
              required
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
import {
  getRepairCategories,
  createRepairCategory,
  updateRepairCategory,
  updateRepairCategoryStatus,
} from "@/api/category.js";

const keyword = ref("");
const tableData = ref([]);
const isModalOpen = ref(false);
const isEditMode = ref(false);
const currentEditId = ref(null);
const form = ref({ name: "", defaultPriorityId: 1 });

const fetchData = async () => {
  try {
    tableData.value = await getRepairCategories(keyword.value);
  } catch (error) {
    tableData.value = [];
  }
};

const handleStatusChange = async (item) => {
  const newStatus = !item.status;
  try {
    await updateRepairCategoryStatus(item.repairCategoriesId, newStatus);
    item.status = newStatus;
  } catch (error) {
    alert("狀態更新失敗");
  }
};

const openCreateModal = () => {
  isEditMode.value = false;
  currentEditId.value = null;
  form.value = { name: "", defaultPriorityId: 1 };
  isModalOpen.value = true;
};

const openEditModal = (item) => {
  isEditMode.value = true;
  currentEditId.value = item.repairCategoriesId;
  form.value = { name: item.name, defaultPriorityId: item.defaultPriorityId };
  isModalOpen.value = true;
};

const handleSubmit = async () => {
  try {
    if (isEditMode.value) {
      await updateRepairCategory(currentEditId.value, form.value);
      alert("更新成功！");
    } else {
      await createRepairCategory(form.value);
      alert("新增成功！");
    }
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