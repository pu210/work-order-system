<template>
  <div class="dashboard-page">
    <!-- 頁面標題與問候區塊 -->
    <div class="page-header dashboard-header mb-4">
      <div class="dashboard-intro">
        <h1 class="h3 fw-bold text-dark mb-1">哈囉！{{ userDisplayName }}</h1>
        <p class="text-muted small mb-0">今天日期是{{ todayFormatted }}，以下是工單狀況與行事曆（工單與Google行程）總覽。</p>
      </div>
    </div>

    <!-- 4 大 KPI 卡片區塊 (真實 SQL Server 資料庫數據) -->
    <div class="row g-3 mb-4">
      <!-- 卡片 1：待審核工單 (PENDING_REVIEW) -->
      <div class="col-md-3 d-flex">
        <div class="card card-pad shadow-sm border-0 bg-white h-100 w-100">
          <div class="text-dark small fw-bold mb-1">待審核工單</div>
          <div class="h2 fw-bold text-dark mb-0">
            <span v-if="kpiStats.loading" class="spinner-border spinner-border-sm text-secondary me-1"></span>
            <span v-else>{{ kpiStats.pendingReviewCount }}</span>
            <span class="fs-6 fw-normal text-dark ms-1">筆</span>
          </div>
          <div class="small text-dark mt-1">需儘速審核與指派工程師</div>
        </div>
      </div>

      <!-- 卡片 2：處理中工單 (IN_PROGRESS) -->
      <div class="col-md-3 d-flex">
        <div class="card card-pad shadow-sm border-0 bg-white h-100 w-100">
          <div class="text-dark small fw-bold mb-1">處理中工單</div>
          <div class="h2 fw-bold text-dark mb-0">
            <span v-if="kpiStats.loading" class="spinner-border spinner-border-sm text-secondary me-1"></span>
            <span v-else>{{ kpiStats.inProgressCount }}</span>
            <span class="fs-6 fw-normal text-dark ms-1">筆</span>
          </div>
          <div class="small text-dark mt-1">工程師積極維修中</div>
        </div>
      </div>

      <!-- 卡片 3：待驗收工單 (PENDING_USER_ACCEPTANCE + PENDING_ADMIN_ACCEPTANCE) -->
      <div class="col-md-3 d-flex">
        <div class="card card-pad shadow-sm border-0 bg-white h-100 w-100">
          <div class="text-dark small fw-bold mb-1">待驗收工單</div>
          <div class="h2 fw-bold text-dark mb-0">
            <span v-if="kpiStats.loading" class="spinner-border spinner-border-sm text-secondary me-1"></span>
            <span v-else>{{ kpiStats.pendingAcceptanceCount }}</span>
            <span class="fs-6 fw-normal text-dark ms-1">筆</span>
          </div>
          <div class="small text-dark mt-1">等待使用者/管理員確認驗收</div>
        </div>
      </div>

      <!-- 卡片 4：已完成工單 (COMPLETED) -->
      <div class="col-md-3 d-flex">
        <div class="card card-pad shadow-sm border-0 bg-white h-100 w-100">
          <div class="text-dark small fw-bold mb-1">已完成工單</div>
          <div class="h2 fw-bold text-dark mb-0">
            <span v-if="kpiStats.loading" class="spinner-border spinner-border-sm text-secondary me-1"></span>
            <span v-else>{{ kpiStats.completedCount }}</span>
            <span class="fs-6 fw-normal text-dark ms-1">筆</span>
          </div>
          <div class="small text-dark mt-1">工單成功修復並歸檔</div>
        </div>
      </div>
    </div>

    <!-- 主要內容兩欄版面：左側 FullCalendar 行事曆 + 右側最新公告 -->
    <div class="row g-3">
      <!-- 左欄：📅 FullCalendar + Google 日曆整合 -->
      <div class="col-lg-9">
        <div class="card card-pad shadow-sm border-0 bg-white h-100">
          <div class="calendar-card-header">
            <h5 class="fw-bold mb-0">📅 整合行事曆</h5>
            <div class="calendar-legends d-flex align-items-center gap-2">
              <span class="badge legend-system">系統工單</span>
              
              <!-- 未綁定時：直接取代 Google 行程 標籤，放在 系統工單 的右手邊 -->
              <button 
                v-if="!isGoogleConnected" 
                class="badge legend-google legend-google-btn border-0 shadow-2xs"
                @click="connectGoogleCalendar"
                style="cursor: pointer;"
              >
                <i class="bi bi-google me-1"></i> 按我綁定GOOGLE日曆
              </button>

              <!-- 綁定成功後：直接取代 Google 行程 標籤，放在 系統工單 的右手邊 -->
              <button 
                v-else 
                class="badge legend-google legend-google-btn border-0 shadow-2xs"
                @click="disconnectGoogleCalendar"
                title="點擊可解除 Google 日曆綁定"
                style="cursor: pointer;"
              >
                ✅ 已同步 Google 日曆 ({{ googleEvents.length }} 筆)
              </button>
            </div>
          </div>

          <!-- FullCalendar 官方組件 -->
          <div class="fullcalendar-wrap" style="min-height: 520px">
            <FullCalendar :options="calendarOptions" />
          </div>
        </div>
      </div>

      <!-- 右欄：📢 最新公告欄 -->
      <div class="col-lg-3">
        <div class="card card-pad shadow-sm border-0 bg-white h-100">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <h5 class="fw-bold mb-0">📢 最新公告</h5>
            <router-link to="/announcements" class="small text-decoration-none">
              查看全部 →
            </router-link>
          </div>

          <!-- API 取得的公告列表 -->
          <div class="announce-preview-list">
            <div
              v-for="a in announcements"
              :key="a.announcementId || a.announcement_id"
              class="py-2 border-bottom"
            >
              <div class="d-flex align-items-center gap-2 mb-1">
                <!-- 📌 置頂標籤 -->
                <span v-if="a.isPinned || a.is_pinned" class="badge bg-danger"
                  >置頂</span
                >
                <!-- 公告標題 -->
                <span class="fw-bold small text-dark">{{ a.title }}</span>
              </div>
              <!-- 發布時間 -->
              <div class="text-muted text-xs" style="font-size: 0.75rem">
                {{ formatCreatedTime(a) }}
              </div>
            </div>
          </div>

          <!-- 無公告提示 -->
          <div
            v-if="announcements.length === 0"
            class="text-muted small text-center py-4"
          >
            目前尚無公告資料
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import axios from "@/plugins/axios.js";
import plainAxios from "axios";
import Swal from "sweetalert2";

import { useAuthStore } from "@/stores/auth.js";

// 📌 匯入 API 模組
import { getWorkOrderList } from "@/api/workOrder.js";
import { getAnnouncements } from "@/api/announcement.js";

// 匯入 FullCalendar 組件與外掛
import FullCalendar from '@fullcalendar/vue3'
import dayGridPlugin from '@fullcalendar/daygrid'
import timeGridPlugin from '@fullcalendar/timegrid'
import interactionPlugin from '@fullcalendar/interaction'
import zhTwLocale from '@fullcalendar/core/locales/zh-tw'

const router = useRouter();
const authStore = useAuthStore();

// 使用者動態顯示名稱
const userDisplayName = computed(() => {
  if (authStore.name) {
    const roles = authStore.roleCodes || [];
    let roleText = "使用者";
    if (roles.includes("ADMIN")) roleText = "管理員";
    else if (roles.includes("HANDLER") || roles.includes("ENGINEER"))
      roleText = "工程師";
    return `${roleText} (${authStore.name})`;
  }
  return authStore.account || "使用者";
});

// Client ID
const GOOGLE_CLIENT_ID =
  "810812971350-qkc6j8tv3d36qskh1as240ho18b386s8.apps.googleusercontent.com";

// 格式化今日日期
const now = new Date();
const todayFormatted = `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日`;
const todayYMD = now.toISOString().split("T")[0];

// 狀態變數
const isGoogleConnected = ref(false);
const googleEvents = ref([]);
const realWorkOrderEvents = ref([]);

// 工單狀態對應色彩與文字
const STATUS_COLOR_MAP = {
  PENDING_REVIEW: "#6c757d", // 灰色 - 待審核
  IN_PROGRESS: "#2F6FED", // 藍色 - 處理中
  PENDING_USER_ACCEPTANCE: "#ffc107", // 黃色 - 待使用者驗收
  PENDING_ADMIN_ACCEPTANCE: "#fd7e14", // 橘色 - 待管理員驗收
  COMPLETED: "#198754", // 綠色 - 已完成
  CANCELLED: "#dc3545", // 紅色 - 已取消
};

const STATUS_TEXT_MAP = {
  PENDING_REVIEW: "待審核",
  IN_PROGRESS: "處理中",
  PENDING_USER_ACCEPTANCE: "待使用者驗收",
  PENDING_ADMIN_ACCEPTANCE: "待管理員驗收",
  COMPLETED: "已完成",
  CANCELLED: "已取消",
};

// -------------------------------------------------------------
// 🛠️ FullCalendar 時間格式化專用防呆函式 (ISO 8601 標準相容)
// -------------------------------------------------------------
const formatFullCalendarDate = (dateVal) => {
  if (!dateVal) return null;

  // 情況 1：若後端回傳的是陣列格式 [2026, 8, 22, 14, 30]
  if (Array.isArray(dateVal)) {
    const [y, m, d, h = 0, min = 0, s = 0] = dateVal;
    const pad = (n) => String(n).padStart(2, "0");
    return `${y}-${pad(m)}-${pad(d)}T${pad(h)}:${pad(min)}:${pad(s)}`;
  }

  // 情況 2：若後端回傳的是字串 (例如 "2026-08-22T11:30:00" 或 "2026-08-22 11:30:00")
  if (typeof dateVal === "string") {
    const cleaned = dateVal.trim().replace(" ", "T");
    if (cleaned.includes("T")) {
      return cleaned.substring(0, 19);
    }
    return cleaned.substring(0, 10);
  }

  // 情況 3：若已是 Date 物件
  if (dateVal instanceof Date) {
    return dateVal.toISOString().substring(0, 19);
  }

  return null;
};

// -------------------------------------------------------------
// 📊 1. 4 大 KPI 卡片區塊 + 依角色分流整合至 FullCalendar
// -------------------------------------------------------------
const kpiStats = ref({
  pendingReviewCount: 0, // 待審核工單筆數 (PENDING_REVIEW)
  inProgressCount: 0, // 處理中工單筆數 (IN_PROGRESS)
  pendingAcceptanceCount: 0, // 待驗收工單筆數 (PENDING_USER_ACCEPTANCE + PENDING_ADMIN_ACCEPTANCE)
  completedCount: 0, // 完成結案筆數 (COMPLETED)
  loading: true,
});

// 更新 FullCalendar 的總事件列表 (整合真實系統工單 + Google 日曆行程)
const updateCalendarEvents = () => {
  calendarOptions.value = {
    ...calendarOptions.value,
    events: [...realWorkOrderEvents.value, ...googleEvents.value],
  };
};

// 載入 4 大 KPI 統計數據與真實工單資料 (含角色分流邏輯)
const loadKpiStats = async () => {
  try {
    kpiStats.value.loading = true;

    // 1. 發送 API 請求，抓取資料庫真實工單清單
    const response = await getWorkOrderList({ page: 0, size: 1000 });
    const allTickets = response?.content || []; // 拿到完整的工單陣列

    // 2. 角色權限分流判斷
    const currentUserId = authStore.userId;
    const currentUserName = authStore.name;
    const roleCodes = authStore.roleCodes || [];

    const isAdmin = roleCodes.includes("ADMIN");
    const isHandler =
      roleCodes.includes("HANDLER") || roleCodes.includes("ENGINEER");

    let userFilteredTickets = [];

    if (isAdmin) {
      // 👑 管理員 (ADMIN)：可以看到全部的單 (權限最大)
      userFilteredTickets = allTickets;
    } else if (isHandler) {
      // 🛠️ 工程師 (HANDLER / ENGINEER)：看自己建立的單 或 指派給自己的單
      userFilteredTickets = allTickets.filter((t) => {
        const creatorId = t.creatorId ?? t.creatorUserId ?? t.creator?.userId;
        const handlerId =
          t.assignedHandlerId ??
          t.assigned_handler_id ??
          t.assignedHandler?.userId;

        // 雙層比對：先比對 ID (若有)，備用比對 Name
        const isCreatorById =
          creatorId != null &&
          currentUserId != null &&
          creatorId === currentUserId;
        const isHandlerById =
          handlerId != null &&
          currentUserId != null &&
          handlerId === currentUserId;

        const isCreatorByName = Boolean(
          t.creatorName && currentUserName && t.creatorName === currentUserName,
        );
        const isHandlerByName = Boolean(
          t.assignedHandlerName &&
          currentUserName &&
          t.assignedHandlerName === currentUserName,
        );

        return (
          isCreatorById || isHandlerById || isCreatorByName || isHandlerByName
        );
      });
    } else {
      // 👤 一般使用者 / 員工 (EMPLOYEE / USER)：依照建立者分流 (如同 my-tickets.vue)
      userFilteredTickets = allTickets.filter((t) => {
        const creatorId = t.creatorId ?? t.creatorUserId ?? t.creator?.userId;
        const isCreatorById =
          creatorId != null &&
          currentUserId != null &&
          creatorId === currentUserId;
        const isCreatorByName = Boolean(
          t.creatorName && currentUserName && t.creatorName === currentUserName,
        );

        return isCreatorById || isCreatorByName;
      });
    }

    console.log(
      `🔒 [Dashboard 權限分流] 當前角色: [${roleCodes.join(", ")}], 使用者 ID: ${currentUserId}, 過濾後展示筆數: ${userFilteredTickets.length} / 全部總筆數: ${allTickets.length}`,
    );

    // 3. 依據分流後的 userFilteredTickets 計算 4 大 KPI 卡片資料
    kpiStats.value.pendingReviewCount = userFilteredTickets.filter(
      (t) => t.status === "PENDING_REVIEW",
    ).length;
    kpiStats.value.inProgressCount = userFilteredTickets.filter(
      (t) => t.status === "IN_PROGRESS",
    ).length;
    kpiStats.value.pendingAcceptanceCount = userFilteredTickets.filter(
      (t) =>
        t.status === "PENDING_USER_ACCEPTANCE" ||
        t.status === "PENDING_ADMIN_ACCEPTANCE",
    ).length;
    kpiStats.value.completedCount = userFilteredTickets.filter(
      (t) => t.status === "COMPLETED",
    ).length;

    // 4. 轉譯分流後的工單陣列為 FullCalendar 相容的事件格式
    realWorkOrderEvents.value = userFilteredTickets.map(t => {
      const rawTime = t.createdTime || t.created_time || t.dueTime || t.due_time
      const startDate = formatFullCalendarDate(rawTime) || todayYMD

      return {
        id: `ticket-${t.workOrderId || t.work_order_id}`,
        title: t.title || "未命名工單",
        start: startDate,
        classNames: ['event-system-ticket'],
        extendedProps: {
          ticket: t,
        },
      };
    });

    // 5. 更新日曆事件
    updateCalendarEvents();
  } catch (error) {
    console.error("❌ 載入工單數據失敗：", error);
  } finally {
    kpiStats.value.loading = false;
  }
};

// 2. 公告 API 連線
const announcements = ref([]);

// 時間格式化相容
const formatCreatedTime = (a) => {
  const timeStr = a.createdTime || a.created_time || "";
  return timeStr ? timeStr.substring(0, 16).replace("T", " ") : "";
};

const loadAnnouncements = async () => {
  try {
    const data = await getAnnouncements();
    console.log("✅ 讀取後端公告成功：", data);
    announcements.value = data || [];
  } catch (error) {
    console.error("❌ 載入公告失敗：", error);
  }
};

// 3. FullCalendar 官方 Vue 3 配置
const calendarOptions = ref({
  plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
  initialView: 'dayGridMonth',
  locale: zhTwLocale,
  displayEventTime: false,
  height: 520,
  headerToolbar: {
    left: 'today',
    center: 'prev,title,next',
    right: ''
  },
  buttonText: {
    today: '本月'
  },
  eventTimeFormat: {
    hour: "2-digit",
    minute: "2-digit",
    hour12: false,
  },
  dayMaxEvents: 2,
  dayCellContent: (arg) => arg.dayNumberText.replace('日', ''),
  moreLinkText: (count) => `+${count}`,
  events: [],
  eventClick: (info) => {
    const ticket = info.event.extendedProps?.ticket;
    if (ticket) {
      const statusText = STATUS_TEXT_MAP[ticket.status] || ticket.status;
      const timeStr =
        formatFullCalendarDate(ticket.createdTime || ticket.created_time) ||
        "—";
      Swal.fire({
        title: info.event.title,
        html: `
          <div class="text-start fs-6">
            <p class="mb-2"><b>工單編號：</b>${ticket.workOrderNo || "無"}</p>
            <p class="mb-2"><b>工單標題：</b>${ticket.title || "無"}</p>
            <p class="mb-2"><b>報修類別：</b>${ticket.categoryName || "無"}</p>
            <p class="mb-2"><b>當前狀態：</b><span class="badge bg-primary">${statusText}</span></p>
            <p class="mb-2"><b>建立時間：</b>${timeStr.replace("T", " ")}</p>
            ${ticket.description ? `<p class="mb-1"><b>工單描述：</b>${ticket.description}</p>` : ""}
          </div>
        `,
        icon: "info",
        showCancelButton: true,
        confirmButtonText: "前往工單詳情 ➔",
        cancelButtonText: "關閉",
        confirmButtonColor: "#2F6FED",
      }).then((result) => {
        if (
          result.isConfirmed &&
          (ticket.workOrderId || ticket.work_order_id)
        ) {
          const id = ticket.workOrderId || ticket.work_order_id;
          router.push({ name: "ticket-detail", params: { id } });
        }
      });
    } else {
      Swal.fire({
        title: info.event.title,
        text: `日期：${info.event.startStr}`,
        icon: "info",
        confirmButtonText: "確定",
      });
    }
  },
});

// 4. 綁定 Google 日曆與獲取私人行程
const connectGoogleCalendar = () => {
  if (!window.google || !window.google.accounts) {
    Swal.fire("提示", "Google 官方 SDK 載入中，請稍後再試！", "warning");
    return;
  }

  const client = window.google.accounts.oauth2.initTokenClient({
    client_id: GOOGLE_CLIENT_ID,
    scope: "https://www.googleapis.com/auth/calendar.events",
    callback: async (response) => {
      if (response.access_token) {
        // 💾 儲存 Access Token 與過期時間至 localStorage
        const expiresIn = response.expires_in ? Number(response.expires_in) : 3600
        const expiresAt = Date.now() + expiresIn * 1000
        localStorage.setItem('google_access_token', response.access_token)
        localStorage.setItem('google_token_expires_at', String(expiresAt))

        isGoogleConnected.value = true
        Swal.fire('成功', 'Google 日曆綁定成功！同步行程中...', 'success')
        await fetchGoogleCalendarEvents(response.access_token, false)
      }
    },
  });

  client.requestAccessToken();
};

// 解除 Google 日曆綁定
const disconnectGoogleCalendar = async () => {
  const result = await Swal.fire({
    title: '確定要解除 Google 日曆綁定嗎？',
    text: '解除後將暫停於行事曆中顯示 Google 私人行程。',
    icon: 'question',
    showCancelButton: true,
    confirmButtonText: '解除綁定',
    cancelButtonText: '取消',
    confirmButtonColor: '#dc3545'
  })

  if (result.isConfirmed) {
    localStorage.removeItem('google_access_token')
    localStorage.removeItem('google_token_expires_at')
    isGoogleConnected.value = false
    googleEvents.value = []
    updateCalendarEvents()
    Swal.fire('已解除', '已成功解除 Google 日曆綁定', 'success')
  }
}

// 向 Google Calendar API 抓取行程 (帶入時間範圍與展開重複行程參數)
const fetchGoogleCalendarEvents = async (accessToken, isSilent = false) => {
  try {
    const currentDate = new Date();
    const timeMin = new Date(
      currentDate.getFullYear(),
      currentDate.getMonth() - 1,
      1,
    ).toISOString();
    const timeMax = new Date(
      currentDate.getFullYear() + 1,
      11,
      31,
    ).toISOString();

    const res = await plainAxios.get(
      "https://www.googleapis.com/calendar/v3/calendars/primary/events",
      {
        headers: {
          Authorization: `Bearer ${accessToken}`,
        },
        params: {
          timeMin: timeMin,
          timeMax: timeMax,
          singleEvents: true,
          orderBy: "startTime",
          maxResults: 250,
        },
      },
    );

    const rawItems = res.data.items || [];
    console.log("✅ Google API 抓取到的近期行程：", rawItems);

    const validItems = rawItems.filter(
      (item) =>
        item.status !== "cancelled" &&
        (item.start?.date || item.start?.dateTime),
    );

    googleEvents.value = validItems.map((item) => {
      const startDate = item.start?.date || item.start?.dateTime;
      const endDate = item.end?.date || item.end?.dateTime;
      return {
        id: item.id,
        title: item.summary || "私人行程",
        start: startDate,
        end: endDate,
        allDay: !!item.start?.date,
        classNames: ['event-google-calendar']
      }
    })

    // 動態更新包含 Google 日曆行程的總事件陣列
    updateCalendarEvents();

    if (!isSilent) {
      Swal.fire('同步完成', `已成功為您載入近期 ${googleEvents.value.length} 筆 Google 日曆私人行程！`, 'success')
    }
  } catch (error) {
    console.error('❌ 抓取 Google 日曆失敗：', error)
    // 若 Token 無效或已過期，清除暫存狀態
    localStorage.removeItem('google_access_token')
    localStorage.removeItem('google_token_expires_at')
    isGoogleConnected.value = false
    googleEvents.value = []
    updateCalendarEvents()

    if (!isSilent) {
      Swal.fire('錯誤', 'Google 日曆授權已過期或無法抓取行程，請重新綁定！', 'error')
    }
  }
}

// 自動檢查是否有暫存且未過期的 Google 存取權杖
const checkSavedGoogleToken = async () => {
  const savedToken = localStorage.getItem('google_access_token')
  const savedExpiresAt = localStorage.getItem('google_token_expires_at')

  if (savedToken && savedExpiresAt) {
    if (Date.now() < Number(savedExpiresAt)) {
      isGoogleConnected.value = true
      await fetchGoogleCalendarEvents(savedToken, true) // 靜默同步載入行程
    } else {
      localStorage.removeItem('google_access_token')
      localStorage.removeItem('google_token_expires_at')
      isGoogleConnected.value = false
    }
  }
};

// 組件掛載 (頁面開啟時自動執行)
onMounted(() => {
  loadAnnouncements()     // 載入公告列表
  loadKpiStats()          // 載入 4 大 KPI 統計數據並渲染真實工單至 FullCalendar
  checkSavedGoogleToken() // 自動還原已綁定的 Google 日曆行程
})
</script>

<style scoped>
.eyebrow {
  font-size: 0.75rem;
  letter-spacing: 0.08em;
}
.card-pad {
  border-radius: 12px;
  padding: 20px;
}
.dashboard-header {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: end;
  gap: 24px;
}
.dashboard-intro {
  min-width: 0;
}
.calendar-connect-btn {
  background-color: #d1e7dd !important;
  color: #0f5132 !important;
  border: 1px solid #badbcc !important;
  font-size: 0.9rem !important;
  font-weight: 600 !important;
  padding: 6px 12px !important;
  border-radius: 6px !important;
  width: 100% !important;
  display: inline-flex !important;
  align-items: center !important;
  justify-content: center !important;
  transition: all 0.2s ease !important;
  white-space: nowrap !important;
}
.calendar-connect-btn:hover {
  background-color: #c1e2d3 !important;
  color: #08342a !important;
  border-color: #a3d7c0 !important;
}
.calendar-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}
.calendar-legends {
  display: flex;
  align-items: center;
  gap: 8px;
}
.legend-system,
.legend-google {
  font-size: 0.9rem;
  padding: 6px 12px;
  font-weight: 600;
  border-radius: 6px;
}
.legend-system {
  background-color: #cfe2ff;
  color: #084298;
  border: 1px solid #b6d4fe;
}
.legend-google {
  background-color: #d1e7dd;
  color: #0f5132;
  border: 1px solid #badbcc;
}
.legend-google-btn {
  transition: all 0.2s ease;
}
.legend-google-btn:hover {
  background-color: #c1e2d3 !important;
  color: #08342a !important;
}
.fullcalendar-wrap {
  width: 100%;
  min-height: 520px;
}
:deep(.fc) {
  width: 100% !important;
  font-family: inherit;
}
:deep(.fc-view-harness),
:deep(.fc-scrollgrid),
:deep(.fc-col-header),
:deep(.fc-col-header table),
:deep(.fc-scrollgrid-section-header table),
:deep(.fc-scrollgrid-section-body table),
:deep(.fc-daygrid-body),
:deep(.fc-scrollgrid-sync-table) {
  width: 100% !important;
  table-layout: fixed !important;
}
:deep(.fc-header-toolbar) {
  display: flex !important;
  align-items: center !important;
  justify-content: space-between !important;
  margin-bottom: 1rem !important;
}
:deep(.fc-header-toolbar .fc-toolbar-chunk:nth-child(2)) {
  display: inline-flex !important;
  flex-direction: row !important;
  align-items: center !important;
  justify-content: center !important;
  background-color: transparent !important;
  color: #212529 !important;
  padding: 0 !important;
  border-radius: 0 !important;
  box-shadow: none !important;
  gap: 6px !important;
}
:deep(.fc-header-toolbar .fc-toolbar-title) {
  display: inline-flex !important;
  align-items: center !important;
  justify-content: center !important;
  height: 38px !important;
  box-sizing: border-box !important;
  margin: 0 6px !important;
  padding: 0 !important;
  font-size: 1.5rem !important;
  font-weight: 800 !important;
  color: #212529 !important;
  line-height: 1 !important;
  white-space: nowrap !important;
}
:deep(.fc-col-header-cell-cushion),
:deep(.fc-daygrid-day-number),
:deep(.fc-daygrid-more-link) {
  color: #212529 !important;
  text-decoration: none !important;
  font-weight: 600 !important;
}
:deep(.fc-today-button) {
  background-color: #2F6FED !important;
  border-color: #2F6FED !important;
}
:deep(.fc-today-button:hover) {
  background-color: #1F4FBF !important;
  border-color: #1F4FBF !important;
}
:deep(.fc .fc-button-primary.fc-prev-button),
:deep(.fc .fc-button-primary.fc-next-button),
:deep(.fc-prev-button),
:deep(.fc-next-button) {
  display: inline-flex !important;
  align-items: center !important;
  justify-content: center !important;
  background: transparent !important;
  background-color: transparent !important;
  border: none !important;
  border-color: transparent !important;
  outline: none !important;
  box-shadow: none !important;
  color: #212529 !important;
  font-size: 1.35rem !important;
  font-weight: 700 !important;
  padding: 0 !important;
  height: 32px !important;
  width: 32px !important;
  min-width: 32px !important;
  cursor: pointer !important;
  transition: opacity 0.2s ease !important;
}
:deep(.fc .fc-button-primary.fc-prev-button:hover),
:deep(.fc .fc-button-primary.fc-next-button:hover),
:deep(.fc .fc-button-primary.fc-prev-button:focus),
:deep(.fc .fc-button-primary.fc-next-button:focus),
:deep(.fc .fc-button-primary.fc-prev-button:active),
:deep(.fc .fc-button-primary.fc-next-button:active),
:deep(.fc-prev-button:hover),
:deep(.fc-next-button:hover),
:deep(.fc-prev-button:focus),
:deep(.fc-next-button:focus),
:deep(.fc-prev-button:active),
:deep(.fc-next-button:active) {
  background: transparent !important;
  background-color: transparent !important;
  border: none !important;
  border-color: transparent !important;
  outline: none !important;
  box-shadow: none !important;
  color: #000000 !important;
  opacity: 0.65 !important;
}
:deep(.fc-daygrid-event) {
  display: flex;
  align-items: center;
  gap: 3px;
  min-width: 0;
  margin-top: 2px;
  padding: 2px 4px;
  overflow: hidden;
  border-radius: 4px;
  font-size: clamp(0.62rem, 1vw, 0.75rem);
  line-height: 1.25;
}
:deep(.event-system-ticket) {
  background-color: #cfe2ff !important;
  border: 1px solid #b6d4fe !important;
  color: #084298 !important;
}
:deep(.event-google-calendar) {
  background-color: #d1e7dd !important;
  border: 1px solid #badbcc !important;
  color: #0f5132 !important;
}
:deep(.fc-daygrid-event .fc-event-main) {
  display: flex;
  align-items: center;
  gap: 3px;
  min-width: 0;
  overflow: hidden;
  color: inherit !important;
}
:deep(.fc-daygrid-event .fc-event-title),
:deep(.fc-daygrid-event .fc-event-time) {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
:deep(.fc-daygrid-event .fc-event-time) {
  flex: 0 0 auto;
  font-weight: 600;
}
:deep(.fc-daygrid-event .fc-event-title) {
  min-width: 0;
  font-weight: 500;
}
:deep(.fc-daygrid-event-dot) {
  display: none;
}

@media (max-width: 900px) {
  .dashboard-header {
    grid-template-columns: 1fr;
    align-items: start;
    gap: 16px;
  }

  .dashboard-intro {
    width: 100%;
  }

  .dashboard-intro h1 {
    font-size: clamp(1.25rem, 5vw, 1.75rem);
    white-space: nowrap;
  }

  .calendar-connect-btn {
    width: fit-content;
    min-height: 44px;
  }
}

@media (max-width: 768px) {
  .calendar-card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .calendar-legends {
    flex-wrap: wrap;
  }

  :deep(.fc .fc-header-toolbar) {
    display: flex !important;
    flex-direction: row !important;
    align-items: center !important;
    justify-content: space-between !important;
    gap: 8px;
    margin-bottom: 14px;
  }

  :deep(.fc .fc-toolbar-title) {
    font-size: clamp(1rem, 4vw, 1.25rem) !important;
    line-height: 1.2;
    white-space: nowrap;
  }

  :deep(.fc .fc-button) {
    min-height: 34px;
    padding: 0.4em 0.65em;
    font-size: clamp(0.72rem, 2.8vw, 0.875rem);
    white-space: nowrap;
  }
}

@media (max-width: 480px) {
  .calendar-connect-btn {
    width: 100%;
  }

  :deep(.fc .fc-header-toolbar) {
    gap: 8px;
  }

  :deep(.fc .fc-button) {
    padding-right: 0.5em;
    padding-left: 0.5em;
  }

  :deep(.fc-daygrid-day-events) {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 4px;
    margin: 2px 4px;
  }

  :deep(.fc-daygrid-day-frame > .fc-daygrid-day-events .fc-daygrid-event) {
    display: block;
    width: 8px;
    min-width: 8px;
    height: 8px;
    min-height: 8px;
    margin: 0;
    padding: 0;
    border: 0 !important;
    border-radius: 50%;
    background: transparent !important;
  }

  :deep(.fc-daygrid-day-frame > .fc-daygrid-day-events .fc-daygrid-event-dot) {
    display: block;
    margin: 0;
    border-width: 4px;
  }

  :deep(
    .fc-daygrid-day-frame
      > .fc-daygrid-day-events
      .fc-daygrid-event
      .fc-event-main
  ),
  :deep(
    .fc-daygrid-day-frame
      > .fc-daygrid-day-events
      .fc-daygrid-event
      .fc-event-time
  ),
  :deep(
    .fc-daygrid-day-frame
      > .fc-daygrid-day-events
      .fc-daygrid-event
      .fc-event-title
  ) {
    display: none;
  }

  :deep(.fc-daygrid-day-frame > .fc-daygrid-day-events .fc-daygrid-more-link) {
    padding: 0 2px;
    font-size: 0.7rem;
    line-height: 1;
  }
}
</style>
