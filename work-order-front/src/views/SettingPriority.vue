<template>
  <div class="settings-container">
    <h2 class="page-title">優先級管理</h2>

    <div class="action-bar">
      <div class="search-box">
        <input
          v-model="keyword"
          placeholder="請輸入優先級名稱搜尋..."
          @keyup.enter="fetchData"
        />
        <button class="btn-search" @click="fetchData">搜尋</button>
      </div>
      <button class="btn-create" @click="openCreateModal">+ 新增優先級</button>
    </div>

    <div class="table-card">
      <table class="modern-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>優先級別</th>
            <th>處理時數</th>
            <th>狀態</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in tableData" :key="item.prioritiesId">
            <td>{{ item.prioritiesId }}</td>
            <td>
              <span class="badge-name">{{ item.name }}</span>
            </td>
            <td>{{ item.hours }} 小時</td>
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
            <td>
              <button class="btn-edit" @click="openEditModal(item)">
                編輯
              </button>
            </td>
          </tr>
          <tr v-if="tableData.length === 0">
            <td colspan="5" class="empty-row">目前沒有符合的資料</td>
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
          <h3>{{ isEditMode ? "編輯優先級" : "新增優先級" }}</h3>
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
            <label>處理時數 (Hours)：</label>
            <input v-model.number="form.hours" type="number" required />
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
  getPriorities,
  createPriority,
  updatePriority,
  updatePriorityStatus,
} from "@/api/priority.js";

const keyword = ref("");
const tableData = ref([]);
const isModalOpen = ref(false);
const isEditMode = ref(false);
const currentEditId = ref(null);
const form = ref({ name: "", hours: 8 });

const fetchData = async () => {
  try {
    const res = await getPriorities(keyword.value);
    tableData.value = Array.isArray(res) ? res : (res.data || []);
  } catch (error) {
    tableData.value = [];
  }
};

const handleStatusChange = async (item) => {
  const newStatus = !item.status;
  try {
    await updatePriorityStatus(item.prioritiesId, newStatus);
    item.status = newStatus;
  } catch (error) {
    alert("狀態更新失敗");
  }
};

const openCreateModal = () => {
  isEditMode.value = false;
  currentEditId.value = null;
  form.value = { name: "", hours: 8 };
  isModalOpen.value = true;
};

const openEditModal = (item) => {
  isEditMode.value = true;
  currentEditId.value = item.prioritiesId;
  form.value = { name: item.name, hours: item.hours };
  isModalOpen.value = true;
};

const handleSubmit = async () => {
  try {
    if (isEditMode.value) {
      await updatePriority(currentEditId.value, form.value);
      alert("更新成功！");
    } else {
      await createPriority(form.value);
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

<style scoped>
.settings-container { padding: 24px; background-color: #f8fafc; min-height: calc(100vh - 64px); }
.page-title { font-size: 22px; font-weight: 700; color: #1e293b; margin-bottom: 20px; }
.action-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; flex-wrap: wrap; gap: 12px; }
.search-box { display: flex; align-items: center; background: #ffffff; border: 1px solid #cbd5e1; border-radius: 8px; overflow: hidden; }
.search-box input { border: none; outline: none; padding: 8px 14px; font-size: 14px; width: 260px; color: #334155; }
.btn-search { background-color: #2563eb; color: #ffffff; border: 1px solid #2563eb; padding: 8px 16px; font-size: 14px; border-radius: 0 8px 8px 0; cursor: pointer; }
.btn-create { background-color: #2563eb; color: #ffffff; border: none; padding: 9px 16px; font-size: 14px; border-radius: 8px; cursor: pointer; }
.btn-edit { background-color: #ffffff; color: #475569; border: 1px solid #cbd5e1; padding: 5px 12px; font-size: 13px; border-radius: 6px; cursor: pointer; }
.table-card { background: #ffffff; border: 1px solid #e2e8f0; border-radius: 10px; overflow: hidden; }
.modern-table { width: 100%; border-collapse: collapse; text-align: left; font-size: 14px; }
.modern-table th { background-color: #f8fafc; color: #475569; font-weight: 600; padding: 12px 16px; border-bottom: 1px solid #e2e8f0; }
.modern-table td { padding: 12px 16px; color: #334155; border-bottom: 1px solid #f1f5f9; }
.modern-table tbody tr:hover { background-color: #f8fafc; }
.empty-row { text-align: center; color: #94a3b8; padding: 32px !important; }
.badge-name { font-weight: 600; color: #1e293b; }
.badge-code { background-color: #f1f5f9; color: #475569; padding: 2px 6px; border-radius: 4px; font-family: monospace; font-size: 13px; }
.time-text { color: #64748b; font-size: 13px; }

/* Switch 開關 */
.switch { position: relative; display: inline-block; width: 40px; height: 22px; }
.switch input { opacity: 0; width: 0; height: 0; }
.slider { position: absolute; cursor: pointer; top: 0; left: 0; right: 0; bottom: 0; background-color: #cbd5e1; transition: 0.3s; border-radius: 22px; }
.slider:before { position: absolute; content: ""; height: 16px; width: 16px; left: 3px; bottom: 3px; background-color: white; transition: 0.3s; border-radius: 50%; }
input:checked + .slider { background-color: #2563eb; }
input:checked + .slider:before { transform: translateX(18px); }

/* Modal 視窗 */
.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background-color: rgba(15, 23, 42, 0.5); display: flex; justify-content: center; align-items: center; z-index: 1000; }
.modal-card { background: #ffffff; width: 100%; max-width: 480px; border-radius: 12px; box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1); overflow: hidden; }
.modal-header { display: flex; justify-content: space-between; align-items: center; padding: 16px 20px; border-bottom: 1px solid #e2e8f0; }
.modal-header h3 { font-size: 16px; font-weight: 600; color: #1e293b; margin: 0; }
.close-btn { background: none; border: none; font-size: 20px; color: #64748b; cursor: pointer; }
.modal-form { padding: 20px; }
.form-group { margin-bottom: 16px; }
.form-group label { display: block; font-size: 13px; font-weight: 500; color: #475569; margin-bottom: 6px; }
.form-group input, .form-select { width: 100%; padding: 9px 12px; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 14px; box-sizing: border-box; outline: none; background: #fff; }
.modal-footer { display: flex; justify-content: flex-end; gap: 10px; margin-top: 24px; }
.btn-cancel { background-color: #ffffff; color: #475569; border: 1px solid #cbd5e1; padding: 8px 16px; border-radius: 6px; font-size: 14px; cursor: pointer; }
.btn-submit { background-color: #2563eb; color: #ffffff; border: none; padding: 8px 16px; border-radius: 6px; font-size: 14px; font-weight: 500; cursor: pointer; }
.btn-submit:hover { background-color: #1d4ed8; }
</style>