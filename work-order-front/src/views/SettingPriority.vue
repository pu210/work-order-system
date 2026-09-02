<template>
  <div class="mt-page">
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

    <!-- 🌟 加上轉場動畫的容器，以 currentPage 作為 key 觸發滑順切換 -->
    <transition name="fade" mode="out-in">
      <div class="table-card" :key="currentPage">
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
            <!-- 🌟 跨頁連續流水號公式 -->
            <tr v-for="(item, index) in tableData" :key="item.prioritiesId || index">
              <td>{{ (currentPage - 1) * pageSize + index + 1 }}</td>
              <td>
                <span class="badge-name">{{ item.name }}</span>
              </td>
              <td>
                <div class="time-cell">
                  <span class="time-num">{{ item.hours }}</span>
                  <span class="time-unit">小時</span>
                </div>
              </td>
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
    </transition>

    <!-- 🌟 分頁按鈕與資訊列 -->
    <div class="pagination-bar" v-if="totalPages > 1">
      <button 
        class="page-btn" 
        :disabled="currentPage === 1" 
        @click="changePage(currentPage - 1)"
      >
        上一頁
      </button>
      
      <span class="page-info">
        第 {{ currentPage }} 頁 / 共 {{ totalPages }} 頁 (總計 {{ totalItems }} 筆)
      </span>

      <button 
        class="page-btn" 
        :disabled="currentPage === totalPages" 
        @click="changePage(currentPage + 1)"
      >
        下一頁
      </button>
    </div>

    <!-- 編輯/新增 表單彈跳視窗 -->
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
            <label>名稱：<span style="color: red;">*</span></label>
            <input v-model="form.name" required placeholder="請輸入名稱" />
          </div>
          <div class="form-group">
            <label>處理時數 (Hours)：<span style="color: red;">*</span></label>
            <input 
              v-model.number="form.hours" 
              type="number" 
              min="1" 
              required 
              placeholder="請輸入處理時數" 
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
import { ref, computed, onMounted } from "vue";
import {
  getPriorities,
  createPriority,
  updatePriority,
  updatePriorityStatus,
} from "@/api/priority.js";
import Swal from "sweetalert2";

const keyword = ref("");
const allTableData = ref([]); // 🌟 儲存從後端抓回來的完整原始資料
const tableData = ref([]);    // 🌟 當前畫面實際顯示的 10 筆資料

// 🌟 分頁相關變數
const currentPage = ref(1);
const pageSize = ref(10);

const totalItems = computed(() => allTableData.value.length);

// 🌟 計算總頁數
const totalPages = computed(() => {
  return Math.ceil(totalItems.value / pageSize.value) || 1;
});

const isModalOpen = ref(false);
const isEditMode = ref(false);
const currentEditId = ref(null);
const form = ref({ name: "", hours: 8 });

// 🌟 1. 抓取全部資料並快取在前端
const fetchData = async () => {
  try {
    const res = await getPriorities(keyword.value);
    const rawData = Array.isArray(res) ? res : (res.data || []);
    
    allTableData.value = rawData;
    currentPage.value = 1; // 搜尋或重新載入時回到第一頁
    updatePageData();

  } catch (error) {
    allTableData.value = [];
    tableData.value = [];
  }
};

// 🌟 2. 純粹用記憶體切片產生當頁資料
const updatePageData = () => {
  const startIndex = (currentPage.value - 1) * pageSize.value;
  const endIndex = startIndex + pageSize.value;
  tableData.value = allTableData.value.slice(startIndex, endIndex);
};

// 🌟 3. 切換頁碼（不發送 API，達到秒切）
const changePage = (page) => {
  if (page < 1 || page > totalPages.value) return;
  currentPage.value = page;
  updatePageData();
};

const handleStatusChange = async (item) => {
  const newStatus = !item.status;
  try {
    await updatePriorityStatus(item.prioritiesId, newStatus);
    item.status = newStatus;
    Swal.fire({
      icon: "success",
      title: "狀態更新成功",
      toast: true,
      position: "top-end",
      showConfirmButton: false,
      timer: 1500,
    });
  } catch (error) {
    item.status = !newStatus;
    Swal.fire({
      icon: "error",
      title: "錯誤",
      text: "狀態更新失敗！",
    });
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
  if (form.value.hours <= 0) {
    Swal.fire({
      icon: "warning",
      title: "格式錯誤",
      text: "處理時數必須大於 0 小時！",
    });
    return;
  }

  try {
    if (isEditMode.value) {
      await updatePriority(currentEditId.value, form.value);
    } else {
      await createPriority(form.value);
    }
    
    isModalOpen.value = false;      
    
    await Swal.fire({
      icon: "success",
      title: isEditMode.value ? "優先級更新成功！" : "優先級新增成功！",
      showConfirmButton: false,
      timer: 1500,
    });

    fetchData();                    
  } catch (error) {
    Swal.fire({
      icon: "error",
      title: "操作失敗",
      text: "請稍後再試或檢查輸入內容。",
    });
  }
};

onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.mt-page {
  background-color: #ffffff;
  box-sizing: border-box;
}

.page-title {
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 24px;
  color: var(--color-ink);
  margin: 0 0 20px 0;
}

.action-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.search-box {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 180px;
}
.search-box input,
.form-group input,
.form-select {
  padding: 8px 12px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--color-border);
  font-size: 13.5px;
  font-family: var(--font-body);
  background: #fff;
  color: var(--color-text);
  outline: none;
  box-sizing: border-box;
  width: 100%;
}
.search-box input:focus,
.form-group input:focus,
.form-select:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--color-primary-soft);
}

/* 搜尋按鈕 */
.btn-search {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 13.5px;
  font-weight: 600;
  padding: 8px 16px;
  border-radius: 0 var(--radius-sm) var(--radius-sm) 0;
  border: 1px solid var(--color-primary);
  background: var(--color-primary);
  color: #fff;
  cursor: pointer;
  font-family: var(--font-body);
}
.btn-search:hover {
  background: var(--color-primary-dark);
}

/* 新增與一般動作按鈕 */
.btn-create, .btn-submit {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 13.5px;
  font-weight: 600;
  padding: 8px 16px;
  border-radius: var(--radius-sm);
  border: none;
  background: var(--color-primary);
  color: #fff;
  cursor: pointer;
  font-family: var(--font-body);
  transition: all 0.15s;
}
.btn-create:hover, .btn-submit:hover {
  background: var(--color-primary-dark);
}

.btn-edit, .btn-cancel, .page-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 12.5px;
  font-weight: 600;
  padding: 6px 14px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--color-border);
  background: #fff;
  color: var(--color-text-muted);
  cursor: pointer;
  font-family: var(--font-body);
}
.btn-edit:hover, .btn-cancel:hover, .page-btn:hover:not(:disabled) {
  border-color: var(--color-primary);
  color: var(--color-primary);
}
.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 表格卡片與外觀 */
.table-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: 0 1px 2px rgba(20, 33, 61, 0.05), 0 2px 8px rgba(20, 33, 61, 0.06);
  overflow: hidden;
}
.modern-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}
.modern-table th {
  text-align: left;
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: var(--color-text-faint);
  font-weight: 700;
  padding: 10px 14px;
  border-bottom: 1px solid var(--color-border);
  white-space: nowrap;
  background-color: transparent;
}
.modern-table td {
  padding: 13px 14px;
  border-bottom: 1px solid var(--color-border);
  vertical-align: middle;
  color: var(--color-text);
}
.modern-table tbody tr {
  transition: background 0.12s;
}
.modern-table tbody tr:hover {
  background: var(--color-bg);
}
.modern-table tbody tr:last-child td {
  border-bottom: none;
}
.empty-row {
  text-align: center;
  color: var(--color-text-muted);
  padding: 32px !important;
}

.badge-name {
  font-weight: 600;
  color: var(--color-ink);
}
.badge-code {
  background: #eef0f4;
  color: var(--color-text-muted);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: var(--font-mono, monospace);
  font-size: 13px;
}

/* 🌟 小時數對齊專用樣式 */
.time-cell {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.time-num {
  display: inline-block;
  width: 24px; /* 固定寬度，讓數字右側對齊 */
  text-align: right;
  font-variant-numeric: tabular-nums;
}
.time-unit {
  color: var(--color-text-muted);
  font-size: 13px;
}

/* 動畫 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}
.fade-enter-from {
  opacity: 0;
  transform: translateY(4px);
}
.fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

/* 分頁列 */
.pagination-bar {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 18px;
  gap: 10px;
}
.page-info {
  font-size: 12.5px;
  color: var(--color-text-muted);
}

/* Switch 開關 */
.switch {
  position: relative;
  display: inline-block;
  width: 40px;
  height: 22px;
}
.switch input { opacity: 0; width: 0; height: 0; }
.slider {
  position: absolute; cursor: pointer; top: 0; left: 0; right: 0; bottom: 0;
  background-color: #cbd5e1; transition: 0.3s; border-radius: 22px;
}
.slider:before {
  position: absolute; content: ""; height: 16px; width: 16px; left: 3px; bottom: 3px;
  background-color: white; transition: 0.3s; border-radius: 50%;
}
input:checked + .slider { background-color: var(--color-primary); }
input:checked + .slider:before { transform: translateX(18px); }

/* Modal 視窗 */
.modal-overlay {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background-color: rgba(15, 23, 42, 0.5);
  display: flex; justify-content: center; align-items: center;
  z-index: 1000;
  animation: fadeIn 0.25s ease-out forwards;
}
.modal-card {
  background: #ffffff; width: 100%; max-width: 480px;
  border-radius: var(--radius-lg);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  animation: scaleUp 0.25s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}
.modal-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 16px 20px; border-bottom: 1px solid var(--color-border);
}
.modal-header h3 {
  font-size: 16px; font-weight: 700; color: var(--color-ink); margin: 0;
  font-family: var(--font-display);
}
.close-btn {
  background: none; border: none; font-size: 20px; color: var(--color-text-muted); cursor: pointer;
}
.modal-form { padding: 20px; }
.form-group { margin-bottom: 16px; }
.form-group label {
  display: block; font-size: 13px; font-weight: 600; color: var(--color-text); margin-bottom: 6px;
}
.modal-footer {
  display: flex; justify-content: flex-end; gap: 10px; margin-top: 24px;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
@keyframes scaleUp {
  from { opacity: 0; transform: scale(0.95) translateY(10px); }
  to { opacity: 1; transform: scale(1) translateY(0); }
}

@media (max-width: 768px) {
  .action-bar {
    flex-direction: column;
    align-items: stretch;
  }
  .search-box { width: 100%; }
  .btn-create { width: 100%; text-align: center; }
  .table-card { overflow-x: auto; }
  .modern-table { min-width: 600px; }
  .modal-card { width: 90%; margin: 0 auto; }
}
</style>