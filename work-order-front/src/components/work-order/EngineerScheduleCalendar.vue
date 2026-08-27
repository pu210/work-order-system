<template>
  <section class="engineer-schedule" aria-labelledby="engineer-schedule-title">
    <div class="schedule-header">
      <div>
        <h6 id="engineer-schedule-title" class="mb-1">工程師未結案工單行事曆</h6>
        <p class="mb-0 text-muted small">依工單完成期限顯示，不包含私人 Google 行程。</p>
      </div>
      <span class="badge text-bg-light">{{ events.length }} 筆</span>
    </div>

    <div v-if="loading" class="schedule-state">
      <span class="spinner-border spinner-border-sm me-2"></span>正在載入工程師行程…
    </div>

    <div v-else-if="errorMessage" class="alert alert-danger py-2 mb-0 small" role="alert">
      {{ errorMessage }}
    </div>

    <div v-else-if="events.length === 0" class="schedule-state">
      這位工程師目前沒有設定完成期限的未結案工單
    </div>

    <template v-else>
      <FullCalendar :options="calendarOptions" />

      <div v-if="selectedTicket" class="selected-ticket">
        <div class="d-flex justify-content-between align-items-start gap-2">
          <strong>{{ selectedTicket.title }}</strong>
          <button
            type="button"
            class="btn-close selected-ticket-close"
            aria-label="關閉工單摘要"
            @click="selectedTicket = null"
          ></button>
        </div>
        <div class="small text-muted mt-2">
          {{ selectedTicket.workOrderNo }}・{{ statusText(selectedTicket.status) }}
        </div>
        <div class="small mt-1">完成期限：{{ formatDateTime(selectedTicket.dueTime) }}</div>
      </div>
    </template>
  </section>
</template>

<script setup>
import { computed, ref, watch } from "vue";
import FullCalendar from "@fullcalendar/vue3";
import dayGridPlugin from "@fullcalendar/daygrid";
import interactionPlugin from "@fullcalendar/interaction";
import zhTwLocale from "@fullcalendar/core/locales/zh-tw";
import { getWorkOrderList } from "@/api/workOrder.js";
import { statusBadgeClass, statusLabel } from "@/constants/workOrderStatus.js";

const props = defineProps({
  handlerId: { type: Number, required: true },
});

const loading = ref(false);
const errorMessage = ref("");
const tickets = ref([]);
const selectedTicket = ref(null);
let requestSequence = 0;

const events = computed(() =>
  tickets.value
    .filter(
      (ticket) =>
        ticket.dueTime && !["COMPLETED", "CANCELLED"].includes(ticket.status),
    )
    .map((ticket) => {
      return {
        id: String(ticket.workOrderId),
        title: ticket.title || ticket.workOrderNo || "未命名工單",
        start: normalizeDateTime(ticket.dueTime),
        extendedProps: { ticket },
      };
    }),
);

const calendarOptions = computed(() => ({
  plugins: [dayGridPlugin, interactionPlugin],
  locale: zhTwLocale,
  initialView: "dayGridMonth",
  height: 420,
  headerToolbar: {
    left: "prev,next",
    center: "title",
    right: "today",
  },
  buttonText: { today: "今天" },
  displayEventTime: true,
  eventTimeFormat: {
    hour: "2-digit",
    minute: "2-digit",
    hour12: false,
  },
  dayMaxEvents: 2,
  moreLinkText: (count) => `另有 ${count} 筆`,
  events: events.value,
  eventContent(info) {
    const label = document.createElement("div");
    const status = info.event.extendedProps.ticket.status;
    label.className = `calendar-event-label ${statusBadgeClass(status)}`;
    label.textContent = [info.timeText, info.event.title].filter(Boolean).join(" ");
    return { domNodes: [label] };
  },
  eventClick(info) {
    selectedTicket.value = info.event.extendedProps.ticket;
  },
  eventDidMount(info) {
    info.el.title = `${info.event.title}｜${formatDateTime(info.event.start)}`;
  },
}));

function normalizeDateTime(value) {
  if (Array.isArray(value)) {
    const [year, month, day, hour = 0, minute = 0, second = 0] = value;
    const pad = (part) => String(part).padStart(2, "0");
    return `${year}-${pad(month)}-${pad(day)}T${pad(hour)}:${pad(minute)}:${pad(second)}`;
  }
  return String(value).trim().replace(" ", "T").slice(0, 19);
}

function formatDateTime(value) {
  if (!value) return "—";
  const date = value instanceof Date ? value : new Date(normalizeDateTime(value));
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

function statusText(status) {
  return statusLabel(status) || "未知狀態";
}

async function fetchHandlerTickets() {
  const sequence = ++requestSequence;
  loading.value = true;
  errorMessage.value = "";
  selectedTicket.value = null;
  try {
    const allTickets = [];
    let page = 0;
    let totalPages = 1;
    do {
      const result = await getWorkOrderList({
        assignedHandlerId: props.handlerId,
        page,
        size: 100,
      });
      allTickets.push(...(result?.content ?? []));
      totalPages = Number(result?.totalPages ?? 0);
      page += 1;
    } while (page < totalPages && sequence === requestSequence);

    if (sequence !== requestSequence) return;
    tickets.value = allTickets;
  } catch (error) {
    if (sequence !== requestSequence) return;
    tickets.value = [];
    errorMessage.value = error.response?.data?.message || "無法載入工程師工單行事曆";
  } finally {
    if (sequence === requestSequence) loading.value = false;
  }
}

watch(() => props.handlerId, fetchHandlerTickets, { immediate: true });
</script>

<style scoped>
.engineer-schedule {
  padding: 1rem;
  border: 1px solid #dce3ee;
  border-radius: 12px;
  background: #f8fafc;
}
.schedule-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
  margin-bottom: 0.9rem;
}
.schedule-state {
  padding: 2rem 1rem;
  border: 1px dashed #cbd5e1;
  border-radius: 10px;
  background: #fff;
  color: #64748b;
  text-align: center;
  font-size: 0.86rem;
}
.selected-ticket {
  margin-top: 0.9rem;
  padding: 0.8rem 0.9rem;
  border-left: 4px solid #2f6fed;
  border-radius: 8px;
  background: #fff;
}
.selected-ticket-close { flex: 0 0 auto; font-size: 0.68rem; }

:deep(.fc) { font-size: 0.78rem; }
:deep(.fc .fc-toolbar-title) { font-size: 1rem; }
:deep(.fc .fc-button) { padding: 0.3rem 0.55rem; font-size: 0.75rem; }
:deep(.fc .fc-daygrid-event) {
  border: 0;
  background: transparent;
  cursor: pointer;
}
:deep(.calendar-event-label) {
  width: 100%;
  overflow: hidden;
  padding: 0.12rem 0.3rem;
  border-radius: 4px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

@media (max-width: 575.98px) {
  .engineer-schedule { padding: 0.75rem; }
  :deep(.fc .fc-toolbar) { align-items: stretch; flex-direction: column; gap: 0.5rem; }
}
</style>
