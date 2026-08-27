<template>
  <div class="tl-page">
    <div class="tl-page-header">
      <h1 class="tl-title">工單列表</h1>
      <p class="tl-subtitle">檢視、篩選公司所有維修工單</p>
    </div>

    <div class="tl-card">
      <div class="tl-toolbar">
        <input
          v-model.trim="keyword"
          type="text"
          class="tl-input tl-search-input"
          placeholder="搜尋標題"
          @keyup.enter="reload"
        />
        <select v-model="categoryFilter" class="tl-input" @change="reload">
          <option value="">全部分類</option>
          <option
            v-for="c in categories"
            :key="c.repairCategoriesId"
            :value="c.repairCategoriesId"
          >
            {{ c.name }}
          </option>
        </select>
        <select v-model="priorityFilter" class="tl-input" @change="reload">
          <option value="">全部優先級</option>
          <option
            v-for="p in priorities"
            :key="p.prioritiesId"
            :value="p.prioritiesId"
          >
            {{ p.name }}
          </option>
        </select>
        <select v-model="statusFilter" class="tl-input" @change="reload">
          <option value="">全部狀態</option>
          <option v-for="s in STATUS_OPTIONS" :key="s.value" :value="s.value">
            {{ s.label }}
          </option>
        </select>
        <select v-model="adminFilter" class="tl-input" @change="reload">
          <option value="">全部管理員</option>
          <option v-for="a in admins" :key="a.userId" :value="a.userId">
            {{ a.name }}
          </option>
        </select>
        <select v-model="handlerFilter" class="tl-input" @change="reload">
          <option value="">全部工程師</option>
          <option v-for="h in handlers" :key="h.userId" :value="h.userId">
            {{ h.name }}
          </option>
        </select>
        <button type="button" class="tl-btn tl-btn-primary" @click="reload">
          搜尋
        </button>
        <button
          type="button"
          class="tl-btn tl-btn-secondary"
          @click="resetFilters"
        >
          重設
        </button>
      </div>

      <div v-if="errorMessage" class="tl-alert-danger">{{ errorMessage }}</div>

      <div v-else-if="loading" class="tl-loading">載入中…</div>

      <div v-else-if="tickets.length === 0" class="tl-empty-state">
        <div class="tl-empty-icon">🔍</div>
        <h3>目前沒有符合的工單</h3>
        <p>試著調整篩選條件</p>
      </div>

      <div v-else class="tl-table-wrap">
        <table class="tl-table">
          <thead>
            <tr>
              <th>工單編號</th>
              <th>標題</th>
              <th>類別</th>
              <th>優先級</th>
              <th>狀態</th>
              <th>建立人</th>
              <th>負責管理員</th>
              <th>處理人</th>
              <th>建立時間</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="t in tickets"
              :key="t.workOrderId"
              role="button"
              @click="
                router.push({
                  name: 'ticket-detail',
                  params: { id: t.workOrderId },
                  query: { from: 'ticket-list' },
                })
              "
            >
              <td class="tl-mono">{{ t.workOrderNo }}</td>
              <td>{{ t.title }}</td>
              <td>{{ t.categoryName }}</td>
              <td>{{ t.priorityName }}</td>
              <td>
                <span :class="['tl-badge', statusBadgeClass(t.status)]">{{
                  statusLabel(t.status)
                }}</span>
              </td>
              <td>{{ t.creatorName || "—" }}</td>
              <td>{{ t.adminName || "—" }}</td>
              <td>{{ t.assignedHandlerName || "—" }}</td>
              <td>{{ formatTime(t.createdTime) }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <nav v-if="totalPages > 1" class="tl-pagination">
        <button
          type="button"
          class="tl-page-btn"
          :disabled="page === 0"
          @click="goToPage(page - 1)"
        >
          上一頁
        </button>
        <span class="tl-page-info"
          >第 {{ page + 1 }} / {{ totalPages }} 頁</span
        >
        <button
          type="button"
          class="tl-page-btn"
          :disabled="page + 1 >= totalPages"
          @click="goToPage(page + 1)"
        >
          下一頁
        </button>
      </nav>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { getWorkOrderList } from "@/api/workOrder.js";
import { getAllRepairCategoriesWithPriority } from "@/api/category.js";
import { getActivePrioritiesForB } from "@/api/priority.js";
import { getUsers } from "@/api/user.js";
import {
  WORK_ORDER_STATUS_OPTIONS,
  statusBadgeClass,
  statusLabel,
} from "@/constants/workOrderStatus.js";

const router = useRouter();

const STATUS_OPTIONS = WORK_ORDER_STATUS_OPTIONS;

const tickets = ref([]);
const categories = ref([]);
const priorities = ref([]);
const handlers = ref([]);
const admins = ref([]);
const keyword = ref("");
const statusFilter = ref("");
const categoryFilter = ref("");
const priorityFilter = ref("");
const handlerFilter = ref("");
const adminFilter = ref("");
const page = ref(0);
const totalPages = ref(0);
const loading = ref(false);
const errorMessage = ref("");

function formatTime(value) {
  if (!value) return "—";
  return value.replace("T", " ").slice(0, 16);
}

async function fetchTickets() {
  loading.value = true;
  errorMessage.value = "";
  try {
    const result = await getWorkOrderList({
      keyword: keyword.value || undefined,
      status: statusFilter.value || undefined,
      categoryId: categoryFilter.value || undefined,
      priorityId: priorityFilter.value || undefined,
      assignedHandlerId: handlerFilter.value || undefined,
      adminUserId: adminFilter.value || undefined,
      page: page.value,
    });
    tickets.value = result.content;
    totalPages.value = result.totalPages;
  } catch (error) {
    errorMessage.value = "無法載入工單列表，請確認後端已啟動";
  } finally {
    loading.value = false;
  }
}

function reload() {
  page.value = 0;
  fetchTickets();
}

function resetFilters() {
  keyword.value = "";
  statusFilter.value = "";
  categoryFilter.value = "";
  priorityFilter.value = "";
  handlerFilter.value = "";
  adminFilter.value = "";
  reload();
}

function goToPage(target) {
  page.value = target;
  fetchTickets();
}

onMounted(async () => {
  try {
    const [categoryList, priorityList, handlerPage, adminPage] =
      await Promise.all([
        getAllRepairCategoriesWithPriority(),
        getActivePrioritiesForB(),
        getUsers({ roleCode: "HANDLER", status: 1, size: 100 }),
        getUsers({ roleCode: "ADMIN", status: 1, size: 100 }),
      ]);
    categories.value = categoryList;
    priorities.value = priorityList;
    handlers.value = handlerPage.content;
    admins.value = adminPage.content;
  } catch (error) {
    errorMessage.value =
      "無法載入分類/優先級/工程師/管理員選項，請確認後端已啟動";
  }
  fetchTickets();
});
</script>

<style scoped>
.tl-page {
  max-width: 1240px;
  margin: 0 auto;
}

/* ---------------------------------------------------------------------- */
/* 頁首 */
/* ---------------------------------------------------------------------- */
.tl-page-header {
  margin-bottom: 22px;
}
.tl-eyebrow {
  display: block;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.08em;
  color: var(--color-primary);
  text-transform: uppercase;
  margin-bottom: 6px;
}
.tl-title {
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 24px;
  color: var(--color-ink);
  margin: 0;
}
.tl-subtitle {
  margin: 6px 0 0;
  color: var(--color-text-muted);
  font-size: 13.5px;
}

/* ---------------------------------------------------------------------- */
/* 卡片容器 */
/* ---------------------------------------------------------------------- */
.tl-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow:
    0 1px 2px rgba(20, 33, 61, 0.05),
    0 2px 8px rgba(20, 33, 61, 0.06);
  padding: 20px 22px;
}

/* ---------------------------------------------------------------------- */
/* 工具列：搜尋 + 篩選下拉選單 */
/* ---------------------------------------------------------------------- */
.tl-toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}
.tl-input {
  padding: 8px 12px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--color-border);
  font-size: 13.5px;
  font-family: var(--font-body);
  background: #fff;
  color: var(--color-text);
}
.tl-input:focus {
  border-color: var(--color-primary);
  outline: none;
  box-shadow: 0 0 0 3px var(--color-primary-soft);
}
.tl-search-input {
  flex: 1;
  min-width: 180px;
}

/* ---------------------------------------------------------------------- */
/* 按鈕 */
/* ---------------------------------------------------------------------- */
.tl-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 13.5px;
  font-weight: 600;
  padding: 8px 16px;
  border-radius: var(--radius-sm);
  border: 1px solid transparent;
  cursor: pointer;
  transition: all 0.15s;
  font-family: var(--font-body);
}
.tl-btn-primary {
  background: var(--color-primary);
  color: #fff;
}
.tl-btn-primary:hover {
  background: var(--color-primary-dark);
}
.tl-btn-secondary {
  background: #fff;
  color: var(--color-text);
  border-color: var(--color-border);
}
.tl-btn-secondary:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

/* ---------------------------------------------------------------------- */
/* 表格 */
/* ---------------------------------------------------------------------- */
.tl-table-wrap {
  overflow-x: auto;
}
.tl-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}
.tl-table th {
  text-align: left;
  font-size: 11.5px;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: var(--color-text-faint);
  font-weight: 700;
  padding: 10px 14px;
  border-bottom: 1px solid var(--color-border);
  white-space: nowrap;
}
.tl-table td {
  padding: 13px 14px;
  border-bottom: 1px solid var(--color-border);
  vertical-align: middle;
}
.tl-table tbody tr {
  transition: background 0.12s;
  cursor: pointer;
}
.tl-table tbody tr:hover {
  background: var(--color-bg);
}
.tl-table tbody tr:last-child td {
  border-bottom: none;
}
.tl-mono {
  font-family: var(--font-mono, monospace);
  font-weight: 600;
  color: var(--color-ink);
}

/* ---------------------------------------------------------------------- */
/* 徽章 */
/* ---------------------------------------------------------------------- */
.tl-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 11.5px;
  font-weight: 700;
  line-height: 1.6;
}
.tl-badge-primary {
  background: var(--color-primary-soft);
  color: var(--color-primary-dark);
}
.tl-badge-success {
  background: var(--color-success-soft);
  color: var(--color-success);
}
.tl-badge-warning {
  background: var(--color-warning-soft);
  color: #92600f;
}
.tl-badge-danger {
  background: var(--color-danger-soft);
  color: var(--color-danger);
}
.tl-badge-neutral {
  background: #eef0f4;
  color: var(--color-text-muted);
}

/* ---------------------------------------------------------------------- */
/* 載入中 / 錯誤 / 空狀態 */
/* ---------------------------------------------------------------------- */
.tl-loading {
  padding: 32px 0;
  text-align: center;
  color: var(--color-text-muted);
  font-size: 13.5px;
}
.tl-alert-danger {
  background: var(--color-danger-soft);
  color: var(--color-danger);
  border-radius: var(--radius-sm);
  padding: 10px 14px;
  font-size: 13.5px;
}
.tl-empty-state {
  text-align: center;
  padding: 48px 20px;
  color: var(--color-text-muted);
}
.tl-empty-icon {
  font-size: 32px;
  margin-bottom: 10px;
}
.tl-empty-state h3 {
  font-size: 15px;
  color: var(--color-text);
  margin-bottom: 4px;
  font-family: var(--font-display);
}
.tl-empty-state p {
  font-size: 12.5px;
  margin: 0;
}

/* ---------------------------------------------------------------------- */
/* 分頁 */
/* ---------------------------------------------------------------------- */
.tl-pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-top: 18px;
}
.tl-page-btn {
  padding: 6px 14px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--color-border);
  background: #fff;
  cursor: pointer;
  font-size: 12.5px;
  color: var(--color-text-muted);
  font-family: var(--font-body);
}
.tl-page-btn:hover:not(:disabled) {
  border-color: var(--color-primary);
  color: var(--color-primary);
}
.tl-page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.tl-page-info {
  font-size: 12.5px;
  color: var(--color-text-muted);
}

@media (max-width: 700px) {
  .tl-toolbar {
    flex-direction: column;
    align-items: stretch;
  }
  .tl-search-input {
    min-width: 0;
  }
}
</style>
