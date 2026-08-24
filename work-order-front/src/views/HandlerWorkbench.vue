<template>
  <section class="engineer-lobby">
    <div v-if="errorMessage" class="alert alert-danger py-2" role="alert">
      {{ errorMessage }}
    </div>

    <div v-if="loading" class="empty-state">
      <span class="spinner-border spinner-border-sm me-2"></span>正在載入你的工程任務…
    </div>

    <div v-else class="row g-4 align-items-start">
      <aside class="col-12 col-xl-4">
        <div class="task-panel">
          <div class="task-panel-header">
            <div class="d-flex align-items-center gap-2">
              <span class="task-panel-mark"></span>
              <h4 class="mb-0">工程師待辦任務</h4>
              <span class="task-count">{{ actionableTickets.length }}</span>
            </div>
            <span class="sort-caption"><i class="bi bi-sort-down me-1"></i>依優先級排序</span>
          </div>

          <div class="task-summary"><span>進行中：{{ actionableTickets.length }}</span></div>

          <div v-if="actionableTickets.length === 0" class="empty-state compact">
            <i class="bi bi-check-circle"></i>
            <div>目前沒有需要處理的工單</div>
          </div>

          <article
            v-for="ticket in actionableTickets"
            :key="`todo-${ticket.workOrderId}`"
            class="todo-card"
            role="link"
            tabindex="0"
            @click="goToDetail(ticket)"
            @keyup.enter="goToDetail(ticket)"
          >
            <div class="d-flex flex-wrap align-items-center gap-2 mb-2">
              <span :class="['status-pill', statusClass(ticket.status)]">{{ statusLabel(ticket.status) }}</span>
              <span :class="['priority-pill', priorityClass(ticket.priorityName)]">
                {{ ticket.priorityName || '未設定優先級' }}
              </span>
              <span class="work-order-no">{{ ticket.workOrderNo }}</span>
            </div>

            <div class="todo-card-content">
              <div class="min-width-0">
                <h5>{{ ticket.title }}</h5>
                <p><i class="bi bi-person me-1"></i>申請人：{{ ticket.creatorName || '—' }}</p>
                <p><i class="bi bi-calendar3 me-1"></i>建立：{{ formatDate(ticket.createdTime) }}</p>
              </div>
            </div>
          </article>
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
              {{ tab.label }} <span class="tab-count">{{ statusCount(tab.value) }}</span>
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

        <div v-if="filteredTickets.length === 0" class="empty-state orders-empty">
          <i class="bi bi-inbox"></i>
          <div>這個狀態目前沒有工單</div>
        </div>

        <div v-else class="orders-list">
          <article
            v-for="ticket in filteredTickets"
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
                <span>・建立：{{ formatDate(ticket.createdTime) }}</span>
              </div>
              <span :class="['status-pill', statusClass(ticket.status)]">{{ statusLabel(ticket.status) }}</span>
            </div>

            <div class="order-card-body">
              <div class="order-main min-width-0">
                <div class="d-flex flex-wrap align-items-center gap-2 mb-2">
                  <h4 class="mb-0">{{ ticket.title }}</h4>
                  <span :class="['priority-pill', priorityClass(ticket.priorityName)]">
                    <i class="bi bi-exclamation-triangle-fill me-1"></i>
                    {{ ticket.priorityName || '未設定優先級' }}
                  </span>
                </div>
                <p class="mb-0">
                  申請人：{{ ticket.creatorName || '—' }}
                  <span class="mx-1">｜</span>
                  指派工程師：{{ ticket.assignedHandlerName || authStore.name || '—' }}
                </p>
              </div>

              <div class="order-actions">
                <button type="button" class="btn btn-outline-secondary btn-sm" @click.stop="goToDetail(ticket)">
                  查看詳情
                </button>
              </div>
            </div>
          </article>
        </div>
      </main>
    </div>

  </section>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { getWorkOrderList } from "@/api/workOrder.js";
import { useAuthStore } from "@/stores/auth.js";

const router = useRouter();
const authStore = useAuthStore();

const statusTabs = [
  { value: "", label: "全部工單" },
  { value: "CANCELLED", label: "已取消" },
  { value: "PENDING_REVIEW", label: "待審查" },
  { value: "IN_PROGRESS", label: "進行中" },
  { value: "PENDING_USER_ACCEPTANCE", label: "使用者驗收" },
  { value: "PENDING_ADMIN_ACCEPTANCE", label: "管理員驗收" },
  { value: "COMPLETED", label: "已完成" },
];

const tickets = ref([]);
const loading = ref(false);
const errorMessage = ref("");
const activeStatus = ref("");
const sortMode = ref("priority-desc");

const actionableTickets = computed(() =>
  sortTickets(tickets.value.filter((ticket) => ticket.status === "IN_PROGRESS")),
);

const filteredTickets = computed(() => {
  const matches = activeStatus.value
    ? tickets.value.filter((ticket) => ticket.status === activeStatus.value)
    : tickets.value;
  return sortTickets(matches);
});

function sortTickets(items) {
  const result = [...items];
  if (sortMode.value === "created-desc") {
    return result.sort((a, b) => timestamp(b.createdTime) - timestamp(a.createdTime));
  }
  if (sortMode.value === "created-asc") {
    return result.sort((a, b) => timestamp(a.createdTime) - timestamp(b.createdTime));
  }
  return result.sort((a, b) => {
    const priorityDifference = priorityWeight(b.priorityName) - priorityWeight(a.priorityName);
    return priorityDifference || timestamp(b.createdTime) - timestamp(a.createdTime);
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

function statusLabel(status) {
  return statusTabs.find((tab) => tab.value === status)?.label || status || "未知狀態";
}

function statusClass(status) {
  return `status-${String(status || "unknown").toLowerCase().replaceAll("_", "-")}`;
}

function statusCount(status) {
  return status ? tickets.value.filter((ticket) => ticket.status === status).length : tickets.value.length;
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

async function loadTickets() {
  loading.value = true;
  errorMessage.value = "";
  try {
    if (!authStore.userId) throw new Error("無法取得目前登入的工程師資料");
    const result = await getWorkOrderList({
      assignedHandlerId: authStore.userId,
      page: 0,
      size: 100,
    });
    tickets.value = result?.content ?? [];
  } catch (error) {
    errorMessage.value = error.response?.data?.message || error.message || "無法載入工程師任務";
  } finally {
    loading.value = false;
  }
}

function goToDetail(ticket) {
  router.push({
    name: "ticket-detail",
    params: { id: ticket.workOrderId },
    query: { from: "handler-workbench" },
  });
}

onMounted(loadTickets);
</script>

<style scoped>
.engineer-lobby {
  --primary: #2f6fed;
  --accent: #ff5438;
  --ink: #17243b;
  --muted: #718096;
  --border: #e3e8f0;
  color: var(--ink);
}
.task-panel, .orders-toolbar, .order-card {
  border: 1px solid var(--border);
  background: #fff;
  box-shadow: 0 8px 24px rgba(23, 36, 59, 0.07);
}
.task-panel { padding: 1.5rem; border-color: #f3dfc6; border-radius: 18px; }
.task-panel-header { display: flex; justify-content: space-between; align-items: center; gap: 1rem; padding-bottom: 1.15rem; border-bottom: 1px solid #edf0f5; }
.task-panel-header h4 { font-size: 1.15rem; font-weight: 750; }
.task-panel-mark { width: 11px; height: 30px; border-radius: 6px; background: var(--accent); }
.task-count { min-width: 38px; padding: 0.2rem 0.65rem; border-radius: 999px; background: #fff0dc; color: #ff5c39; font-weight: 700; text-align: center; }
.sort-caption { color: #97a3b5; font-size: 0.82rem; white-space: nowrap; }
.task-summary { padding: 1rem 0; }
.task-summary span { display: inline-block; padding: 0.3rem 0.7rem; border: 1px solid #e1e6ed; border-radius: 6px; background: #f7f9fc; color: #66748a; font-size: 0.82rem; }
.todo-card { padding: 1rem; border: 1px solid #dde3ec; border-radius: 13px; background: #fbfcfe; cursor: pointer; transition: 0.2s ease; }
.todo-card + .todo-card { margin-top: 0.8rem; }
.todo-card:hover, .todo-card:focus-visible, .order-card:hover, .order-card:focus-visible { border-color: #9dbcfb; box-shadow: 0 12px 26px rgba(47, 111, 237, 0.12); outline: none; transform: translateY(-2px); }
.todo-card-content { display: flex; justify-content: space-between; align-items: center; gap: 1rem; }
.todo-card h5 { margin-bottom: 0.55rem; overflow: hidden; color: #24344f; font-size: 0.96rem; font-weight: 750; text-overflow: ellipsis; white-space: nowrap; }
.todo-card p { margin: 0.2rem 0; color: #7c899d; font-size: 0.78rem; }
.work-order-no { color: #67758a; font-size: 0.88rem; }
.status-pill, .priority-pill { display: inline-flex; align-items: center; padding: 0.3rem 0.7rem; border-radius: 999px; font-size: 0.78rem; font-weight: 700; white-space: nowrap; }
.status-in-progress { background: #dcecff; color: #2563b8; }
.status-pending-review { background: #f3e6ff; color: #8b3cc7; }
.status-pending-user-acceptance { background: #fff0d6; color: #a96500; }
.status-pending-admin-acceptance { background: #e9e5ff; color: #5e45bd; }
.status-completed { background: #dcf7e8; color: #198754; }
.status-cancelled, .status-unknown { background: #eef1f5; color: #6b7280; }
.priority-pill { border: 1px solid transparent; border-radius: 6px; }
.priority-critical, .priority-high { border-color: #ffc9c9; background: #fff0f0; color: #e13c3c; }
.priority-medium { border-color: #ffe1a8; background: #fff8e7; color: #b66d00; }
.priority-low { border-color: #d8e1ec; background: #f4f7fa; color: #65758a; }
.orders-toolbar { display: flex; align-items: center; gap: 0.45rem; margin-bottom: 1.1rem; padding: 0 0.7rem; border-radius: 17px; }
.status-tabs { display: flex; flex: 1 1 auto; align-items: stretch; justify-content: space-between; min-width: 0; overflow: visible; }
.status-tab { position: relative; min-width: 0; padding: 0.9rem 0.3rem; border: 0; border-radius: 9px; background: transparent; color: #7b8799; font-size: 0.78rem; text-align: center; white-space: nowrap; transition: background-color 0.18s ease, color 0.18s ease; }
.status-tab:hover { background: #f6f8fb; color: #536277; }
.status-tab::after { position: absolute; right: 0.55rem; bottom: 0; left: 0.55rem; height: 3px; border-radius: 3px 3px 0 0; background: transparent; content: ""; }
.status-tab.active { color: #f04f37; font-weight: 750; }
.status-tab.active::after { background: #f04f37; }
.tab-count { display: inline-block; min-width: 18px; margin-left: 0.1rem; padding: 0.03rem 0.28rem; border-radius: 999px; background: #f1f3f7; color: #7b8799; font-size: 0.65rem; }
.sort-control { display: flex; align-items: center; flex: 0 0 auto; gap: 0.2rem; padding-left: 0.45rem; border-left: 1px solid #edf0f4; color: #7b8799; font-size: 0.76rem; white-space: nowrap; }
.sort-control .form-select { width: 126px; min-height: 30px; padding-top: 0.2rem; padding-bottom: 0.2rem; padding-left: 0.35rem; border: 0; background-position: right 0.35rem center; background-size: 10px 8px; font-size: 0.75rem; font-weight: 650; box-shadow: none; }
.orders-list { display: grid; gap: 1rem; }
.order-card { overflow: hidden; border-radius: 17px; cursor: pointer; transition: 0.2s ease; }
.order-card-top { display: flex; justify-content: space-between; align-items: center; gap: 1rem; padding: 0.8rem 1.5rem; border-bottom: 1px solid #e7ebf1; background: #fbfcfe; }
.order-card-top span:not(.status-pill) { color: #8a96a8; font-size: 0.83rem; }
.order-card-body { display: flex; justify-content: space-between; align-items: center; gap: 1.5rem; padding: 1.5rem; }
.order-main h4 { overflow: hidden; font-size: 1.08rem; font-weight: 760; text-overflow: ellipsis; white-space: nowrap; }
.order-main p { color: #738096; font-size: 0.86rem; }
.order-actions { display: flex; gap: 0.6rem; flex-shrink: 0; }
.empty-state { padding: 3rem 1.5rem; border: 1px dashed #ccd5e2; border-radius: 16px; background: #fff; color: var(--muted); text-align: center; }
.empty-state.compact { padding: 2.2rem 1rem; }
.empty-state i { display: block; margin-bottom: 0.6rem; color: #91a2b9; font-size: 1.8rem; }
.orders-empty { min-height: 220px; }
.min-width-0 { min-width: 0; }
@media (max-width: 767.98px) {
  .task-panel-header, .order-card-body { align-items: stretch; flex-direction: column; }
  .sort-caption { padding-left: 1.2rem; }
  .orders-toolbar { align-items: stretch; flex-direction: column; padding: 0.75rem; }
  .status-tabs { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); width: 100%; }
  .sort-control { align-self: flex-end; padding-top: 0.55rem; padding-left: 0; border-top: 1px solid #edf0f4; border-left: 0; }
  .order-card-top { align-items: flex-start; }
  .order-main h4 { white-space: normal; }
  .order-actions { justify-content: flex-end; }
}
</style>
