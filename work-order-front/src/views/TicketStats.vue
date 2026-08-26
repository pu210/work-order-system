<template>
  <div class="ticket-stats-container container-fluid py-4 px-3 px-md-4">
    <!-- 1. 頁面頂部標題列 -->
    <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center mb-4 gap-3">
      <div>
        <h2 class="h4 fw-bold text-dark mb-1 d-flex align-items-center gap-2">
          <i class="bi bi-pie-chart-fill text-primary"></i> 工單統計報表中心
        </h2>
        <p class="text-muted small mb-0">針對全系統工單進行多維度視覺化數據分析與占比統計</p>
      </div>

      <div class="d-flex align-items-center gap-2">
        <button class="btn btn-outline-secondary btn-sm rounded-pill px-3 shadow-2xs" @click="loadData" :disabled="loading">
          <i class="bi bi-arrow-clockwise me-1" :class="{ 'spin': loading }"></i> 重新載入
        </button>
        <button class="btn btn-primary btn-sm rounded-pill px-3 shadow-2xs" @click="exportReport">
          <i class="bi bi-download me-1"></i> 匯出報表
        </button>
      </div>
    </div>

    <!-- 2. KPI 數據摘要卡片 -->
    <div class="row g-3 mb-4">
      <div class="col-12 col-sm-6 col-xl-3">
        <div class="card border-0 shadow-2xs rounded-3 p-3 bg-white hover-lift transition-all">
          <div class="d-flex justify-content-between align-items-center">
            <div>
              <div class="text-muted small mb-1 fw-medium">工單總筆數</div>
              <div class="h3 fw-bold mb-0 text-primary">{{ totalCount }} <span class="fs-6 text-muted fw-normal">筆</span></div>
            </div>
            <div class="icon-avatar bg-primary-subtle text-primary rounded-circle p-3">
              <i class="bi bi-inboxes-fill fs-4"></i>
            </div>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="card border-0 shadow-2xs rounded-3 p-3 bg-white hover-lift transition-all">
          <div class="d-flex justify-content-between align-items-center">
            <div>
              <div class="text-muted small mb-1 fw-medium">
                {{ getDimensionTitle() }}總數
              </div>
              <div class="h3 fw-bold mb-0 text-success">{{ categoryReportList.length }} <span class="fs-6 text-muted fw-normal">類</span></div>
            </div>
            <div class="icon-avatar bg-success-subtle text-success rounded-circle p-3">
              <i class="bi bi-grid-3x3-gap-fill fs-4"></i>
            </div>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="card border-0 shadow-2xs rounded-3 p-3 bg-white hover-lift transition-all">
          <div class="d-flex justify-content-between align-items-center">
            <div>
              <div class="text-muted small mb-1 fw-medium">
                {{ getTopCategoryCardTitle() }}
              </div>
              <div class="h5 fw-bold mb-0 text-warning text-truncate" style="max-width: 140px;">
                {{ topCategory ? getDisplayName(topCategory) : '無資料' }}
              </div>
              <div class="small text-muted" v-if="topCategory">
                {{ topCategory.count || topCategory.completedCount || 0 }} 筆 ({{ topCategoryPercentage }}%)
              </div>
            </div>
            <div class="icon-avatar bg-warning-subtle text-warning rounded-circle p-3">
              <i class="bi bi-trophy-fill fs-4"></i>
            </div>
          </div>
        </div>
      </div>

      <div class="col-12 col-sm-6 col-xl-3">
        <div class="card border-0 shadow-2xs rounded-3 p-3 bg-white hover-lift transition-all">
          <div class="d-flex justify-content-between align-items-center">
            <div>
              <div class="text-muted small mb-1 fw-medium">平均工單數</div>
              <div class="h3 fw-bold mb-0 text-info">{{ avgCount }} <span class="fs-6 text-muted fw-normal">筆/類</span></div>
            </div>
            <div class="icon-avatar bg-info-subtle text-info rounded-circle p-3">
              <i class="bi bi-calculator-fill fs-4"></i>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 3. 客製化條件篩選控制列 (含 5 大維度快捷 Tab 按鈕) -->
    <div class="card border-0 shadow-sm rounded-3 mb-4 overflow-hidden">
      <div class="card-header bg-light bg-opacity-75 py-2.5 px-3 border-0 d-flex align-items-center justify-content-between flex-wrap gap-2">
        <div class="d-flex align-items-center gap-3 flex-wrap">
          <div class="d-flex align-items-center gap-1">
            <span class="text-muted small fw-medium">資料來源：</span>
            <span class="badge bg-primary-subtle text-primary border border-primary-subtle rounded-pill px-2.5 py-1 fw-normal">
              <i class="bi bi-database me-1"></i>設備報修單
            </span>
          </div>

          <!-- 5 大統計維度快速頁籤 Tab 按鈕 -->
          <div class="btn-group rounded-pill p-1 bg-white border shadow-2xs flex-wrap">
            <button 
              type="button"
              class="btn btn-sm rounded-pill px-3 py-1 transition-all" 
              :class="filterDimension === 'CATEGORY' ? 'btn-primary shadow-xs fw-bold' : 'btn-light text-secondary border-0'"
              @click="filterDimension = 'CATEGORY'"
            >
              <i class="bi bi-grid-fill me-1"></i> 大分類
            </button>
            <button 
              type="button"
              class="btn btn-sm rounded-pill px-3 py-1 transition-all" 
              :class="filterDimension === 'SUBCATEGORY' ? 'btn-primary shadow-xs fw-bold' : 'btn-light text-secondary border-0'"
              @click="filterDimension = 'SUBCATEGORY'"
            >
              <i class="bi bi-diagram-3-fill me-1"></i> 細項分類
            </button>
            <button 
              type="button"
              class="btn btn-sm rounded-pill px-3 py-1 transition-all" 
              :class="filterDimension === 'STATUS' ? 'btn-primary shadow-xs fw-bold' : 'btn-light text-secondary border-0'"
              @click="filterDimension = 'STATUS'"
            >
              <i class="bi bi-flag-fill me-1"></i> 依狀態
            </button>
            <button 
              type="button"
              class="btn btn-sm rounded-pill px-3 py-1 transition-all" 
              :class="filterDimension === 'CREATOR' ? 'btn-primary shadow-xs fw-bold' : 'btn-light text-secondary border-0'"
              @click="filterDimension = 'CREATOR'"
            >
              <i class="bi bi-person-fill me-1"></i> 依建立者
            </button>
            <button 
              type="button"
              class="btn btn-sm rounded-pill px-3 py-1 transition-all" 
              :class="filterDimension === 'PRIORITY' ? 'btn-primary shadow-xs fw-bold' : 'btn-light text-secondary border-0'"
              @click="filterDimension = 'PRIORITY'"
            >
              <i class="bi bi-exclamation-triangle-fill me-1"></i> 依優先級
            </button>
            <button 
              type="button"
              class="btn btn-sm rounded-pill px-3 py-1 transition-all" 
              :class="filterDimension === 'ENGINEER_KPI' ? 'btn-primary shadow-xs fw-bold' : 'btn-light text-secondary border-0'"
              @click="filterDimension = 'ENGINEER_KPI'"
            >
              <i class="bi bi-speedometer2 me-1"></i> 工程師 KPI
            </button>
          </div>
        </div>

        <div class="d-flex align-items-center gap-2">
          <button class="btn btn-sm btn-outline-primary rounded-pill px-3 py-1 text-nowrap" @click="resetFilters">
            <i class="bi bi-arrow-counterclockwise me-1"></i>重置篩選
          </button>
          <button class="btn btn-sm btn-primary rounded-pill px-3 py-1 text-nowrap" @click="applyFilters">
            套用篩選
          </button>
        </div>
      </div>

      <div class="card-body p-3 bg-light-subtle">
        <div class="row g-2 align-items-center">
          <div class="col-12 col-sm-6 col-md-3">
            <div class="input-group input-group-sm">
              <label class="input-group-text bg-white text-muted" for="limit-select">資料筆數</label>
              <select class="form-select border-start-0" id="limit-select" v-model="filterLimit">
                <option value="ALL">全部資料</option>
                <option value="3">前 3 大項目</option>
                <option value="5">前 5 大項目</option>
                <option value="10">前 10 大項目</option>
              </select>
            </div>
          </div>

          <div class="col-12 col-sm-6 col-md-3">
            <div class="input-group input-group-sm">
              <label class="input-group-text bg-white text-muted" for="dimension-select">統計維度</label>
              <select class="form-select border-start-0" id="dimension-select" v-model="filterDimension">
                <option value="CATEGORY">按工單大分類</option>
                <option value="SUBCATEGORY">按細項分類</option>
                <option value="STATUS">依工單狀態</option>
                <option value="CREATOR">依工單建立者</option>
                <option value="PRIORITY">依優先級 (Priority)</option>
                <option value="ENGINEER_KPI">依工程師處理 KPI (Engineer KPI)</option>
              </select>
            </div>
          </div>

          <!-- 日期區間篩選 (開始) -->
          <div class="col-12 col-sm-6 col-md-3">
            <div class="input-group input-group-sm">
              <label class="input-group-text bg-white text-muted" for="start-date-input">開始日期</label>
              <input 
                type="date" 
                class="form-control border-start-0" 
                id="start-date-input" 
                v-model="filterStartDate"
              />
            </div>
          </div>

          <!-- 日期區間篩選 (結束) -->
          <div class="col-12 col-sm-6 col-md-3">
            <div class="input-group input-group-sm">
              <label class="input-group-text bg-white text-muted" for="end-date-input">結束日期</label>
              <input 
                type="date" 
                class="form-control border-start-0" 
                id="end-date-input" 
                v-model="filterEndDate"
              />
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 4. 圓餅圖 + 圖例數據區塊 -->
    <div class="row g-4 mb-4">
      <!-- 左側/主要圓餅圖展示卡片 -->
      <div class="col-12 col-lg-8">
        <div class="card border-0 shadow-sm rounded-3 h-100 p-3 p-md-4 bg-white">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <h5 class="card-title h6 fw-bold text-dark mb-0">
              <i class="bi bi-pie-chart me-1 text-primary"></i> 
              {{ getChartTitle() }}
            </h5>
            <!-- <span class="badge bg-secondary-subtle text-secondary rounded-pill px-2.5">Chart.js 視覺化</span> -->
          </div>

          <div v-if="loading" class="d-flex flex-column align-items-center justify-content-center py-5 min-h-300">
            <div class="spinner-border text-primary" role="status"></div>
            <p class="text-muted small mt-2">正在載入報表數據...</p>
          </div>

          <div v-else-if="filteredReportData.length === 0" class="d-flex flex-column align-items-center justify-content-center py-5 min-h-300">
            <i class="bi bi-pie-chart text-muted opacity-50 fs-1 mb-2"></i>
            <p class="text-muted small mb-0">目前尚無統計數據</p>
          </div>

          <div v-else class="chart-wrapper min-h-350 d-flex align-items-center justify-content-center position-relative">
            <Pie :data="chartData" :options="chartOptions" />
          </div>
        </div>
      </div>

      <!-- 右側 Legend 圖例與百分比清單 -->
      <div class="col-12 col-lg-4">
        <div class="card border-0 shadow-sm rounded-3 h-100 p-3 p-md-4 bg-white">
          <h5 class="card-title h6 fw-bold text-dark mb-3 d-flex align-items-center justify-content-between">
            <span><i class="bi bi-list-stars me-1 text-primary"></i> 分類數據明細</span>
            <span class="text-muted small fw-normal">共 {{ filteredReportData.length }} 類</span>
          </h5>

          <div v-if="loading" class="py-4 text-center text-muted small">載入中...</div>

          <div v-else class="legend-list-wrap custom-scrollbar">
            <div 
              v-for="(item, index) in filteredReportData" 
              :key="getDisplayName(item)"
              class="legend-item d-flex align-items-center justify-content-between py-2 px-2.5 rounded-2 transition-all mb-1 hover-bg-light"
            >
              <div class="d-flex align-items-center gap-2 text-truncate me-2">
                <span 
                  class="color-badge rounded-circle flex-shrink-0" 
                  :style="{ backgroundColor: paletteColors[index % paletteColors.length] }"
                ></span>
                <div>
                  <span class="fw-medium text-dark small text-truncate d-block">{{ getDisplayName(item) }}</span>
                  <small v-if="filterDimension === 'ENGINEER_KPI'" class="text-primary text-xs d-block fw-semibold">
                    <i class="bi bi-clock-history me-1"></i>平均耗時: {{ item.avgDurationHours || 0 }} 小時 ({{ item.avgDurationMinutes || 0 }} 分鐘)
                  </small>
                </div>
              </div>

              <div class="d-flex align-items-center gap-3 flex-shrink-0">
                <span class="badge bg-light text-secondary border px-2 py-1 small font-monospace">
                  {{ item.count || item.completedCount || 0 }} 筆完工
                </span>
                <span class="fw-bold small text-dark font-monospace text-end" style="min-width: 50px;">
                  {{ calculatePercentage(item.count || item.completedCount || 0) }}%
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 5. 報修數量趨勢折線圖卡片 (放置於頁面最下方) -->
    <div class="card border-0 shadow-sm rounded-3 mb-4 p-3 p-md-4 bg-white">
      <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center mb-3 gap-2">
        <div>
          <h5 class="card-title h6 fw-bold text-dark mb-1 d-flex align-items-center gap-2">
            <i class="bi bi-graph-up text-primary"></i> 
            {{ trendMonth ? `${trendMonth} 月每日報修數量趨勢分析 (折線圖)` : '每月報修數量趨勢分析 (折線圖)' }}
          </h5>
          <p class="text-muted small mb-0">
            {{ trendMonth ? `追蹤 ${trendMonth} 月份中各日期的報修數量分布` : '依據工單建立時間追蹤每月報修單總數與趨勢走勢 (選取特定月份可查看每日明細)' }}
          </p>
        </div>

        <div class="d-flex align-items-center gap-2 flex-wrap">
          <!-- 折線圖樣式選擇 -->
          <div class="input-group input-group-sm" style="width: 155px;">
            <label class="input-group-text bg-light text-muted border-0 fw-medium">樣式</label>
            <select class="form-select bg-light border-0" v-model="lineStyle">
              <option value="SMOOTH">曲線折線圖</option>
              <option value="STRAIGHT">直線折線圖</option>
            </select>
          </div>

          <!-- 年份篩選下拉選單 -->
          <div class="input-group input-group-sm" style="width: 140px;">
            <label class="input-group-text bg-light text-muted border-0 fw-medium">年份</label>
            <select class="form-select bg-light border-0" v-model="trendYear">
              <option :value="null">全部年份</option>
              <option :value="2025">2025 年</option>
              <option :value="2026">2026 年</option>
            </select>
          </div>

          <!-- 月份篩選下拉選單 -->
          <div class="input-group input-group-sm" style="width: 140px;">
            <label class="input-group-text bg-light text-muted border-0 fw-medium">月份</label>
            <select class="form-select bg-light border-0" v-model="trendMonth">
              <option :value="null">全部月份</option>
              <option v-for="m in 12" :key="m" :value="m">{{ m }} 月</option>
            </select>
          </div>
        </div>
      </div>

      <div v-if="trendLoading" class="d-flex flex-column align-items-center justify-content-center py-5 min-h-300">
        <div class="spinner-border text-primary" role="status"></div>
        <p class="text-muted small mt-2">正在載入報修趨勢數據...</p>
      </div>

      <div v-else-if="trendRawData.length === 0" class="d-flex flex-column align-items-center justify-content-center py-5 min-h-300">
        <i class="bi bi-graph-up text-muted opacity-50 fs-1 mb-2"></i>
        <p class="text-muted small mb-0">目前尚無報修趨勢數據</p>
      </div>

      <div v-else class="chart-wrapper min-h-300 d-flex align-items-center justify-content-center position-relative">
        <Line :data="lineChartData" :options="lineChartOptions" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import {
  getCategoryReport,
  getSubCategoryReport,
  getStatusReport,
  getCreatorReport,
  getPriorityReport,
  getMonthlyReport,
  getDailyReport,
  getEngineerKpiReport
} from '@/api/report.js'
import { notify } from '@/plugins/notify.js'

// 匯入 Chart.js 與 vue-chartjs 組件
import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  ArcElement,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Filler
} from 'chart.js'
import { Pie, Line } from 'vue-chartjs'
import ChartDataLabels from 'chartjs-plugin-datalabels'

// 註冊 Chart.js 元件與外掛
ChartJS.register(
  Title,
  Tooltip,
  Legend,
  ArcElement,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Filler,
  ChartDataLabels
)

// ---- 1. State 狀態 ----
const loading = ref(false)
const categoryReportList = ref([]) // 後端 API 抓回的原始分類數據

// 下拉選單控制條件
const filterLimit = ref('ALL')          // 限制筆數 ('ALL' | '3' | '5' | '10')
const filterDimension = ref('CATEGORY') // 統計維度 ('CATEGORY' | 'SUBCATEGORY' | 'STATUS' | 'CREATOR' | 'PRIORITY')
const filterField = ref('CATEGORY_NAME')// 選擇欄位
const filterStartDate = ref('')         // 開始日期 YYYY-MM-DD
const filterEndDate = ref('')           // 結束日期 YYYY-MM-DD

// 調和色彩盤
const paletteColors = [
  '#60a5fa', // 天藍
  '#f97316', // 暖橘
  '#f43f5e', // 玫瑰紅
  '#c084fc', // 柔紫
  '#2dd4bf', // 青綠
  '#fbbf24', // 金黃
  '#818cf8', // 靛藍
  '#38bdf8', // 亮藍
  '#a3e635', // 嫩綠
  '#e879f9'  // 粉紫
]

// 工單狀態碼對照表
const formatStatus = (statusStr) => {
  if (!statusStr) return '未指定'
  const map = {
    PENDING_REVIEW: '待審核',
    DRAFT: '草稿',
    SUBMITTED: '已送出',
    ASSIGNED: '已派單',
    IN_PROGRESS: '處理中',
    PENDING_USER_ACCEPTANCE: '待使用者驗收',
    PENDING_ADMIN_ACCEPTANCE: '待管理員驗收',
    COMPLETED: '已完成',
    CLOSED: '已結案',
    CANCELLED: '已撤回',
    REJECTED: '已退單'
  }
  return map[statusStr] || statusStr
}

// 取得項目的顯示名稱（相容大分類、細項名稱、狀態、建立者、優先級、工程師）
const getDisplayName = (item) => {
  if (!item) return '未指定'
  if (filterDimension.value === 'STATUS') {
    return formatStatus(item.statusName || item.status)
  }
  return (
    item.engineerName ||
    item.categoryName ||
    item.subCategoryName ||
    item.creatorName ||
    item.priorityName ||
    item.statusName ||
    '未指定'
  )
}

// 取得當前維度的中文標題
const getDimensionTitle = () => {
  switch (filterDimension.value) {
    case 'SUBCATEGORY':
      return '細項分類'
    case 'STATUS':
      return '工單狀態'
    case 'CREATOR':
      return '工單建立者'
    case 'PRIORITY':
      return '工單優先級'
    case 'ENGINEER_KPI':
      return '工程師處理 KPI'
    default:
      return '報修大分類'
  }
}

// 取得當前最高項目卡片標題
const getTopCategoryCardTitle = () => {
  switch (filterDimension.value) {
    case 'STATUS':
      return '最多的狀態'
    case 'CREATOR':
      return '最多的建立者'
    case 'PRIORITY':
      return '最多的優先級'
    case 'ENGINEER_KPI':
      return '完成件數最高'
    case 'SUBCATEGORY':
      return '最高細項分類'
    default:
      return '最高報修項目'
  }
}

const getChartTitle = () => {
  if (filterDimension.value === 'ENGINEER_KPI') {
    return '完成工單之工程師占比'
  }
  return `工單${getDimensionTitle()}占比圖`
}

// ---- 2. Computed 計算屬性 ----
const filteredReportData = computed(() => {
  let list = [...categoryReportList.value]

  // 按數量高到低排序
  list.sort((a, b) => ((b.count || b.completedCount || 0) - (a.count || a.completedCount || 0)))

  if (filterLimit.value !== 'ALL') {
    const limit = parseInt(filterLimit.value, 10)
    list = list.slice(0, limit)
  }

  return list
})

// 計算總工單筆數
const totalCount = computed(() => {
  return categoryReportList.value.reduce((sum, item) => sum + (item.count || item.completedCount || 0), 0)
})

// 計算最高報修項目
const topCategory = computed(() => {
  if (categoryReportList.value.length === 0) return null
  const sorted = [...categoryReportList.value].sort((a, b) => ((b.count || b.completedCount || 0) - (a.count || a.completedCount || 0)))
  return sorted[0]
})

// 計算最高占比百分比
const topCategoryPercentage = computed(() => {
  if (!topCategory.value || totalCount.value === 0) return '0.0'
  const count = topCategory.value.count || topCategory.value.completedCount || 0
  return ((count / totalCount.value) * 100).toFixed(1)
})

// 平均每個分類筆數 (保留小數點第一位)
const avgCount = computed(() => {
  if (categoryReportList.value.length === 0) return '0.0'
  return (totalCount.value / categoryReportList.value.length).toFixed(1)
})

// 計算特定數量的百分比占比
const calculatePercentage = (count) => {
  if (!totalCount.value || totalCount.value === 0) return '0.0'
  return (((count || 0) / totalCount.value) * 100).toFixed(1)
}

// ---- 3. Chart.js 圓餅圖數據結構與資料轉置 ----
const chartData = computed(() => {
  // 1. 從後端數據中萃取所有項目的名稱標籤 (例如：['環境設施', '資訊系統', '儀器設備'])
  const labels = filteredReportData.value.map(item => getDisplayName(item))
  
  // 2. 從後端數據中萃取對應的工單筆數數字 (相容大分類 count 與工程師 completedCount)
  const counts = filteredReportData.value.map(item => item.count || item.completedCount || 0)
  
  // 3. 根據標籤數量截取對應數量的顏色
  const colors = paletteColors.slice(0, labels.length)

  // 4. 當只有 1 個項目 (100%) 時，動態將分隔邊界設為 0，避免在圓餅圖上方留下白色的縫隙切線
  const isSingleSlice = labels.length <= 1 || counts.filter(c => c > 0).length <= 1

  // 5. 組合成 Chart.js 要求的標準資料格式，傳給 HTML 的 <Pie :data="chartData" /> 繪製
  return {
    labels,
    datasets: [
      {
        backgroundColor: colors,                          // 各扇形區塊背景顏色
        hoverBackgroundColor: colors,                     // 滑鼠移入時的懸浮背景顏色
        borderWidth: isSingleSlice ? 0 : 2,               // 100% 時無邊框，多區塊時為 2px 白邊
        borderColor: isSingleSlice ? 'transparent' : '#ffffff', // 100% 時透明，多區塊時為白色分割線
        data: counts                                      // 填入真正的工單筆數數字
      }
    ]
  }
})

// Chart.js 設定選項
const chartOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      display: false
    },
    tooltip: {
      callbacks: {
        label: (context) => {
          const val = context.raw || 0
          const pct = calculatePercentage(val)
          return `${context.label}: ${val} 筆 (${pct}%)`
        }
      }
    },
    datalabels: {
      color: '#ffffff',
      font: {
        weight: 'bold',
        size: 11
      },
      formatter: (value) => {
        const pct = calculatePercentage(value)
        if (parseFloat(pct) < 3.0) return ''
        return `${pct}%`
      },
      textShadowColor: 'rgba(0, 0, 0, 0.4)',
      textShadowBlur: 4,
      anchor: 'center',
      align: 'center'
    }
  }
}))

// ---- 4. Actions 方法 ----
const loadData = async () => {
  loading.value = true
  try {
    const params = {}
    if (filterStartDate.value) params.startDate = filterStartDate.value
    if (filterEndDate.value) params.endDate = filterEndDate.value

    let data = []
    if (filterDimension.value === 'SUBCATEGORY') {
      data = await getSubCategoryReport(params)
    } else if (filterDimension.value === 'STATUS') {
      data = await getStatusReport(params)
    } else if (filterDimension.value === 'CREATOR') {
      data = await getCreatorReport(params)
    } else if (filterDimension.value === 'PRIORITY') {
      data = await getPriorityReport(params)
    } else if (filterDimension.value === 'ENGINEER_KPI') {
      data = await getEngineerKpiReport(params)
    } else {
      data = await getCategoryReport(params)
    }
    categoryReportList.value = Array.isArray(data) ? data : []
  } catch (err) {
    console.error('載入統計報表失敗：', err)
    notify.error('載入報表數據失敗，請稍後再試')
  } finally {
    loading.value = false
  }
}

// 當維度或日期過濾區間變化時，自動重新向後端請求資料
watch([filterDimension, filterStartDate, filterEndDate], () => {
  loadData()
})

const applyFilters = () => {
  loadData()
  notify.success('已更新報表套用條件')
}

const resetFilters = () => {
  filterLimit.value = 'ALL'
  filterDimension.value = 'CATEGORY'
  filterField.value = 'CATEGORY_NAME'
  filterStartDate.value = ''
  filterEndDate.value = ''
  loadData()
  notify.info('已重置篩選條件')
}

const exportReport = () => {
  if (!filteredReportData.value || filteredReportData.value.length === 0) {
    notify.warning('目前無可匯出的報表數據')
    return
  }

  try {
    const title = getDimensionTitle()
    const todayStr = new Date().toISOString().split('T')[0]

    // 1. 建立 CSV 標題與資料陣列
    const csvRows = []
    
    // 報表元數據 Header
    csvRows.push([`"工單統計報表 - ${title}"`])
    csvRows.push([`"匯出時間"`, `"${new Date().toLocaleString()}"`])
    if (filterStartDate.value || filterEndDate.value) {
      csvRows.push([`"篩選日期區間"`, `"${filterStartDate.value || '不限'} ~ ${filterEndDate.value || '不限'}"`])
    }
    csvRows.push([]) // 空列隔開

    // 表頭與資料列
    if (filterDimension.value === 'ENGINEER_KPI') {
      csvRows.push(['"項次"', '"工程師姓名"', '"完工數量(筆)"', '"平均處理時數(小時)"', '"平均處理時間(分鐘)"'])
      filteredReportData.value.forEach((item, index) => {
        const name = getDisplayName(item)
        const count = item.completedCount || item.count || 0
        const hours = item.avgDurationHours || 0
        const minutes = item.avgDurationMinutes || 0
        csvRows.push([index + 1, `"${name}"`, count, hours, minutes])
      })
    } else {
      csvRows.push(['"項次"', '"名稱"', '"工單數量(筆)"', '"百分比占比(%)"'])
      filteredReportData.value.forEach((item, index) => {
        const name = getDisplayName(item)
        const count = item.count || 0
        const pct = calculatePercentage(count)
        csvRows.push([index + 1, `"${name}"`, count, `"${pct}%"`])
      })
    }

    // 2. 將陣列組合為 CSV 格式字串
    const csvString = csvRows.map(row => row.join(',')).join('\n')

    // 3. 加入 UTF-8 BOM ('\uFEFF')，確保 Microsoft Excel 開啟時中文無亂碼
    const blob = new Blob(['\uFEFF' + csvString], { type: 'text/csv;charset=utf-8;' })

    // 4. 建立隱藏 HTML 下載連結並觸發點擊
    const link = document.createElement('a')
    const url = URL.createObjectURL(blob)
    link.setAttribute('href', url)
    link.setAttribute('download', `工單統計報表_${title}_${todayStr}.csv`)
    link.style.visibility = 'hidden'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)

    notify.success(`已成功下載「${title}」CSV 報表檔案`)
  } catch (err) {
    console.error('匯出報表失敗：', err)
    notify.error('匯出報表失敗，請稍後再試')
  }
}

// ---- 5. 折線圖趨勢狀態與邏輯 ----
const lineStyle = ref('SMOOTH')  // 'SMOOTH' (平滑曲線) | 'STRAIGHT' (直線折線)
const trendYear = ref(null)      // null (全部) | 2025 | 2026
const trendMonth = ref(null)     // null (全部月份 - 每月統計) | 1 ~ 12 (特定月份 - 每日統計)
const trendLoading = ref(false)
const trendRawData = ref([])

// 從後端 API 拉取月/日統計數據
const loadTrendData = async () => {
  trendLoading.value = true
  try {
    const params = {}
    if (trendYear.value != null) params.year = trendYear.value
    if (trendMonth.value != null) params.month = trendMonth.value

    if (trendMonth.value != null) {
      // 選擇特定月份 ➡️ 呼叫每日統計 API
      trendRawData.value = (await getDailyReport(params)) || []
    } else {
      // 未選擇月份 (全部月份) ➡️ 呼叫每月統計 API
      trendRawData.value = (await getMonthlyReport(params)) || []
    }
  } catch (err) {
    console.error('載入報修趨勢失敗：', err)
    notify.error('載入報修趨勢失敗')
  } finally {
    trendLoading.value = false
  }
}

// 監聽年份或月份變化，自動重新載入
watch([trendYear, trendMonth], () => {
  loadTrendData()
})

// 折線圖 Chart.js 數據轉置
const lineChartData = computed(() => {
  const isDaily = trendMonth.value != null

  const labels = trendRawData.value.map(item => {
    if (isDaily) {
      // 每日模式：顯示 "3/1" 或 "2025/3/1"
      return trendYear.value
        ? `${item.month}/${item.day}`
        : `${item.year}/${item.month}/${item.day}`
    }
    // 每月模式：顯示 "1 月" 或 "2025-01"
    return trendYear.value ? `${item.month} 月` : `${item.year}-${String(item.month).padStart(2, '0')}`
  })

  const counts = trendRawData.value.map(item => item.count || 0)

  return {
    labels,
    datasets: [
      {
        label: isDaily ? `${trendMonth.value} 月每日報修工單數` : '每月報修工單數',
        backgroundColor: lineStyle.value === 'SMOOTH' ? 'rgba(47, 111, 237, 0.12)' : 'transparent',
        borderColor: '#2F6FED',
        pointBackgroundColor: '#2F6FED',
        pointBorderColor: '#ffffff',
        pointBorderWidth: 2,
        pointRadius: 5,
        pointHoverRadius: 7,
        borderWidth: 3,
        tension: lineStyle.value === 'SMOOTH' ? 0.35 : 0, // SMOOTH 為平滑曲線，STRAIGHT 為直切折線
        fill: lineStyle.value === 'SMOOTH',
        data: counts
      }
    ]
  }
})

// 折線圖 Chart.js 選項設定
const lineChartOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      display: true,
      position: 'top'
    },
    tooltip: {
      callbacks: {
        label: (context) => ` 報修數量: ${context.raw} 筆`
      }
    },
    datalabels: {
      display: true,
      align: 'top',
      anchor: 'end',
      offset: 4,
      color: '#2F6FED',
      font: {
        weight: 'bold',
        size: 15
      },
      formatter: (val) => `${val}`
    }
  },
  scales: {
    y: {
      beginAtZero: true,
      ticks: {
        precision: 0
      }
    }
  }
}))

// 頁面載入時自動拉取後端 API 數據
onMounted(() => {
  loadData()
  loadTrendData()
})
</script>

<style scoped>
.ticket-stats-container {
  max-width: 1320px;
  margin: 0 auto;
}

.color-badge {
  width: 12px;
  height: 12px;
  display: inline-block;
}

.min-h-350 {
  min-height: 350px;
}

.min-h-300 {
  min-height: 300px;
}

.legend-list-wrap {
  max-height: 360px;
  overflow-y: auto;
}

.hover-bg-light:hover {
  background-color: #f8fafc;
}

.hover-lift {
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.hover-lift:hover {
  transform: translateY(-2px);
  box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.08) !important;
}

.shadow-2xs {
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
}

.custom-scrollbar::-webkit-scrollbar {
  width: 5px;
}

.custom-scrollbar::-webkit-scrollbar-track {
  background: #f1f5f9;
  border-radius: 4px;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 4px;
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

.spin {
  animation: spin-anim 1s linear infinite;
}

@keyframes spin-anim {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
