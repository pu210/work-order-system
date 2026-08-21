<template>
  <div class="dashboard-page">
    <!-- 頁面標題與問候區塊 -->
    <div class="page-header d-flex justify-content-between align-items-end mb-4">
      <div>
        <span class="eyebrow text-primary text-uppercase fw-bold">WELCOME BACK</span>
        <h1 class="h3 fw-bold text-dark mb-1">哈囉！管理員 (王建宏)</h1>
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
      <div class="col-md-3">
        <div class="card card-pad shadow-sm border-0 bg-white">
          <div class="text-muted small fw-bold mb-1">待審核工單</div>
          <div class="h2 fw-bold text-dark mb-0">
            <span v-if="kpiStats.loading" class="spinner-border spinner-border-sm text-secondary me-1"></span>
            <span v-else>{{ kpiStats.pendingReviewCount }}</span> 筆
          </div>
          <div class="small text-danger mt-1">需儘速審核與指派工程師</div>
        </div>
      </div>

      <!-- 卡片 2：處理中工單 (IN_PROGRESS) -->
      <div class="col-md-3">
        <div class="card card-pad shadow-sm border-0 bg-white">
          <div class="text-muted small fw-bold mb-1">處理中工單</div>
          <div class="h2 fw-bold text-primary mb-0">
            <span v-if="kpiStats.loading" class="spinner-border spinner-border-sm text-primary me-1"></span>
            <span v-else>{{ kpiStats.inProgressCount }}</span> 筆
          </div>
          <div class="small text-muted mt-1">工程師積極維修中</div>
        </div>
      </div>

      <!-- 卡片 3：待驗收工單 (PENDING_USER_ACCEPTANCE + PENDING_ADMIN_ACCEPTANCE) -->
      <div class="col-md-3">
        <div class="card card-pad shadow-sm border-0 bg-white">
          <div class="text-muted small fw-bold mb-1">待驗收工單</div>
          <div class="h2 fw-bold text-warning mb-0">
            <span v-if="kpiStats.loading" class="spinner-border spinner-border-sm text-warning me-1"></span>
            <span v-else>{{ kpiStats.pendingAcceptanceCount }}</span> 筆
          </div>
          <div class="small text-warning mt-1">等待使用者/管理員確認驗收</div>
        </div>
      </div>

      <!-- 卡片 4：本月完成結案工單 (COMPLETED) -->
      <div class="col-md-3">
        <div class="card card-pad shadow-sm border-0 bg-white">
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
import { ref, onMounted } from 'vue'
import axios from '@/plugins/axios.js'
import plainAxios from 'axios'
import Swal from 'sweetalert2'

// 📌 參考 TicketList.vue 做法，匯入 getWorkOrderList API 來查詢真實資料庫工單
import { getWorkOrderList } from '@/api/workOrder.js'

// 匯入 FullCalendar 組件與外掛
import FullCalendar from '@fullcalendar/vue3'
import dayGridPlugin from '@fullcalendar/daygrid'
import timeGridPlugin from '@fullcalendar/timegrid'
import interactionPlugin from '@fullcalendar/interaction'

// Client ID
const GOOGLE_CLIENT_ID = '810812971350-qkc6j8tv3d36qskh1as240ho18b386s8.apps.googleusercontent.com'

// 格式化今日日期
const now = new Date()
const todayFormatted = `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日`
const todayYMD = now.toISOString().split('T')[0]

// 狀態變數
const isGoogleConnected = ref(false)
const googleEvents = ref([])

// -------------------------------------------------------------
// 📊 1. 4 大 KPI 卡片區塊 - 讀取真實資料庫數據
// -------------------------------------------------------------
const kpiStats = ref({
  pendingReviewCount: 0,      // 待審核工單筆數 (PENDING_REVIEW)
  inProgressCount: 0,         // 處理中工單筆數 (IN_PROGRESS)
  pendingAcceptanceCount: 0,  // 待驗收工單筆數 (PENDING_USER_ACCEPTANCE + PENDING_ADMIN_ACCEPTANCE)
  completedCount: 0,          // 完成結案筆數 (COMPLETED)
  loading: true
})

// 載入 4 大 KPI 統計數據 (傳統一般做法：一次抓取全量資料，前端用 .filter().length 算長度)
const loadKpiStats = async () => {
  try {
    kpiStats.value.loading = true

    // 1. 一般傳統方式：只發送 1 次 API 請求，抓取工單資料清單 (設定較大的 size 抓回全量陣列)
    const response = await getWorkOrderList({ page: 0, size: 1000 })
    const allTickets = response?.content || [] // 拿到完整的工單陣列

    // 2. 前端直接使用 JavaScript 原生的 .filter() 陣列過濾，再用 .length 取得長度/總筆數！
    kpiStats.value.pendingReviewCount = allTickets.filter(t => t.status === 'PENDING_REVIEW').length
    kpiStats.value.inProgressCount = allTickets.filter(t => t.status === 'IN_PROGRESS').length
    kpiStats.value.pendingAcceptanceCount = allTickets.filter(
      t => t.status === 'PENDING_USER_ACCEPTANCE' || t.status === 'PENDING_ADMIN_ACCEPTANCE'
    ).length
    kpiStats.value.completedCount = allTickets.filter(t => t.status === 'COMPLETED').length

    console.log('✅ 傳統一般方式（前端陣列 .filter().length）計算 4 大 KPI 成功：', kpiStats.value)
  } catch (error) {
    console.error('❌ 載入 KPI 統計數據失敗：', error)
  } finally {
    kpiStats.value.loading = false
  }
}

// 1. 公告 API 連線
const announcements = ref([])
const API_BASE = '/api/announcements'

// 時間格式化相容
const formatCreatedTime = (a) => {
  const timeStr = a.createdTime || a.created_time || ''
  return timeStr ? timeStr.substring(0, 16).replace('T', ' ') : ''
}

const loadAnnouncements = async () => {
  try {
    const response = await axios.get(API_BASE)
    console.log('✅ 讀取後端公告成功：', response.data)
    announcements.value = response.data || []
  } catch (error) {
    console.error('❌ 載入公告失敗：', error)
  }
}

// 2. 預設系統報修工單 (使用當前真實日期)
const systemWorkOrderEvents = [
  { id: 'wo-1', title: '🔧 WO-001: 3樓冷氣維修', start: todayYMD, backgroundColor: '#2F6FED', borderColor: '#2F6FED' },
  { id: 'wo-2', title: '🔧 WO-002: 電腦無法開機', start: todayYMD, backgroundColor: '#2F6FED', borderColor: '#2F6FED' },
  { id: 'wo-3', title: '🔧 WO-003: 印表機卡紙檢修', start: todayYMD, backgroundColor: '#D64545', borderColor: '#D64545' }
]

// 3. FullCalendar 官方 Vue 3 建議 ref 結構
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
  events: [...systemWorkOrderEvents],
  eventClick: (info) => {
    Swal.fire({
      title: info.event.title,
      text: `日期：${info.event.startStr}`,
      icon: 'info',
      confirmButtonText: '確定'
    })
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
    // 設定查詢時間範圍：前 1 個月 到 未來 12 個月 (精準捕捉當前日曆畫面上的所有行程)
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

    // 1. 過濾掉已取消 (cancelled) 以及沒有開始時間的無效行程
    const validItems = rawItems.filter(item => item.status !== 'cancelled' && (item.start?.date || item.start?.dateTime))

    // 2. 轉換成 FullCalendar 標準格式
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

    console.log('✅ 轉換成 FullCalendar 的近距離行程：', googleEvents.value)

    // 3. 動態替換 FullCalendar 的 events 陣列
    calendarOptions.value = {
      ...calendarOptions.value,
      events: [
        ...systemWorkOrderEvents,
        ...googleEvents.value
      ]
    }

    Swal.fire('同步完成', `已成功為您載入近期 ${googleEvents.value.length} 筆 Google 日曆私人行程！`, 'success')
  } catch (error) {
    console.error('❌ 抓取 Google 日曆失敗：', error)
    Swal.fire('錯誤', '無法抓取 Google 日曆行程，請確認權限', 'error')
  }
}

// 組件掛載 (頁面開啟時自動執行)
onMounted(() => {
  loadAnnouncements() // 載入公告列表
  loadKpiStats()      // 載入 4 大 KPI 工單真實統計數據
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
