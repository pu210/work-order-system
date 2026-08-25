<template>
  <div class="dashboard-page">
    <!-- 頁面標題與問候區塊 -->
    <div class="page-header d-flex justify-content-between align-items-end mb-4">
      <div>
        <span class="eyebrow text-primary text-uppercase fw-bold">WELCOME BACK</span>
        <h1 class="h3 fw-bold text-dark mb-1">哈囉！{{ userDisplayName }}</h1>
        <p class="text-muted small mb-0">今天是 {{ todayFormatted }}，以下是今日工單狀況與 Google 行事曆總覽。</p>
      </div>

      <!-- Google 日曆綁定按鈕 -->
      <button 
        v-if="!isGoogleConnected" 
        class="btn btn-outline-danger shadow-sm fw-bold" 
        @click="connectGoogleCalendar"
      >
        <i class="bi bi-google me-2"></i> 🔗 綁定 Google 日曆
      </button>
      <button v-else class="btn btn-success shadow-sm fw-bold" disabled>
        <i class="bi bi-check-circle-fill me-2"></i> ✅ 已成功同步 Google 日曆 ({{ googleEvents.length }} 筆行程)
      </button>
    </div>

    <!-- 4 大 KPI 卡片區塊 (真實 SQL Server 資料庫數據) -->
    <div class="row g-3 mb-4">
      <!-- 卡片 1：待審核工單 (PENDING_REVIEW) -->
      <div class="col-md-3 d-flex">
        <div class="card card-pad shadow-sm border-0 bg-white h-100 w-100">
          <div class="text-muted small fw-bold mb-1">待審核工單</div>
          <div class="h2 fw-bold text-dark mb-0">
            <span v-if="kpiStats.loading" class="spinner-border spinner-border-sm text-secondary me-1"></span>
            <span v-else>{{ kpiStats.pendingReviewCount }}</span> 筆
          </div>
          <div class="small text-danger mt-1">需儘速審核與指派工程師</div>
        </div>
      </div>

      <!-- 卡片 2：處理中工單 (IN_PROGRESS) -->
      <div class="col-md-3 d-flex">
        <div class="card card-pad shadow-sm border-0 bg-white h-100 w-100">
          <div class="text-muted small fw-bold mb-1">處理中工單</div>
          <div class="h2 fw-bold text-primary mb-0">
            <span v-if="kpiStats.loading" class="spinner-border spinner-border-sm text-primary me-1"></span>
            <span v-else>{{ kpiStats.inProgressCount }}</span> 筆
          </div>
          <div class="small text-muted mt-1">工程師積極維修中</div>
        </div>
      </div>

      <!-- 卡片 3：待驗收工單 (PENDING_USER_ACCEPTANCE + PENDING_ADMIN_ACCEPTANCE) -->
      <div class="col-md-3 d-flex">
        <div class="card card-pad shadow-sm border-0 bg-white h-100 w-100">
          <div class="text-muted small fw-bold mb-1">待驗收工單</div>
          <div class="h2 fw-bold text-warning mb-0">
            <span v-if="kpiStats.loading" class="spinner-border spinner-border-sm text-warning me-1"></span>
            <span v-else>{{ kpiStats.pendingAcceptanceCount }}</span> 筆
          </div>
          <div class="small text-warning mt-1">等待使用者/管理員確認驗收</div>
        </div>
      </div>

      <!-- 卡片 4：本月完成結案工單 (COMPLETED) -->
      <div class="col-md-3 d-flex">
        <div class="card card-pad shadow-sm border-0 bg-white h-100 w-100">
          <div class="text-muted small fw-bold mb-1">本月完成結案</div>
          <div class="h2 fw-bold text-success mb-0">
            <span v-if="kpiStats.loading" class="spinner-border spinner-border-sm text-success me-1"></span>
            <span v-else>{{ kpiStats.completedCount }}</span> 筆
          </div>
          <div class="small text-success mt-1">工單成功修復並歸檔</div>
        </div>
      </div>
    </div>

    <!-- 主要內容兩欄版面：左側 FullCalendar 行事曆 + 右側最新公告 -->
    <div class="row g-3">
      <!-- 左欄：📅 FullCalendar + Google 日曆整合 -->
      <div class="col-lg-8">
        <div class="card card-pad shadow-sm border-0 bg-white h-100">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <h5 class="fw-bold mb-0">📅 FullCalendar 整合行事曆</h5>
            <div class="d-flex align-items-center gap-2">
              <span class="badge bg-primary">🔵 系統工單</span>
              <span class="badge bg-success">🟢 Google 私人行程</span>
            </div>
          </div>

          <!-- FullCalendar 官方組件 -->
          <div class="fullcalendar-wrap" style="min-height: 520px;">
            <FullCalendar :options="calendarOptions" />
          </div>
        </div>
      </div>

      <!-- 右欄：📢 最新公告欄 -->
      <div class="col-lg-4">
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
                <span v-if="a.isPinned || a.is_pinned" class="badge bg-danger">置頂</span>
                <!-- 公告標題 -->
                <span class="fw-bold small text-dark">{{ a.title }}</span>
              </div>
              <!-- 發布時間 -->
              <div class="text-muted text-xs" style="font-size: 0.75rem;">
                {{ formatCreatedTime(a) }}
              </div>
            </div>
          </div>

          <!-- 無公告提示 -->
          <div v-if="announcements.length === 0" class="text-muted small text-center py-4">
            目前尚無公告資料
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from '@/plugins/axios.js'
import plainAxios from 'axios'
import Swal from 'sweetalert2'

import { useAuthStore } from '@/stores/auth.js'

// 📌 匯入 API 模組
import { getWorkOrderList } from '@/api/workOrder.js'
import { getAnnouncements } from '@/api/announcement.js'

// 匯入 FullCalendar 組件與外掛
import FullCalendar from '@fullcalendar/vue3'
import dayGridPlugin from '@fullcalendar/daygrid'
import timeGridPlugin from '@fullcalendar/timegrid'
import interactionPlugin from '@fullcalendar/interaction'

const router = useRouter()
const authStore = useAuthStore()

// 使用者動態顯示名稱
const userDisplayName = computed(() => {
  if (authStore.name) {
    const roles = authStore.roleCodes || []
    let roleText = '使用者'
    if (roles.includes('ADMIN')) roleText = '管理員'
    else if (roles.includes('HANDLER') || roles.includes('ENGINEER')) roleText = '工程師'
    return `${roleText} (${authStore.name})`
  }
  return authStore.account || '使用者'
})

// Client ID
const GOOGLE_CLIENT_ID = '810812971350-qkc6j8tv3d36qskh1as240ho18b386s8.apps.googleusercontent.com'

// 格式化今日日期
const now = new Date()
const todayFormatted = `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日`
const todayYMD = now.toISOString().split('T')[0]

// 狀態變數
const isGoogleConnected = ref(false)
const googleEvents = ref([])
const realWorkOrderEvents = ref([])

// 工單狀態對應色彩與文字
const STATUS_COLOR_MAP = {
  PENDING_REVIEW: '#6c757d',          // 灰色 - 待審核
  IN_PROGRESS: '#2F6FED',             // 藍色 - 處理中
  PENDING_USER_ACCEPTANCE: '#ffc107', // 黃色 - 待使用者驗收
  PENDING_ADMIN_ACCEPTANCE: '#fd7e14',// 橘色 - 待管理員驗收
  COMPLETED: '#198754',               // 綠色 - 已完成
  CANCELLED: '#dc3545'                // 紅色 - 已取消
}

const STATUS_TEXT_MAP = {
  PENDING_REVIEW: '待審核',
  IN_PROGRESS: '處理中',
  PENDING_USER_ACCEPTANCE: '待使用者驗收',
  PENDING_ADMIN_ACCEPTANCE: '待管理員驗收',
  COMPLETED: '已完成',
  CANCELLED: '已取消'
}

// -------------------------------------------------------------
// 🛠️ FullCalendar 時間格式化專用防呆函式 (ISO 8601 標準相容)
// -------------------------------------------------------------
const formatFullCalendarDate = (dateVal) => {
  if (!dateVal) return null

  // 情況 1：若後端回傳的是陣列格式 [2026, 8, 22, 14, 30]
  if (Array.isArray(dateVal)) {
    const [y, m, d, h = 0, min = 0, s = 0] = dateVal
    const pad = (n) => String(n).padStart(2, '0')
    return `${y}-${pad(m)}-${pad(d)}T${pad(h)}:${pad(min)}:${pad(s)}`
  }

  // 情況 2：若後端回傳的是字串 (例如 "2026-08-22T11:30:00" 或 "2026-08-22 11:30:00")
  if (typeof dateVal === 'string') {
    const cleaned = dateVal.trim().replace(' ', 'T')
    if (cleaned.includes('T')) {
      return cleaned.substring(0, 19)
    }
    return cleaned.substring(0, 10)
  }

  // 情況 3：若已是 Date 物件
  if (dateVal instanceof Date) {
    return dateVal.toISOString().substring(0, 19)
  }

  return null
}

// -------------------------------------------------------------
// 📊 1. 4 大 KPI 卡片區塊 + 依角色分流整合至 FullCalendar
// -------------------------------------------------------------
const kpiStats = ref({
  pendingReviewCount: 0,      // 待審核工單筆數 (PENDING_REVIEW)
  inProgressCount: 0,         // 處理中工單筆數 (IN_PROGRESS)
  pendingAcceptanceCount: 0,  // 待驗收工單筆數 (PENDING_USER_ACCEPTANCE + PENDING_ADMIN_ACCEPTANCE)
  completedCount: 0,          // 完成結案筆數 (COMPLETED)
  loading: true
})

// 更新 FullCalendar 的總事件列表 (整合真實系統工單 + Google 日曆行程)
const updateCalendarEvents = () => {
  calendarOptions.value = {
    ...calendarOptions.value,
    events: [
      ...realWorkOrderEvents.value,
      ...googleEvents.value
    ]
  }
}

// 載入 4 大 KPI 統計數據與真實工單資料 (含角色分流邏輯)
const loadKpiStats = async () => {
  try {
    kpiStats.value.loading = true

    // 1. 發送 API 請求，抓取資料庫真實工單清單
    const response = await getWorkOrderList({ page: 0, size: 1000 })
    const allTickets = response?.content || [] // 拿到完整的工單陣列

    // 2. 角色權限分流判斷
    const currentUserId = authStore.userId
    const currentUserName = authStore.name
    const roleCodes = authStore.roleCodes || []

    const isAdmin = roleCodes.includes('ADMIN')
    const isHandler = roleCodes.includes('HANDLER') || roleCodes.includes('ENGINEER')

    let userFilteredTickets = []

    if (isAdmin) {
      // 👑 管理員 (ADMIN)：可以看到全部的單 (權限最大)
      userFilteredTickets = allTickets
    } else if (isHandler) {
      // 🛠️ 工程師 (HANDLER / ENGINEER)：看自己建立的單 或 指派給自己的單
      userFilteredTickets = allTickets.filter(t => {
        const creatorId = t.creatorId ?? t.creatorUserId ?? t.creator?.userId
        const handlerId = t.assignedHandlerId ?? t.assigned_handler_id ?? t.assignedHandler?.userId

        // 雙層比對：先比對 ID (若有)，備用比對 Name
        const isCreatorById = creatorId != null && currentUserId != null && creatorId === currentUserId
        const isHandlerById = handlerId != null && currentUserId != null && handlerId === currentUserId

        const isCreatorByName = Boolean(t.creatorName && currentUserName && t.creatorName === currentUserName)
        const isHandlerByName = Boolean(t.assignedHandlerName && currentUserName && t.assignedHandlerName === currentUserName)

        return isCreatorById || isHandlerById || isCreatorByName || isHandlerByName
      })
    } else {
      // 👤 一般使用者 / 員工 (EMPLOYEE / USER)：依照建立者分流 (如同 my-tickets.vue)
      userFilteredTickets = allTickets.filter(t => {
        const creatorId = t.creatorId ?? t.creatorUserId ?? t.creator?.userId
        const isCreatorById = creatorId != null && currentUserId != null && creatorId === currentUserId
        const isCreatorByName = Boolean(t.creatorName && currentUserName && t.creatorName === currentUserName)

        return isCreatorById || isCreatorByName
      })
    }

    console.log(`🔒 [Dashboard 權限分流] 當前角色: [${roleCodes.join(', ')}], 使用者 ID: ${currentUserId}, 過濾後展示筆數: ${userFilteredTickets.length} / 全部總筆數: ${allTickets.length}`)

    // 3. 依據分流後的 userFilteredTickets 計算 4 大 KPI 卡片資料
    kpiStats.value.pendingReviewCount = userFilteredTickets.filter(t => t.status === 'PENDING_REVIEW').length
    kpiStats.value.inProgressCount = userFilteredTickets.filter(t => t.status === 'IN_PROGRESS').length
    kpiStats.value.pendingAcceptanceCount = userFilteredTickets.filter(
      t => t.status === 'PENDING_USER_ACCEPTANCE' || t.status === 'PENDING_ADMIN_ACCEPTANCE'
    ).length
    kpiStats.value.completedCount = userFilteredTickets.filter(t => t.status === 'COMPLETED').length

    // 4. 轉譯分流後的工單陣列為 FullCalendar 相容的事件格式
    realWorkOrderEvents.value = userFilteredTickets.map(t => {
      const rawTime = t.createdTime || t.created_time || t.dueTime || t.due_time
      const startDate = formatFullCalendarDate(rawTime) || todayYMD
      const color = STATUS_COLOR_MAP[t.status] || '#2F6FED'

      return {
        id: `ticket-${t.workOrderId || t.work_order_id}`,
        title: `🔧 ${t.workOrderNo ? t.workOrderNo + ': ' : ''}${t.title}`,
        start: startDate,
        backgroundColor: color,
        borderColor: color,
        extendedProps: {
          ticket: t
        }
      }
    })

    // 5. 更新日曆事件
    updateCalendarEvents()
  } catch (error) {
    console.error('❌ 載入工單數據失敗：', error)
  } finally {
    kpiStats.value.loading = false
  }
}

// 2. 公告 API 連線
const announcements = ref([])

// 時間格式化相容
const formatCreatedTime = (a) => {
  const timeStr = a.createdTime || a.created_time || ''
  return timeStr ? timeStr.substring(0, 16).replace('T', ' ') : ''
}

const loadAnnouncements = async () => {
  try {
    const data = await getAnnouncements()
    console.log('✅ 讀取後端公告成功：', data)
    announcements.value = data || []
  } catch (error) {
    console.error('❌ 載入公告失敗：', error)
  }
}

// 3. FullCalendar 官方 Vue 3 配置
const calendarOptions = ref({
  plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
  initialView: 'dayGridMonth',
  height: 520,
  headerToolbar: {
    left: 'prev,next today',
    center: 'title',
    right: 'dayGridMonth,timeGridWeek'
  },
  buttonText: {
    today: '今天',
    month: '月視圖',
    week: '週視圖'
  },
  events: [],
  eventClick: (info) => {
    const ticket = info.event.extendedProps?.ticket
    if (ticket) {
      const statusText = STATUS_TEXT_MAP[ticket.status] || ticket.status
      const timeStr = formatFullCalendarDate(ticket.createdTime || ticket.created_time) || '—'
      Swal.fire({
        title: info.event.title,
        html: `
          <div class="text-start fs-6">
            <p class="mb-2"><b>工單編號：</b>${ticket.workOrderNo || '無'}</p>
            <p class="mb-2"><b>工單標題：</b>${ticket.title || '無'}</p>
            <p class="mb-2"><b>報修類別：</b>${ticket.categoryName || '無'}</p>
            <p class="mb-2"><b>當前狀態：</b><span class="badge bg-primary">${statusText}</span></p>
            <p class="mb-2"><b>建立時間：</b>${timeStr.replace('T', ' ')}</p>
            ${ticket.description ? `<p class="mb-1"><b>工單描述：</b>${ticket.description}</p>` : ''}
          </div>
        `,
        icon: 'info',
        showCancelButton: true,
        confirmButtonText: '前往工單詳情 ➔',
        cancelButtonText: '關閉',
        confirmButtonColor: '#2F6FED'
      }).then((result) => {
        if (result.isConfirmed && (ticket.workOrderId || ticket.work_order_id)) {
          const id = ticket.workOrderId || ticket.work_order_id
          router.push({ name: 'ticket-detail', params: { id } })
        }
      })
    } else {
      Swal.fire({
        title: info.event.title,
        text: `日期：${info.event.startStr}`,
        icon: 'info',
        confirmButtonText: '確定'
      })
    }
  }
})

// 4. 綁定 Google 日曆與獲取私人行程
const connectGoogleCalendar = () => {
  if (!window.google || !window.google.accounts) {
    Swal.fire('提示', 'Google 官方 SDK 載入中，請稍後再試！', 'warning')
    return
  }

  const client = window.google.accounts.oauth2.initTokenClient({
    client_id: GOOGLE_CLIENT_ID,
    scope: 'https://www.googleapis.com/auth/calendar.events',
    callback: async (response) => {
      if (response.access_token) {
        Swal.fire('成功', 'Google 日曆綁定成功！同步行程中...', 'success')
        isGoogleConnected.value = true
        await fetchGoogleCalendarEvents(response.access_token)
      }
    }
  })

  client.requestAccessToken()
}

// 向 Google Calendar API 抓取行程 (帶入時間範圍與展開重複行程參數)
const fetchGoogleCalendarEvents = async (accessToken) => {
  try {
    const currentDate = new Date()
    const timeMin = new Date(currentDate.getFullYear(), currentDate.getMonth() - 1, 1).toISOString()
    const timeMax = new Date(currentDate.getFullYear() + 1, 11, 31).toISOString()

    const res = await plainAxios.get(
      'https://www.googleapis.com/calendar/v3/calendars/primary/events',
      {
        headers: {
          Authorization: `Bearer ${accessToken}`
        },
        params: {
          timeMin: timeMin,
          timeMax: timeMax,
          singleEvents: true,
          orderBy: 'startTime',
          maxResults: 250
        }
      }
    )

    const rawItems = res.data.items || []
    console.log('✅ Google API 抓取到的近期行程：', rawItems)

    const validItems = rawItems.filter(item => item.status !== 'cancelled' && (item.start?.date || item.start?.dateTime))

    googleEvents.value = validItems.map(item => {
      const startDate = item.start?.date || item.start?.dateTime
      const endDate = item.end?.date || item.end?.dateTime
      return {
        id: item.id,
        title: `🟢 ${item.summary || '私人行程'}`,
        start: startDate,
        end: endDate,
        allDay: !!item.start?.date,
        backgroundColor: '#198754',
        borderColor: '#198754'
      }
    })

    // 動態更新包含 Google 日曆行程的總事件陣列
    updateCalendarEvents()

    Swal.fire('同步完成', `已成功為您載入近期 ${googleEvents.value.length} 筆 Google 日曆私人行程！`, 'success')
  } catch (error) {
    console.error('❌ 抓取 Google 日曆失敗：', error)
    Swal.fire('錯誤', '無法抓取 Google 日曆行程，請確認權限', 'error')
  }
}

// 組件掛載 (頁面開啟時自動執行)
onMounted(() => {
  loadAnnouncements() // 載入公告列表
  loadKpiStats()      // 載入 4 大 KPI 統計數據並渲染真實工單至 FullCalendar
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
.fullcalendar-wrap {
  min-height: 520px;
}
:deep(.fc) {
  font-family: inherit;
}
:deep(.fc-button-primary) {
  background-color: #2F6FED !important;
  border-color: #2F6FED !important;
}
:deep(.fc-button-primary:hover) {
  background-color: #1F4FBF !important;
  border-color: #1F4FBF !important;
}
</style>
