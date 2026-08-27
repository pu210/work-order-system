<template>
  <section class="admin-workbench">
    <div v-if="errorMessage" class="alert alert-danger py-2" role="alert">
      {{ errorMessage }}
    </div>

    <div v-if="loading" class="empty-state">
      <span class="spinner-border spinner-border-sm me-2"></span>正在載入管理員工單…
    </div>

    <div v-else class="row g-4 align-items-start">
      <aside class="col-12 col-xl-4">
        <div class="task-panel">
          <div class="task-panel-header">
            <div class="d-flex align-items-center gap-2">
              <span class="task-panel-mark"></span>
              <h4 class="mb-0">尚未審查工單</h4>
              <span class="task-count">{{ totalActionableCount }}</span>
            </div>
            <span class="sort-caption"><i class="bi bi-sort-down me-1"></i>依優先級排序</span>
          </div>

          <form class="workbench-filters left-filters" @submit.prevent="applyLeftFilters">
            <input
              v-model.trim="leftFilterDraft.search"
              type="search"
              class="form-control form-control-sm filter-search"
              placeholder="搜尋標題或工單編號"
              aria-label="搜尋尚未審查工單"
            />
            <select
              v-model="leftFilterDraft.category"
              class="form-select form-select-sm"
              aria-label="篩選尚未審查工單分類"
            >
              <option value="">全部分類</option>
              <option v-for="category in categoryOptions" :key="category" :value="category">
                {{ category }}
              </option>
            </select>
            <select
              v-model="leftFilterDraft.priority"
              class="form-select form-select-sm"
              aria-label="篩選尚未審查工單優先級"
            >
              <option value="">全部優先級</option>
              <option v-for="priority in priorityOptions" :key="priority" :value="priority">
                {{ priority }}
              </option>
            </select>
            <button type="submit" class="btn btn-outline-secondary btn-sm">搜尋</button>
          </form>

          <div v-if="actionableTickets.length === 0" class="empty-state compact">
            <i class="bi bi-check-circle"></i>
            <div>目前沒有尚未審查的工單</div>
          </div>

          <article
            v-for="ticket in pagedActionableTickets"
            :key="`admin-task-${ticket.workOrderId}`"
            class="todo-card"
            role="link"
            tabindex="0"
            @click="goToDetail(ticket)"
            @keyup.enter="goToDetail(ticket)"
          >
            <div class="todo-card-top">
              <span class="work-order-no">{{ ticket.workOrderNo }}</span>
              <div class="card-badges">
                <span :class="['priority-pill', priorityClass(ticket.priorityName)]">
                  {{ ticket.priorityName || '未設定優先級' }}
                </span>
                <span :class="['status-pill', statusBadgeClass(ticket.status)]">
                  待審查
                </span>
              </div>
            </div>

            <div class="todo-title-row">
              <h5>{{ ticket.title }}</h5>
              <span class="todo-created"><i class="bi bi-calendar3 me-1"></i>建立：{{ formatDate(ticket.createdTime) }}</span>
            </div>
            <p class="todo-applicant"><i class="bi bi-person me-1"></i>報修人：{{ ticket.creatorName || '—' }}</p>
            <div class="detail-hint"><i class="bi bi-chevron-right"></i></div>
          </article>

          <nav
            v-if="taskTotalPages > 1"
            class="task-pagination"
            aria-label="管理員待辦分頁"
          >
            <button
              type="button"
              class="btn btn-sm btn-outline-secondary"
              :disabled="taskCurrentPage === 0"
              aria-label="上一頁待辦"
              @click="taskCurrentPage -= 1"
            >
              <i class="bi bi-chevron-left"></i>
            </button>
            <span>{{ taskCurrentPage + 1 }} / {{ taskTotalPages }}</span>
            <button
              type="button"
              class="btn btn-sm btn-outline-secondary"
              :disabled="taskCurrentPage + 1 >= taskTotalPages"
              aria-label="下一頁待辦"
              @click="taskCurrentPage += 1"
            >
              <i class="bi bi-chevron-right"></i>
            </button>
          </nav>
        </div>
      </aside>

      <main class="col-12 col-xl-8">
        <div class="orders-toolbar">
          <nav class="status-tabs" aria-label="工單狀態篩選">
            <button
              v-for="tab in statusTabs"
              :key="tab.value || 'ALL'"
              type="button"
              :class="['status-tab', { active: activeStatus === tab.value }]"
              @click="activeStatus = tab.value"
            >
              {{ tab.label }}
              <span :class="['tab-count', { 'tab-count-alert': shouldHighlightCount(tab.value) }]">
                {{ statusCount(tab.value) }}
              </span>
            </button>
          </nav>

          <label class="sort-control">
            <i class="bi bi-arrow-down-up"></i>
            <span class="visually-hidden">排序方式</span>
            <select v-model="sortMode" class="form-select form-select-sm">
              <option value="priority-desc">優先級（高→低）</option>
              <option value="created-desc">建立時間（新→舊）</option>
              <option value="created-asc">建立時間（舊→新）</option>
            </select>
          </label>
        </div>

        <form class="workbench-filters right-filters" @submit.prevent="applyRightFilters">
          <input
            v-model.trim="rightFilterDraft.search"
            type="search"
            class="form-control form-control-sm filter-search"
            placeholder="搜尋標題或工單編號"
            aria-label="搜尋工單"
          />
          <select
            v-model="rightFilterDraft.category"
            class="form-select form-select-sm"
            aria-label="篩選工單分類"
          >
            <option value="">全部分類</option>
            <option v-for="category in categoryOptions" :key="category" :value="category">
              {{ category }}
            </option>
          </select>
          <select
            v-model="rightFilterDraft.priority"
            class="form-select form-select-sm"
            aria-label="篩選工單優先級"
          >
            <option value="">全部優先級</option>
            <option v-for="priority in priorityOptions" :key="priority" :value="priority">
              {{ priority }}
            </option>
          </select>
          <button type="submit" class="btn btn-outline-secondary btn-sm">搜尋</button>
        </form>

        <div v-if="filteredTickets.length === 0" class="empty-state orders-empty">
          <i class="bi bi-inbox"></i>
          <div>這個狀態目前沒有工單</div>
        </div>

        <div v-else class="orders-list">
          <article
            v-for="ticket in pagedTickets"
            :key="ticket.workOrderId"
            class="order-card"
            role="link"
            tabindex="0"
            @click="goToDetail(ticket)"
            @keyup.enter="goToDetail(ticket)"
          >
            <div class="order-card-top">
              <div>
                <strong>{{ ticket.workOrderNo }}</strong>
                <span class="order-created">・建立：{{ formatDate(ticket.createdTime) }}</span>
              </div>
              <div class="card-badges">
                <span
                  v-if="!isClosed(ticket)"
                  :class="['priority-pill', priorityClass(ticket.priorityName)]"
                >
                  <i class="bi bi-exclamation-triangle-fill me-1"></i>
                  {{ ticket.priorityName || '未設定優先級' }}
                </span>
                <span v-if="!isClosed(ticket)" :class="['overdue-pill', overdueClass(ticket)]">
                  {{ overdueLabel(ticket) }}
                </span>
                <span :class="['status-pill', statusBadgeClass(ticket.status)]">
                  {{ displayStatusLabel(ticket) }}
                </span>
              </div>
            </div>

            <div class="order-card-body">
              <div class="order-main min-width-0">
                <h4 class="mb-2">{{ ticket.title }}</h4>
                <p class="mb-1">
                  報修人：{{ ticket.creatorName || '—' }}
                  <span class="mx-1">｜</span>
                  負責管理員：{{ ticket.adminName || '尚未指定' }}
                </p>
                <p class="mb-0">
                  負責工程師：{{ ticket.assignedHandlerName || '尚未指派' }}
                  <span class="mx-1">｜</span>
                  完成期限：{{ formatDateTime(ticket.dueTime) }}
                </p>
              </div>

              <button
                type="button"
                class="btn btn-outline-secondary btn-sm flex-shrink-0"
                @click.stop="goToDetail(ticket)"
              >
                查看詳情
              </button>
            </div>
          </article>
        </div>

        <nav
          v-if="totalPages > 1"
          class="pagination-bar"
          aria-label="管理員工單分頁"
        >
          <button
            type="button"
            class="btn btn-sm btn-outline-secondary"
            :disabled="currentPage === 0"
            @click="currentPage -= 1"
          >
            <i class="bi bi-chevron-left me-1"></i>上一頁
          </button>
          <span>第 {{ currentPage + 1 }} / {{ totalPages }} 頁</span>
          <button
            type="button"
            class="btn btn-sm btn-outline-secondary"
            :disabled="currentPage + 1 >= totalPages"
            @click="currentPage += 1"
          >
            下一頁<i class="bi bi-chevron-right ms-1"></i>
          </button>
        </nav>
      </main>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref, watch } from "vue";
import { useRouter } from "vue-router";
import { getWorkOrderList } from "@/api/workOrder.js";
import { statusBadgeClass, statusLabel } from "@/constants/workOrderStatus.js";

const router = useRouter();

const statusTabs = [
  { value: "", label: "未結案" },
  { value: "RE_REVIEW", label: "重新審查" },
  { value: "IN_PROGRESS", label: statusLabel("IN_PROGRESS") },
  { value: "PENDING_USER_ACCEPTANCE", label: statusLabel("PENDING_USER_ACCEPTANCE") },
  { value: "PENDING_ADMIN_ACCEPTANCE", label: statusLabel("PENDING_ADMIN_ACCEPTANCE") },
  { value: "COMPLETED", label: statusLabel("COMPLETED") },
  { value: "CANCELLED", label: statusLabel("CANCELLED") },
];

const tickets = ref([]);
const loading = ref(false);
const errorMessage = ref("");
const activeStatus = ref("");
const sortMode = ref("priority-desc");
const currentPage = ref(0);
const taskCurrentPage = ref(0);
const pageSize = 5;
const taskPageSize = 5;
const leftFilterDraft = ref(emptyFilters());
const leftFilters = ref(emptyFilters());
const rightFilterDraft = ref(emptyFilters());
const rightFilters = ref(emptyFilters());

const categoryOptions = computed(() => uniqueOptions("categoryName"));
const priorityOptions = computed(() => uniqueOptions("priorityName"));

const actionableTickets = computed(() =>
  sortTaskTickets(
    tickets.value
      .filter(
        (ticket) => ticket.status === "PENDING_REVIEW" && ticket.adminUserId == null,
      )
      .filter((ticket) => matchesFilters(ticket, leftFilters.value)),
  ),
);

const totalActionableCount = computed(() => actionableTickets.value.length);

const taskTotalPages = computed(() =>
  Math.max(1, Math.ceil(actionableTickets.value.length / taskPageSize)),
);

const pagedActionableTickets = computed(() => {
  const start = taskCurrentPage.value * taskPageSize;
  return actionableTickets.value.slice(start, start + taskPageSize);
});

watch(actionableTickets, () => {
  if (taskCurrentPage.value >= taskTotalPages.value) {
    taskCurrentPage.value = Math.max(0, taskTotalPages.value - 1);
  }
});

function sortTaskTickets(items) {
  return [...items].sort((a, b) => {
    const difference = priorityWeight(b.priorityName) - priorityWeight(a.priorityName);
    return difference || timestamp(b.createdTime) - timestamp(a.createdTime);
  });
}

async function fetchAllTickets() {
  const fetchSize = 100;
  const allTickets = [];
  let page = 0;
  let totalPages = 1;

  do {
    const result = await getWorkOrderList({
      page,
      size: fetchSize,
      sort: "WORK_ORDER_NO_ASC",
    });
    allTickets.push(...(result?.content ?? []));
    totalPages = Number(result?.totalPages ?? 0);
    page += 1;
  } while (page < totalPages);

  return allTickets;
}

const filteredTickets = computed(() => {
  let matches = tickets.value.filter(
    (ticket) =>
      !(ticket.status === "PENDING_REVIEW" && ticket.adminUserId == null) &&
      !["COMPLETED", "CANCELLED"].includes(ticket.status),
  );
  if (activeStatus.value === "RE_REVIEW") {
    matches = tickets.value.filter(
      (ticket) => ticket.status === "PENDING_REVIEW" && ticket.adminUserId != null,
    );
  } else if (activeStatus.value) {
    matches = tickets.value.filter((ticket) => ticket.status === activeStatus.value);
  }
  return sortTickets(matches.filter((ticket) => matchesFilters(ticket, rightFilters.value)));
});

const totalPages = computed(() =>
  Math.max(1, Math.ceil(filteredTickets.value.length / pageSize)),
);

const pagedTickets = computed(() => {
  const start = currentPage.value * pageSize;
  return filteredTickets.value.slice(start, start + pageSize);
});

watch([activeStatus, sortMode], () => {
  currentPage.value = 0;
});

function emptyFilters() {
  return { search: "", category: "", priority: "" };
}

function uniqueOptions(field) {
  return [...new Set(tickets.value.map((ticket) => ticket[field]).filter(Boolean))]
    .map(String)
    .sort((a, b) => a.localeCompare(b, "zh-TW"));
}

function matchesFilters(ticket, filters) {
  const query = String(filters.search || "").trim().toLocaleLowerCase("zh-TW");
  const matchesSearch =
    !query ||
    [ticket.title, ticket.workOrderNo].some((value) =>
      String(value || "").toLocaleLowerCase("zh-TW").includes(query),
    );
  const matchesCategory =
    !filters.category || String(ticket.categoryName || "") === filters.category;
  const matchesPriority =
    !filters.priority || String(ticket.priorityName || "") === filters.priority;
  return matchesSearch && matchesCategory && matchesPriority;
}

function applyLeftFilters() {
  leftFilters.value = { ...leftFilterDraft.value };
  taskCurrentPage.value = 0;
}

function applyRightFilters() {
  rightFilters.value = { ...rightFilterDraft.value };
  currentPage.value = 0;
}

function sortTickets(items) {
  const result = [...items];
  if (sortMode.value === "created-desc") {
    return result.sort((a, b) => timestamp(b.createdTime) - timestamp(a.createdTime));
  }
  if (sortMode.value === "created-asc") {
    return result.sort((a, b) => timestamp(a.createdTime) - timestamp(b.createdTime));
  }
  return result.sort((a, b) => {
    const difference = priorityWeight(b.priorityName) - priorityWeight(a.priorityName);
    return difference || timestamp(b.createdTime) - timestamp(a.createdTime);
  });
}

function priorityWeight(priorityName) {
  const value = String(priorityName || "").toUpperCase();
  if (value.includes("緊急") || value.includes("URGENT") || value.includes("CRITICAL")) return 4;
  if (value.includes("高") || value.includes("HIGH")) return 3;
  if (value.includes("中") || value.includes("MEDIUM")) return 2;
  if (value.includes("低") || value.includes("LOW")) return 1;
  return 0;
}

function priorityClass(priorityName) {
  const weight = priorityWeight(priorityName);
  if (weight >= 4) return "priority-critical";
  if (weight === 3) return "priority-high";
  if (weight === 2) return "priority-medium";
  return "priority-low";
}

function displayStatusLabel(ticket) {
  return statusLabel(ticket.status);
}

function statusCount(status) {
  if (status === "RE_REVIEW") {
    return tickets.value.filter(
      (ticket) => ticket.status === "PENDING_REVIEW" && ticket.adminUserId != null,
    ).length;
  }
  if (!status) {
    return tickets.value.filter(
      (ticket) =>
        !(ticket.status === "PENDING_REVIEW" && ticket.adminUserId == null) &&
        !["COMPLETED", "CANCELLED"].includes(ticket.status),
    ).length;
  }
  return tickets.value.filter((ticket) => ticket.status === status).length;
}

function shouldHighlightCount(status) {
  return ["RE_REVIEW", "PENDING_ADMIN_ACCEPTANCE"].includes(status) && statusCount(status) > 0;
}

function isClosed(ticket) {
  return ["COMPLETED", "CANCELLED"].includes(ticket.status);
}

function overdueLabel(ticket) {
  if (["COMPLETED", "CANCELLED"].includes(ticket.status)) return "不適用";
  if (!ticket.dueTime) return "尚未設定";
  return ticket.isOverdue ? "已逾期" : "未逾期";
}

function overdueClass(ticket) {
  if (["COMPLETED", "CANCELLED"].includes(ticket.status) || !ticket.dueTime) {
    return "overdue-neutral";
  }
  return ticket.isOverdue ? "overdue-danger" : "overdue-ok";
}

function timestamp(value) {
  const parsed = value ? new Date(value).getTime() : 0;
  return Number.isNaN(parsed) ? 0 : parsed;
}

function formatDate(value) {
  if (!value) return "—";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return String(value).replace("T", " ").slice(0, 16);
  return new Intl.DateTimeFormat("zh-TW", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  }).format(date);
}

function formatDateTime(value) {
  if (!value) return "尚未設定";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return String(value).replace("T", " ").slice(0, 16);
  return new Intl.DateTimeFormat("zh-TW", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
    hour12: false,
  }).format(date);
}

async function loadTickets() {
  loading.value = true;
  errorMessage.value = "";
  try {
    tickets.value = await fetchAllTickets();
    currentPage.value = 0;
    taskCurrentPage.value = 0;
  } catch (error) {
    errorMessage.value = error.response?.data?.message || "無法載入管理員工單";
  } finally {
    loading.value = false;
  }
}

function goToDetail(ticket) {
  router.push({
    name: "ticket-detail",
    params: { id: ticket.workOrderId },
    query: { from: "ticket-assign" },
  });
}

onMounted(loadTickets);
</script>

<style scoped>
.admin-workbench { --accent: #8b5cf6; --ink: #17243b; --muted: #718096; --border: #e3e8f0; color: var(--ink); }
.task-panel, .orders-toolbar, .order-card { border: 1px solid var(--border); background: #fff; box-shadow: 0 8px 24px rgba(23, 36, 59, 0.07); }
.task-panel { padding: 1.5rem; border-color: #e9dcff; border-radius: 18px; }
.task-panel-header { display: flex; justify-content: space-between; align-items: center; gap: 1rem; padding-bottom: 1.15rem; border-bottom: 1px solid #edf0f5; }
.task-panel-header h4 { font-size: 1.15rem; font-weight: 750; }
.task-panel-mark { width: 11px; height: 30px; border-radius: 6px; background: var(--accent); }
.task-count { min-width: 38px; padding: 0.2rem 0.65rem; border-radius: 999px; background: #f1eaff; color: #7541d4; font-weight: 700; text-align: center; }
.sort-caption { color: #97a3b5; font-size: 0.82rem; white-space: nowrap; }
.workbench-filters { display: grid; grid-template-columns: minmax(180px, 1.5fr) repeat(2, minmax(120px, 1fr)) auto; gap: 0.55rem; margin-bottom: 1rem; }
.workbench-filters .form-control, .workbench-filters .form-select, .workbench-filters .btn { min-height: 34px; font-size: 0.78rem; }
.left-filters { grid-template-columns: repeat(2, minmax(0, 1fr)) auto; margin-top: 1rem; }
.left-filters .filter-search { grid-column: 1 / -1; }
.workbench-filters .btn { padding-right: 0.9rem; padding-left: 0.9rem; white-space: nowrap; }
.todo-card { position: relative; padding: 1rem 2rem 1rem 1rem; border: 1px solid #dde3ec; border-radius: 13px; background: #fbfcfe; cursor: pointer; transition: 0.2s ease; }
.todo-card + .todo-card { margin-top: 0.8rem; }
.todo-card-top { display: flex; justify-content: space-between; align-items: flex-start; gap: 0.6rem; margin-bottom: 0.75rem; }
.todo-card:hover, .todo-card:focus-visible, .order-card:hover, .order-card:focus-visible { border-color: #bca4f4; box-shadow: 0 12px 26px rgba(124, 83, 210, 0.13); outline: none; transform: translateY(-2px); }
.todo-card h5 { overflow: hidden; color: #24344f; font-size: 0.96rem; font-weight: 750; text-overflow: ellipsis; white-space: nowrap; }
.todo-card p { margin: 0.2rem 0; color: #7c899d; font-size: 0.78rem; }
.todo-title-row { display: flex; align-items: baseline; gap: 0.55rem; margin-bottom: 0.55rem; }
.todo-title-row h5 { flex: 1 1 auto; min-width: 0; margin: 0; }
.todo-created { flex: 0 0 auto; margin-left: auto; color: #8a96a8; font-size: 0.72rem; white-space: nowrap; }
.todo-applicant { overflow-wrap: anywhere; }
.detail-hint { position: absolute; top: 50%; right: 0.75rem; color: #a3aec0; transform: translateY(-50%); }
.work-order-no { color: #67758a; font-size: 0.88rem; }
.card-badges { display: flex; flex-wrap: wrap; justify-content: flex-end; align-items: center; gap: 0.4rem; }
.status-pill, .priority-pill, .overdue-pill { display: inline-flex; align-items: center; padding: 0.3rem 0.7rem; border-radius: 999px; font-size: 0.78rem; font-weight: 700; white-space: nowrap; }
.priority-pill { border: 1px solid transparent; border-radius: 6px; }
.priority-critical, .priority-high { border-color: #ffc9c9; background: #fff0f0; color: #e13c3c; }
.priority-medium { border-color: #ffe1a8; background: #fff8e7; color: #b66d00; }
.priority-low { border-color: #d8e1ec; background: #f4f7fa; color: #65758a; }
.overdue-danger { background: #ffe1e1; color: #c92a2a; }
.overdue-ok { background: #dcf7e8; color: #198754; }
.overdue-neutral { background: #eef1f5; color: #6b7280; }
.orders-toolbar { display: flex; align-items: center; gap: 0.45rem; margin-bottom: 1.1rem; padding: 0 0.7rem; border-radius: 17px; }
.status-tabs { display: flex; flex: 1 1 auto; align-items: stretch; justify-content: space-between; min-width: 0; }
.status-tab { position: relative; min-width: 0; padding: 0.9rem 0.3rem; border: 0; border-radius: 9px; background: transparent; color: #7b8799; font-size: 0.78rem; text-align: center; white-space: nowrap; transition: 0.18s ease; }
.status-tab:hover { background: #f6f8fb; color: #536277; }
.status-tab::after { position: absolute; right: 0.55rem; bottom: 0; left: 0.55rem; height: 3px; border-radius: 3px 3px 0 0; background: transparent; content: ""; }
.status-tab.active { color: #7c3aed; font-weight: 750; }
.status-tab.active::after { background: #7c3aed; }
.tab-count { display: inline-block; min-width: 18px; margin-left: 0.1rem; padding: 0.03rem 0.28rem; border-radius: 999px; background: #f1f3f7; color: #7b8799; font-size: 0.65rem; }
.tab-count-alert { background: #dc3545; color: #fff; }
.sort-control { display: flex; align-items: center; flex: 0 0 auto; gap: 0.2rem; padding-left: 0.45rem; border-left: 1px solid #edf0f4; color: #7b8799; font-size: 0.76rem; white-space: nowrap; }
.sort-control .form-select { width: 126px; min-height: 30px; padding: 0.2rem 1.5rem 0.2rem 0.35rem; border: 0; background-position: right 0.35rem center; background-size: 10px 8px; font-size: 0.75rem; font-weight: 650; box-shadow: none; }
.orders-list { display: grid; gap: 1rem; }
.order-card { overflow: hidden; border-radius: 17px; cursor: pointer; transition: 0.2s ease; }
.order-card-top { display: flex; justify-content: space-between; align-items: center; gap: 1rem; padding: 0.8rem 1.5rem; border-bottom: 1px solid #e7ebf1; background: #fbfcfe; }
.order-created { color: #8a96a8; font-size: 0.83rem; }
.order-card-body { display: flex; justify-content: space-between; align-items: center; gap: 1.5rem; padding: 1.5rem; }
.order-main h4 { overflow: hidden; font-size: 1.08rem; font-weight: 760; text-overflow: ellipsis; white-space: nowrap; }
.order-main p { color: #738096; font-size: 0.86rem; }
.empty-state { padding: 3rem 1.5rem; border: 1px dashed #ccd5e2; border-radius: 16px; background: #fff; color: var(--muted); text-align: center; }
.empty-state.compact { padding: 2.2rem 1rem; }
.empty-state i { display: block; margin-bottom: 0.6rem; color: #91a2b9; font-size: 1.8rem; }
.orders-empty { min-height: 220px; }
.pagination-bar { display: flex; justify-content: center; align-items: center; gap: 1rem; margin-top: 1.25rem; color: #738096; font-size: 0.82rem; }
.task-pagination { display: flex; justify-content: center; align-items: center; gap: 0.7rem; margin-top: 1rem; color: #738096; font-size: 0.78rem; }
.task-pagination .btn { width: 32px; height: 30px; padding: 0; }
.min-width-0 { min-width: 0; }
@media (max-width: 767.98px) {
  .task-panel-header, .order-card-body { align-items: stretch; flex-direction: column; }
  .sort-caption { padding-left: 1.2rem; }
  .orders-toolbar { align-items: stretch; flex-direction: column; padding: 0.75rem; }
  .workbench-filters, .left-filters { grid-template-columns: 1fr; }
  .left-filters .filter-search { grid-column: auto; }
  .status-tabs { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); width: 100%; }
  .sort-control { align-self: flex-end; padding-top: 0.55rem; padding-left: 0; border-top: 1px solid #edf0f4; border-left: 0; }
  .todo-card-top, .order-card-top { align-items: flex-start; flex-direction: column; }
  .card-badges { justify-content: flex-start; }
  .order-main h4 { white-space: normal; }
  .pagination-bar { gap: 0.55rem; }
}
</style>
