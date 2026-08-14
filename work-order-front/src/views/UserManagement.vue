<!-- <template>
  <div>
    <h3 class="fw-bold mb-3">人員管理</h3>
    <div class="card p-4 shadow-sm border-0">系統使用者與權限設定</div>
  </div>
</template>

<script setup>
	
</script>
    
<style>
    
</style> -->

<!-- src/views/Users.vue -->
<template>
  <div class="users-page">
    <!-- 頁面頂部：標題與操作按鈕 -->
    <div
      class="d-flex flex-column flex-sm-row justify-content-between align-items-sm-center mb-4 gap-3"
    >
      <div>
        <h4 class="fw-bold text-slate-800 mb-1">帳號管理</h4>
        <p class="text-muted extra-small mb-0">管理系統使用者帳號與權限設定</p>
      </div>
      <!-- <button class="btn btn-primary d-flex align-items-center gap-2 px-3 py-2 rounded-3 shadow-2xs extra-small fw-semibold">
        <i class="bi bi-person-plus-fill fs-6"></i>
        <span>新增使用者</span>
      </button> -->
      <router-link
        :to="{ name: 'user-create' }"
        class="btn btn-primary d-flex align-items-center gap-2 px-3 py-2 rounded-3 shadow-2xs extra-small fw-semibold text-decoration-none"
      >
        <i class="bi bi-person-plus-fill fs-6"></i>
        <span>新增使用者</span>
      </router-link>
    </div>

    <!-- 主要表格卡片區 -->
    <div class="card border-0 rounded-4 shadow-sm bg-white overflow-hidden">
      <!-- 頂部搜尋框 -->
      <div class="p-3 border-bottom bg-light-subtle">
        <div class="input-group search-input-group" style="max-width: 280px">
          <span class="input-group-text bg-white border-end-0 text-muted ps-3">
            <i class="bi bi-search extra-small"></i>
          </span>
          <input
            type="text"
            v-model="searchQuery"
            class="form-control border-start-0 extra-small ps-1"
            placeholder="搜尋姓名或信箱..."
          />
        </div>
      </div>

      <!-- 表格內容 -->
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="table-light extra-small text-secondary">
            <tr>
              <th class="ps-4 py-3" style="width: 20%">姓名</th>
              <th class="py-3" style="width: 30%">電子郵件信箱</th>
              <th class="py-3" style="width: 15%">狀態</th>
              <th class="py-3" style="width: 15%">角色</th>
              <!-- 🎯 修正重點：操作標題與底下欄位統一對齊風格 -->
              <th class="pe-4 py-3 text-center" style="width: 20%">操作</th>
            </tr>
          </thead>
          <tbody class="extra-small text-dark">
            <tr
              v-for="user in users"
              :key="user.id"
              :class="{
                'opacity-50 bg-light-subtle': user.status === 'disabled',
              }"
            >
              <!-- 1. 姓名 (🎯 移除小圈圈，回歸乾淨純文字) -->
              <td class="ps-4 py-3 fw-semibold text-dark">
                {{ user.name }}
              </td>

              <!-- 2. 電子郵件 (藍字比照原草圖) -->
              <td class="text-primary-emphasis fw-normal">{{ user.email }}</td>

              <!-- 3. 狀態標籤 -->
              <td>
                <span
                  v-if="user.status === 'active'"
                  class="badge bg-success-subtle text-success border border-success-subtle px-2.5 py-1 rounded-pill"
                >
                  使用中
                </span>
                <span
                  v-else
                  class="badge bg-secondary-subtle text-secondary border border-secondary-subtle px-2.5 py-1 rounded-pill"
                >
                  已停用
                </span>
              </td>

              <!-- 4. 角色 Tag -->
              <td>
                <span
                  class="badge bg-light text-secondary border rounded-pill px-3 py-1 fw-normal"
                >
                  {{ user.role }}
                </span>
              </td>

              <!-- 5. 操作 (🎯 修正對齊與間距問題) -->
              <td class="pe-4 py-3 text-center">
                <div
                  class="d-inline-flex align-items-center justify-content-center gap-2"
                >
                  <!-- 編輯按鈕 -->
                  <!-- <button type="button" class="btn btn-sm btn-outline-secondary d-flex align-items-center gap-1.5 px-2.5 py-1 rounded-2">
                    <i class="bi bi-pencil-square"></i>
                    <span>編輯</span>
                  </button> -->
                  <router-link
                    :to="{
                      name: 'user-edit',
                      params: { id: user.id },
                    }"
                    class="btn btn-sm btn-outline-secondary d-flex align-items-center gap-1.5 px-2.5 py-1 rounded-2"
                  >
                    <i class="bi bi-pencil-square"></i>
                    <span>編輯</span>
                  </router-link>

                  <!-- 停用 / 啟用按鈕 -->
                  <button
                    v-if="user.status === 'active'"
                    type="button"
                    class="btn btn-sm btn-outline-danger d-flex align-items-center gap-1.5 px-2.5 py-1 rounded-2"
                    @click="toggleStatus(user)"
                    title="停用此帳號"
                  >
                    <i class="bi bi-person-x"></i>
                    <span>停用</span>
                  </button>

                  <button
                    v-else
                    type="button"
                    class="btn btn-sm btn-outline-success d-flex align-items-center gap-1.5 px-2.5 py-1 rounded-2"
                    @click="toggleStatus(user)"
                    title="啟用此帳號"
                  >
                    <i class="bi bi-person-check"></i>
                    <span>啟用</span>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 頁尾分頁列 -->
      <div
        class="p-3 border-top d-flex flex-column flex-sm-row justify-content-between align-items-center gap-2 extra-small text-muted"
      >
        <div>顯示第 1 至 {{ users.length }} 筆，共 {{ users.length }} 筆</div>

        <nav aria-label="Page navigation">
          <ul class="pagination pagination-sm m-0">
            <li class="page-item disabled">
              <a class="page-link" href="#"
                ><i class="bi bi-chevron-left"></i
              ></a>
            </li>
            <li class="page-item active">
              <a class="page-link" href="#">1</a>
            </li>
            <li class="page-item">
              <a class="page-link" href="#"
                ><i class="bi bi-chevron-right"></i
              ></a>
            </li>
          </ul>
        </nav>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";

const searchQuery = ref("");

const users = ref([
  {
    id: 1,
    name: "Eve",
    email: "eve@sample.com",
    status: "active",
    role: "User",
  },
  {
    id: 2,
    name: "William",
    email: "william@sample.com",
    status: "active",
    role: "User",
  },
  {
    id: 3,
    name: "Lillian",
    email: "lillian@ragic.com",
    status: "active",
    role: "Admin",
  },
  {
    id: 4,
    name: "Kayline",
    email: "kayline@ragic.com",
    status: "disabled",
    role: "Admin",
  },
]);

const toggleStatus = (user) => {
  user.status = user.status === "active" ? "disabled" : "active";
};
</script>

<style scoped>
.extra-small {
  font-size: 0.82rem;
}

.text-slate-800 {
  color: #1e293b;
}

.search-input-group .form-control:focus {
  box-shadow: none;
  border-color: #0d6efd;
}

.shadow-2xs {
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
}

.pagination .page-link {
  color: #475569;
  border-color: #e2e8f0;
}
.pagination .page-item.active .page-link {
  background-color: #0d6efd;
  border-color: #0d6efd;
  color: #fff;
}
</style>
