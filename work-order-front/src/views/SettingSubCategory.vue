<template>
  <div class="mt-page">
    <h2 class="page-title">報修細項管理</h2>

    <div class="action-bar">
      <div class="search-box">
        <input
          v-model="keyword"
          placeholder="請輸入細項名稱搜尋..."
          @keyup.enter="fetchData"
        />
        <button class="btn-search" @click="fetchData">搜尋</button>
      </div>
      <button class="btn-create" @click="openCreateModal">
        + 新增報修細項
      </button>
    </div>

    <!-- 🌟 加上轉場動畫的容器 -->
    <transition name="fade" mode="out-in">
      <div class="table-card" :key="currentPage">
        <table class="modern-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>細項名稱</th>
              <th>所屬大類</th>
              <th>優先級別</th>
              <th>狀態</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(item, index) in tableData" :key="item.subCategoriesId || index">
              <td>{{ (currentPage - 1) * pageSize + index + 1 }}</td>
              <td>
                <span class="badge-name">{{ item.name }}</span>
              </td>
              <td>{{ item.categoryName || "無" }}</td>
              <td>{{ getEffectivePriorityText(item) }}</td>
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
              <td colspan="6" class="empty-row">目前沒有符合的資料</td>
            </tr>
          </tbody>
        </table>
      </div>
    </transition>

    <!-- 分頁按鈕與資訊列 -->
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
          <h3>{{ isEditMode ? "編輯報修細項" : "新增報修細項" }}</h3>
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
            <label>所屬大類：<span style="color: red;">*</span></label>
            <select v-model.number="form.categoryId" required class="form-select">
              <option disabled value="">請選擇所屬大類</option>
              <option v-for="cat in categoryList" :key="cat.repairCategoriesId" :value="cat.repairCategoriesId">
                {{ cat.name }}
              </option>
            </select>
          </div>

          <div class="form-group">
            <label>覆寫優先級 (選填)：</label>
            <select v-model.number="form.overridePriorityId" class="form-select">
              <option :value="null">-- 不覆寫（使用大類預設） --</option>
              <option v-for="p in priorityList" :key="p.prioritiesId" :value="p.prioritiesId">
                {{ p.name }} ({{ p.hours }}小時)
              </option>
            </select>
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
  getSubCategories, 
  createSubCategory, 
  updateSubCategory, 
  getActiveRepairCategories,
  updateSubCategoryStatus 
} from "@/api/category.js";
import { getActivePriorities } from "@/api/priority.js";
import Swal from "sweetalert2";

const keyword = ref("");
const allTableData = ref([]); // 🌟 儲存從後端抓回來的完整原始資料
const tableData = ref([]);    // 🌟 當前畫面實際顯示的 10 筆資料
const categoryList = ref([]); 
const priorityList = ref([]); 

const currentPage = ref(1);
const pageSize = ref(10);

const totalItems = computed(() => allTableData.value.length);
const totalPages = computed(() => {
  return Math.ceil(totalItems.value / pageSize.value) || 1;
});

const isModalOpen = ref(false);
const isEditMode = ref(false); 
const currentEditId = ref(null); 
const form = ref({ name: "", categoryId: "", overridePriorityId: null });

// 🌟 1. 抓取全部資料並存入快取
const fetchData = async () => {
  try {
    const res = await getSubCategories(keyword.value); 
    allTableData.value = Array.isArray(res) ? res : (res.data || []);
    
    // 重新搜尋時，強制回到第一頁
    currentPage.value = 1;
    updatePageData();
  } catch (error) {
    allTableData.value = [];
    tableData.value = [];
  }
};

// 🌟 2. 純粹用記憶體切片，不發送 API，實現秒切
const updatePageData = () => {
  const startIndex = (currentPage.value - 1) * pageSize.value;
  const endIndex = startIndex + pageSize.value;
  tableData.value = allTableData.value.slice(startIndex, endIndex);
};

// 🌟 3. 換頁時直接切換，毫無延遲
const changePage = (page) => {
  if (page < 1 || page > totalPages.value) return;
  currentPage.value = page;
  updatePageData(); // 瞬間完成，不需要 await API
};

const getEffectivePriorityText = (item) => {
  if (item.overridePriorityId) {
    const foundPriority = priorityList.value.find(p => p.prioritiesId === item.overridePriorityId);
    return foundPriority ? `${foundPriority.name} ` : "自訂優先級";
  }

  const foundCategory = categoryList.value.find(c => c.repairCategoriesId === item.categoryId);
  if (foundCategory && foundCategory.defaultPriorityId) {
    const defaultPriority = priorityList.value.find(p => p.prioritiesId === foundCategory.defaultPriorityId);
    return defaultPriority ? `${defaultPriority.name} (大類預設)` : "大類預設";
  }

  return item.effectivePriorityName || "無";
};

const handleStatusChange = async (item) => {
  const newStatus = !item.status;
  try {
    await updateSubCategoryStatus(item.subCategoriesId, newStatus);
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

const fetchCategories = async () => {
  try {
    const res = await getActiveRepairCategories(); 
    categoryList.value = Array.isArray(res) ? res : (res.data || []);
  } catch (error) {
    categoryList.value = [];
  }
};

const fetchPriorities = async () => {
  try {
    const res = await getActivePriorities(); 
    priorityList.value = Array.isArray(res) ? res : (res.data || []);
  } catch (error) {
    priorityList.value = [];
  }
};

const openCreateModal = () => {
  isEditMode.value = false;
  currentEditId.value = null;
  form.value = { name: "", categoryId: "", overridePriorityId: null };
  isModalOpen.value = true;
};

const openEditModal = (item) => {
  isEditMode.value = true;
  currentEditId.value = item.subCategoriesId;
  form.value = { 
    name: item.name, 
    categoryId: item.categoryId, 
    overridePriorityId: item.overridePriorityId ?? null 
  };
  isModalOpen.value = true;
};

const handleSubmit = async () => {
  try {
    if (isEditMode.value) {
      await updateSubCategory(currentEditId.value, form.value);
    } else {
      await createSubCategory(form.value);
    }
    
    isModalOpen.value = false;      
    
    await Swal.fire({
      icon: "success",
      title: isEditMode.value ? "報修細項更新成功！" : "報修細項新增成功！",
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
  fetchCategories();
  fetchPriorities();
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

/* 🌟 表格轉場動畫樣式 */
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

  /* 5. 表格卡片支援水平滾動，避免細項名稱、所屬大類與優先級別欄位被擠壓破版 */
  .table-card {
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }

  .modern-table {
    min-width: 650px; /* 確保表格在手機上有足夠的延展寬度來容納所有欄位 */
  }

  /* 6. 彈跳視窗 (Modal) 在手機上自動適應寬度並保持適當留白 */
  .modal-card {
    width: 90%;
    margin: 0 auto;
  }
}

</style>