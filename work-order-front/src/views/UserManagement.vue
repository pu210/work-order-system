<template>
  <div class="users-page">
    <!-- 頁面頂部：標題與操作按鈕 -->
    <div
      class="d-flex flex-column flex-sm-row justify-content-between align-items-sm-center mb-4 gap-3"
    >
      <div>
        <h3 class="fw-bold text-slate-800 mb-1">帳號管理</h3>
        <p class="text-muted extra-small mb-0">管理系統使用者帳號設定</p>
      </div>

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
      <div
        v-if="errorMessage"
        class="alert alert-danger py-2 px-3 m-3 extra-small"
      >
        {{ errorMessage }}
      </div>
      <!-- 頂部搜尋框 -->
      <div
        class="input-group input-group-sm search-input-group"
        style="max-width: 240px"
      >
        <span class="input-group-text bg-white border-end-0 text-muted ps-3">
          <i class="bi bi-search"></i>
        </span>

        <input
          v-model="searchQuery"
          type="search"
          class="form-control border-start-0 ps-1"
          placeholder="搜尋姓名、帳號或信箱"
          aria-label="搜尋使用者"
          @keyup.enter="
            currentPage = 0;
            loadUsers();
          "
        />
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
              :key="user.userId"
              :class="{
                'opacity-50 bg-light-subtle': user.status === 0,
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
                  class="badge bg-light text-secondary border px-2.5 py-1 rounded-pill"
                >
                  {{ statusLabels[user.status] ?? "未知狀態" }}
                </span>
              </td>

              <!-- 4. 角色 Tag -->
              <td>
                <span
                  v-for="roleCode in user.roleCodes"
                  :key="roleCode"
                  class="badge bg-light text-secondary border rounded-pill px-3 py-1 fw-normal me-1"
                >
                  {{ roleCode }}
                </span>
              </td>

              <!-- 5. 操作 (🎯 修正對齊與間距問題) -->
              <td class="pe-4 py-3 text-center">
                <div
                  class="d-inline-flex align-items-center justify-content-center gap-2"
                >
                  <router-link
                    :to="{
                      name: 'user-edit',
                      params: { id: user.userId },
                    }"
                    class="btn btn-sm btn-outline-secondary d-flex align-items-center gap-1.5 px-2.5 py-1 rounded-2"
                  >
                    <i class="bi bi-pencil-square"></i>
                    <span>編輯</span>
                  </router-link>

                  <!-- 停用 / 啟用按鈕 -->
                  <button
                    v-if="user.status === 1"
                    type="button"
                    class="btn btn-sm btn-outline-danger d-flex align-items-center gap-1.5 px-2.5 py-1 rounded-2"
                    :disabled="updatingUserId === user.userId"
                    @click="toggleStatus(user)"
                    title="停用此帳號"
                  >
                    <span
                      v-if="updatingUserId === user.userId"
                      class="spinner-border spinner-border-sm"
                    ></span>
                    <i v-else class="bi bi-person-x"></i>
                    <span>停用</span>
                  </button>

                  <button
                    v-else-if="user.status === 0"
                    type="button"
                    class="btn btn-sm btn-outline-success d-flex align-items-center gap-1.5 px-2.5 py-1 rounded-2"
                    :disabled="updatingUserId === user.userId"
                    @click="toggleStatus(user)"
                    title="啟用此帳號"
                  >
                    <span
                      v-if="updatingUserId === user.userId"
                      class="spinner-border spinner-border-sm"
                    ></span>
                    <i v-else class="bi bi-person-check"></i>
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
        <div>
          顯示第
          {{ totalElements === 0 ? 0 : currentPage * pageSize + 1 }}
          至
          {{ Math.min((currentPage + 1) * pageSize, totalElements) }}
          筆，共 {{ totalElements }} 筆
        </div>

        <nav v-if="totalPages > 0" aria-label="Page navigation">
          <ul class="pagination pagination-sm m-0">
            <!-- 上一頁 -->
            <li class="page-item" :class="{ disabled: currentPage === 0 }">
              <button
                class="page-link"
                type="button"
                :disabled="currentPage === 0"
                @click="changePage(currentPage - 1)"
              >
                <i class="bi bi-chevron-left"></i>
              </button>
            </li>

            <!-- 頁碼 -->
            <li
              v-for="pageNumber in totalPages"
              :key="pageNumber"
              class="page-item"
              :class="{ active: currentPage === pageNumber - 1 }"
            >
              <button
                class="page-link"
                type="button"
                @click="changePage(pageNumber - 1)"
              >
                {{ pageNumber }}
              </button>
            </li>

            <!-- 下一頁 -->
            <li
              class="page-item"
              :class="{ disabled: currentPage === totalPages - 1 }"
            >
              <button
                class="page-link"
                type="button"
                :disabled="currentPage === totalPages - 1"
                @click="changePage(currentPage + 1)"
              >
                <i class="bi bi-chevron-right"></i>
              </button>
            </li>
          </ul>
        </nav>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { getUsers, updateUserStatus } from "@/api/user.js";
import { getErrorMessage } from "@/utils/apiError.js";
import { notify } from "@/plugins/notify.js";

const searchQuery = ref("");
const users = ref([]);
const currentPage = ref(0);
const pageSize = ref(10);
const totalElements = ref(0);
const totalPages = ref(0);
const loading = ref(false);
const errorMessage = ref("");
const updatingUserId = ref(null);

const statusLabels = {
  0: "已停用",
  1: "使用中",
  2: "待審核",
  3: "審核未通過",
};

async function loadUsers() {
  loading.value = true;
  errorMessage.value = "";

  try {
    const data = await getUsers({
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchQuery.value.trim() || undefined,
    });

    users.value = data.content;
    totalElements.value = data.totalElements;
    totalPages.value = data.totalPages;
  } catch (error) {
    errorMessage.value = getErrorMessage(error, "人員資料載入失敗");
  } finally {
    loading.value = false;
  }
}
async function changePage(page) {
  if (page < 0 || page >= totalPages.value) {
    return;
  }

  currentPage.value = page;
  await loadUsers();
}

onMounted(loadUsers);

async function toggleStatus(user) {
  if (user.status !== 0 && user.status !== 1) {
    return;
  }

  const nextStatus = user.status === 1 ? 0 : 1;
  const actionName = nextStatus === 1 ? "啟用" : "停用";

  const result = await notify.confirm({
    title: `確定要${actionName}帳號？`,
    text: `使用者：${user.name}`,
    confirmButtonText: `確定${actionName}`,
  });

  if (!result.isConfirmed) {
    return;
  }

  updatingUserId.value = user.userId;
  errorMessage.value = "";

  try {
    await updateUserStatus(user.userId, nextStatus);
    await loadUsers();
    notify.success(`帳號已成功${actionName}`);
  } catch (error) {
    errorMessage.value = getErrorMessage(error, `${actionName}帳號失敗`);
  } finally {
    updatingUserId.value = null;
  }
}
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
