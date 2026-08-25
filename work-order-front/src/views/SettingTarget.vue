<template>
  <div class="mt-page">
    <h2 class="page-title">報修設備目標管理</h2>

    <div class="action-bar">
      <div class="search-box">
        <input
          v-model="keyword"
          placeholder="請輸入設備名稱、編號或型號搜尋..."
          @keyup.enter="fetchData"
        />
        <button class="btn-search" @click="fetchData">搜尋</button>
      </div>
      <button class="btn-create" @click="openCreateModal">
        + 新增報修設備
      </button>
    </div>

    <div class="table-card">
      <table class="modern-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>設備名稱</th>
            <th>設備編號</th>
            <th>型號</th>
            <th>狀態</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in tableData" :key="item.targetId">
            <td>{{ item.targetId }}</td>
            <td>
              <span class="badge-name">{{ item.name }}</span>
            </td>
            <td>
              <span class="badge-code">{{ item.targetNo }}</span>
            </td>
            <td>{{ item.model || "-" }}</td>
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

    <!-- 編輯/新增 表單彈跳視窗 -->
    <div
      v-if="isModalOpen"
      class="modal-overlay"
      @click.self="isModalOpen = false"
    >
      <div class="modal-card">
        <div class="modal-header">
          <h3>{{ isEditMode ? "編輯報修設備" : "新增報修設備" }}</h3>
          <button class="close-btn" @click="isModalOpen = false">
            &times;
          </button>
        </div>
        <form @submit.prevent="handleSubmit" class="modal-form">
          <div class="form-group">
            <label>設備名稱：</label>
            <input v-model="form.name" required placeholder="請輸入設備名稱 (例如: 蘋果手機)" />
          </div>

          <!-- 只有在「編輯」模式下才顯示設備編號，且設為唯讀 (disabled) -->
          <div class="form-group" v-if="isEditMode">
            <label>設備編號 (系統自動產生)：</label>
            <input v-model="form.targetNo" disabled class="disabled-input" />
          </div>

          <div class="form-group">
            <label>型號：</label>
            <input v-model="form.model" placeholder="請輸入型號 (例如: 17 pro)" />
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

    <!-- 🌟 精美成功提示彈窗 (獨立提升 z-index 確保在最上層) -->
    <div v-if="successModalOpen" class="modal-overlay success-overlay">
      <div class="modal-card success-card">
        <div class="success-icon-wrapper">
          <svg class="success-check-icon" viewBox="0 0 24 24" width="48" height="48">
            <circle cx="12" cy="12" r="11" fill="none" stroke="#22c55e" stroke-width="2"/>
            <path fill="none" stroke="#22c55e" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" d="M7 13l3 3 7-7"/>
          </svg>
        </div>
        <h3 class="success-title">操作成功</h3>
        <p class="success-desc">{{ successMessage }}</p>
        <button class="btn-submit success-ok-btn" @click="successModalOpen = false">
          OK
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import Swal from "sweetalert2";
import {
  getRepairTargets,
  createRepairTarget,
  updateRepairTarget,
  updateRepairTargetStatus,
} from "@/api/repairTarget.js";

const keyword = ref("");
const tableData = ref([]);

const isModalOpen = ref(false);
const isEditMode = ref(false);
const currentEditId = ref(null);

const form = ref({ 
  name: "", 
  model: "" 
});

// 🌟 成功彈窗狀態變數
const successModalOpen = ref(false);
const successMessage = ref("");

const fetchData = async () => {
  try {
    const res = await getRepairTargets(keyword.value); 
    tableData.value = Array.isArray(res) ? res : (res.data || []);
  } catch (error) {
    console.error("載入設備資料失敗", error);
    tableData.value = [];
  }
};

const handleStatusChange = async (item) => {
  const newStatus = !item.status;
  try {
    await updateRepairTargetStatus(item.targetId, newStatus);
    item.status = newStatus;
  } catch (error) {
    item.status = !newStatus; 
    alert("狀態更新失敗");
  }
};

const openCreateModal = () => {
  isEditMode.value = false;
  currentEditId.value = null;
  form.value = { name: "", model: "" };
  isModalOpen.value = true;
};

const openEditModal = (item) => {
  isEditMode.value = true;
  currentEditId.value = item.targetId;
  form.value = { 
    name: item.name, 
    targetNo: item.targetNo, 
    model: item.model 
  };
  isModalOpen.value = true;
};

const handleSubmit = async () => {
  try {
    if (isEditMode.value) {
      await updateRepairTarget(currentEditId.value, form.value);
    } else {
      await createRepairTarget(form.value);
    }
    
    isModalOpen.value = false; // 關閉表單彈窗
    
    // 🌟 SweetAlert2 成功提示
    await Swal.fire({
      icon: "success",
      title: isEditMode.value ? "報修設備更新成功！" : "報修設備新增成功！",
      showConfirmButton: false,
      timer: 1500,
    });

    fetchData(); // 重新載入列表
  } catch (error) {
    console.error(error);
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

/* Switch 開關 */
.switch { position: relative; display: inline-block; width: 40px; height: 22px; }
.switch input { opacity: 0; width: 0; height: 0; }
.slider { position: absolute; cursor: pointer; top: 0; left: 0; right: 0; bottom: 0; background-color: #cbd5e1; transition: 0.3s; border-radius: 22px; }
.slider:before { position: absolute; content: ""; height: 16px; width: 16px; left: 3px; bottom: 3px; background-color: white; transition: 0.3s; border-radius: 50%; }
input:checked + .slider { background-color: #2563eb; }
input:checked + .slider:before { transform: translateX(18px); }

/* Modal 視窗 (含背景淡入動畫) */
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
.form-group input { width: 100%; padding: 9px 12px; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 14px; box-sizing: border-box; outline: none; background: #fff; }
.form-group input.disabled-input { background-color: #f1f5f9; color: #64748b; cursor: not-allowed; }
.modal-footer { display: flex; justify-content: flex-end; gap: 10px; margin-top: 24px; }
.btn-cancel { background-color: #ffffff; color: #475569; border: 1px solid #cbd5e1; padding: 8px 16px; border-radius: 6px; font-size: 14px; cursor: pointer; }
.btn-submit { background-color: #2563eb; color: #ffffff; border: none; padding: 8px 16px; border-radius: 6px; font-size: 14px; font-weight: 500; cursor: pointer; }
.btn-submit:hover { background-color: #1d4ed8; }

/* 🌟 精美成功彈窗專屬樣式與流暢動畫 */
.success-overlay {
  z-index: 2000;
}
.success-card {
  text-align: center;
  padding: 36px 28px;
  max-width: 360px;
}
.success-icon-wrapper {
  display: flex;
  justify-content: center;
  margin-bottom: 18px;
}

/* 綠色打勾圖示動態感 (放大彈出的感覺) */
.success-check-icon {
  animation: bounceIn 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275) forwards;
}

.success-title {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 8px;
}
.success-desc {
  font-size: 14px;
  color: #64748b;
  margin-bottom: 24px;
}
.success-ok-btn {
  width: 100%;
  padding: 10px 0;
  font-size: 15px;
  border-radius: 8px;
}

/* 🎬 定義動畫關鍵影格 */
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes scaleUp {
  from { opacity: 0; transform: scale(0.95) translateY(10px); }
  to { opacity: 1; transform: scale(1) translateY(0); }
}

@keyframes bounceIn {
  0% { transform: scale(0); opacity: 0; }
  60% { transform: scale(1.1); opacity: 1; }
  100% { transform: scale(1); opacity: 1; }
}
</style>