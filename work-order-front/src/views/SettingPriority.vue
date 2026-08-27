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
.mt-page { padding: 24px; background-color: #f8fafc; min-height: calc(100vh - 64px); max-width: 1200px; margin: 0 auto; box-sizing: border-box; }
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
.badge-name { font-weight: 650; color: #1e293b; }
.badge-code { background-color: #f1f5f9; color: #475569; padding: 2px 6px; border-radius: 4px; font-family: monospace; font-size: 13px; }
.time-text { color: #64748b; font-size: 13px; }

/* 🌟 表格轉場動畫樣式 */
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

/* 分頁按鈕列樣式 */
.pagination-bar {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 20px;
  gap: 16px;
}
.page-btn {
  background-color: #ffffff;
  color: #334155;
  border: 1px solid #cbd5e1;
  padding: 6px 14px;
  font-size: 14px;
  border-radius: 6px;
  cursor: pointer;
}
.page-btn:disabled {
  background-color: #f1f5f9;
  color: #94a3b8;
  cursor: not-allowed;
}
.page-info {
  font-size: 14px;
  color: #475569;
}

/* Switch 開關 */
.switch { position: relative; display: inline-block; width: 40px; height: 22px; }
.switch input { opacity: 0; width: 0; height: 0; }
.slider { position: absolute; cursor: pointer; top: 0; left: 0; right: 0; bottom: 0; background-color: #cbd5e1; transition: 0.3s; border-radius: 22px; }
.slider:before { position: absolute; content: ""; height: 16px; width: 16px; left: 3px; bottom: 3px; background-color: white; transition: 0.3s; border-radius: 50%; }
input:checked + .slider { background-color: #2563eb; }
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
  border-radius: 12px; box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15); 
  overflow: hidden; 
  animation: scaleUp 0.25s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}
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

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
@keyframes scaleUp {
  from { opacity: 0; transform: scale(0.95) translateY(10px); }
  to { opacity: 1; transform: scale(1) translateY(0); }
}

/* ================================================================= */
/* RWD 響應式設計 (針對平板與手機螢幕: 768px 以下) */
/* ================================================================= */
@media (max-width: 768px) {
  /* 1. 調整頁面整體左右內距 */
  .mt-page {
    padding: 12px;
  }

  /* 2. 頂部操作列改為上下垂直排列 */
  .action-bar {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
  }

  /* 3. 搜尋框在手機上佔滿整行 */
  .search-box {
    width: 100%;
  }

  .search-box input {
    width: 100%;
    flex: 1;
  }

  /* 4. 新增按鈕改為寬版按鈕，方便手指點擊 */
  .btn-create {
    width: 100%;
    text-align: center;
    padding: 10px 16px;
  }

  /* 5. 表格卡片支援水平滾動，避免優先級或時數欄位被擠壓破版 */
  .table-card {
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }

  .modern-table {
    min-width: 500px; /* 確保表格在手機上有足夠的延展寬度 */
  }

  /* 6. 彈跳視窗 (Modal) 在手機上自動縮放兩側留白 */
  .modal-card {
    width: 90%;
    margin: 0 auto;
  }
}
</style>