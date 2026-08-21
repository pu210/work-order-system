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
            <th>優先級別</th>
            <th>狀態</th>
            <th>建立時間</th>
            <th>更新時間</th>
            <th>操作</th> <!-- 補回操作欄位 -->
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in tableData" :key="item.subCategoriesId">
            <td>{{ item.subCategoriesId }}</td>
            <td>
              <span class="badge-name">{{ item.name }}</span>
            </td>
            <td>{{ item.categoryName || "無" }} </td>
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
            <td class="time-text">{{ item.createdTime || "-" }}</td>
            <td class="time-text">{{ item.updatedTime || "-" }}</td>
            <td>
              <!-- 補回編輯按鈕 -->
              <button class="btn-edit" @click="openEditModal(item)">
                編輯
              </button>
            </td>
          </tr>
          <tr v-if="tableData.length === 0">
            <td colspan="8" class="empty-row">目前沒有符合的資料</td>
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
          <h3>{{ isEditMode ? "編輯報修子類" : "新增報修子類" }}</h3>
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
            <label>所屬大類：</label>
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
import { ref, onMounted } from "vue";
import { 
  getSubCategories, 
  createSubCategory, 
  updateSubCategory, /* 確保 API 有匯入更新方法 */
  getRepairCategories,
  updateSubCategoryStatus 
} from "@/api/category.js";
import { getPriorities } from "@/api/priority.js";

const keyword = ref("");
const tableData = ref([]);
const categoryList = ref([]); 
const priorityList = ref([]); 

const isModalOpen = ref(false);
const isEditMode = ref(false); // 判斷是否為編輯模式
const currentEditId = ref(null); // 記錄當前編輯的 ID
const form = ref({ name: "", categoryId: "", overridePriorityId: null });

const fetchData = async () => {
  try {
    const res = await getSubCategories(keyword.value); 
    tableData.value = Array.isArray(res) ? res : (res.data || []);
  } catch (error) {
    tableData.value = [];
  }
};

// 畫面動態對應實際生效的優先級文字
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
  } catch (error) {
    alert("狀態更新失敗");
  }
};

const fetchCategories = async () => {
  try {
    const res = await getRepairCategories();
    categoryList.value = Array.isArray(res) ? res : (res.data || []);
  } catch (error) {
    categoryList.value = [];
  }
};

const fetchPriorities = async () => {
  try {
    const res = await getPriorities();
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

// 開啟編輯視窗
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
      alert("更新成功！");
    } else {
      await createSubCategory(form.value);
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
  fetchCategories();
  fetchPriorities();
});
</script>

<style scoped>
.settings-container { padding: 24px; background-color: #f8fafc; min-height: calc(100vh - 64px); max-width: 1200px; margin: 0 auto; box-sizing: border-box; }
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