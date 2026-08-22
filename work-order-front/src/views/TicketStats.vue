<template>
  <div class="ticket-stats-container container-fluid py-4 px-3 px-md-4">
    <!-- 1. 頁面頂部標題列 -->
    <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center mb-4 gap-3">
      <div>
        <h2 class="h4 fw-bold text-dark mb-1 d-flex align-items-center gap-2">
          <i class="bi bi-pie-chart-fill text-primary"></i> 工單統計報表中心
        </h2>
        <p class="text-muted small mb-0">針對全系統工單進行多維度視覺化數據分析與分類占比統計</p>
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
                {{ filterDimension === 'SUBCATEGORY' ? '細項分類總數' : '報修大分類總數' }}
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
              <div class="text-muted small mb-1 fw-medium">最高報修項目</div>
              <div class="h5 fw-bold mb-0 text-warning text-truncate" style="max-width: 140px;">
                {{ topCategory ? getDisplayName(topCategory) : '無資料' }}
              </div>
              <div class="small text-muted" v-if="topCategory">
                {{ topCategory.count }} 筆 ({{ topCategoryPercentage }}%)
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
              <div class="text-muted small mb-1 fw-medium">平均分類工單數</div>
              <div class="h3 fw-bold mb-0 text-info">{{ avgCount }} <span class="fs-6 text-muted fw-normal">筆/類</span></div>
            </div>
            <div class="icon-avatar bg-info-subtle text-info rounded-circle p-3">
              <i class="bi bi-calculator-fill fs-4"></i>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 3. 客製化條件篩選控制列 (包含大分類 / 細項分類 快速頁籤) -->
    <div class="card border-0 shadow-sm rounded-3 mb-4 overflow-hidden">
      <div class="card-header bg-light bg-opacity-75 py-2.5 px-3 border-0 d-flex align-items-center justify-content-between flex-wrap gap-2">
        <div class="d-flex align-items-center gap-3">
          <div class="d-flex align-items-center gap-1">
            <span class="text-muted small fw-medium">資料來源：</span>
            <span class="badge bg-primary-subtle text-primary border border-primary-subtle rounded-pill px-2.5 py-1 fw-normal">
              <i class="bi bi-database me-1"></i>設備報修單
            </span>
          </div>

          <!-- 快速分類切換 Tab 按鈕 -->
          <div class="btn-group rounded-pill p-1 bg-white border shadow-2xs">
            <button 
              type="button"
              class="btn btn-sm rounded-pill px-3 py-1 transition-all" 
              :class="filterDimension === 'CATEGORY' ? 'btn-primary shadow-xs fw-bold' : 'btn-light text-secondary border-0'"
              @click="filterDimension = 'CATEGORY'"
            >
              <i class="bi bi-grid-fill me-1"></i> 大分類統計
            </button>
            <button 
              type="button"
              class="btn btn-sm rounded-pill px-3 py-1 transition-all" 
              :class="filterDimension === 'SUBCATEGORY' ? 'btn-primary shadow-xs fw-bold' : 'btn-light text-secondary border-0'"
              @click="filterDimension = 'SUBCATEGORY'"
            >
              <i class="bi bi-diagram-3-fill me-1"></i> 細項分類統計
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
          <div class="col-12 col-sm-4 col-md-3">
            <div class="input-group input-group-sm">
              <label class="input-group-text bg-white text-muted" for="limit-select">資料筆數</label>
              <select class="form-select border-start-0" id="limit-select" v-model="filterLimit">
                <option value="ALL">全部資料</option>
                <option value="3">前 3 大分類</option>
                <option value="5">前 5 大分類</option>
                <option value="10">前 10 大分類</option>
              </select>
            </div>
          </div>

          <div class="col-12 col-sm-4 col-md-3">
            <div class="input-group input-group-sm">
              <label class="input-group-text bg-white text-muted" for="dimension-select">統計維度</label>
              <select class="form-select border-start-0" id="dimension-select" v-model="filterDimension">
                <option value="CATEGORY">按工單大分類</option>
                <option value="SUBCATEGORY">按細項分類</option>
              </select>
            </div>
          </div>

          <div class="col-12 col-sm-4 col-md-3">
            <div class="input-group input-group-sm">
              <label class="input-group-text bg-white text-muted" for="field-select">選擇分類欄位</label>
              <select class="form-select border-start-0" id="field-select" v-model="filterField">
                <option v-if="filterDimension === 'CATEGORY'" value="CATEGORY_NAME">報修大分類 (Category)</option>
                <option v-else value="SUB_CATEGORY_NAME">細項分類 (SubCategory)</option>
              </select>
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
              {{ filterDimension === 'SUBCATEGORY' ? '工單細項分類占比圖' : '工單報修大分類占比圖' }}
            </h5>
            <span class="badge bg-secondary-subtle text-secondary rounded-pill px-2.5">Chart.js 視覺化</span>
          </div>

          <div v-if="loading" class="d-flex flex-column align-items-center justify-content-center py-5 min-h-300">
            <div class="spinner-border text-primary" role="status"></div>
            <p class="text-muted small mt-2">正在載入報表數據...</p>
          </div>

          <div v-else-if="filteredReportData.length === 0" class="d-flex flex-column align-items-center justify-content-center py-5 min-h-300">
            <i class="bi bi-pie-chart text-muted opacity-50 fs-1 mb-2"></i>
            <p class="text-muted small mb-0">目前尚無分類統計數據</p>
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
                <span class="fw-medium text-dark small text-truncate">{{ getDisplayName(item) }}</span>
              </div>

              <div class="d-flex align-items-center gap-3 flex-shrink-0">
                <span class="badge bg-light text-secondary border px-2 py-1 small font-monospace">
                  {{ item.count }} 筆
                </span>
                <span class="fw-bold small text-dark font-monospace text-end" style="min-width: 50px;">
                  {{ calculatePercentage(item.count) }}%
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 5. 底部表格細節清單 -->
    <div class="card border-0 shadow-sm rounded-3 overflow-hidden">
      <div class="card-header bg-white py-3 px-4 border-0 d-flex align-items-center justify-content-between">
        <h5 class="card-title h6 fw-bold text-dark mb-0">
          <i class="bi bi-table me-1 text-primary"></i> 
          {{ filterDimension === 'SUBCATEGORY' ? '細項分類統計詳細數據表' : '大分類統計詳細數據表' }}
        </h5>
      </div>

      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="table-light">
            <tr>
              <th class="ps-4" style="width: 80px;">#</th>
              <th>分類名稱</th>
              <th>工單數量</th>
              <th>百分比占比</th>
              <th class="pe-4" style="width: 30%;">占比視覺進度條</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="5" class="text-center py-4 text-muted">資料載入中...</td>
            </tr>
            <tr v-else-if="filteredReportData.length === 0">
              <td colspan="5" class="text-center py-4 text-muted">暫無數據</td>
            </tr>
            <tr v-else v-for="(item, index) in filteredReportData" :key="getDisplayName(item)">
              <td class="ps-4 fw-medium text-muted">{{ index + 1 }}</td>
              <td>
                <div class="d-flex align-items-center gap-2">
                  <span 
                    class="color-badge rounded-circle" 
                    :style="{ backgroundColor: paletteColors[index % paletteColors.length] }"
                  ></span>
                  <span class="fw-semibold text-dark">{{ getDisplayName(item) }}</span>
                </div>
              </td>
              <td class="fw-bold text-dark font-monospace">{{ item.count }} 筆</td>
              <td class="fw-bold text-primary font-monospace">{{ calculatePercentage(item.count) }}%</td>
              <td class="pe-4">
                <div class="progress rounded-pill" style="height: 8px;">
                  <div 
                    class="progress-bar rounded-pill transition-all" 
                    role="progressbar" 
                    :style="{ 
                      width: calculatePercentage(item.count) + '%',
                      backgroundColor: paletteColors[index % paletteColors.length]
                    }"
                  ></div>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { getCategoryReport, getSubCategoryReport } from '@/api/report.js'
import { notify } from '@/plugins/notify.js'

// 匯入 Chart.js 與 vue-chartjs 組件
import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  ArcElement,
  CategoryScale
} from 'chart.js'
import { Pie } from 'vue-chartjs'
import ChartDataLabels from 'chartjs-plugin-datalabels'

// 註冊 Chart.js 元件與外掛
ChartJS.register(Title, Tooltip, Legend, ArcElement, CategoryScale, ChartDataLabels)

// ---- 1. State 狀態 ----
const loading = ref(false)
const categoryReportList = ref([]) // 後端 API 抓回的原始分類數據

// 下拉選單控制條件
const filterLimit = ref('ALL')          // 限制筆數 ('ALL' | '3' | '5' | '10')
const filterDimension = ref('CATEGORY') // 統計維度 ('CATEGORY' | 'SUBCATEGORY')
const filterField = ref('CATEGORY_NAME')// 選擇欄位

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

// 取得項目的顯示名稱（相容大分類 categoryName 與細項名稱 subCategoryName）
const getDisplayName = (item) => {
  return item.categoryName || item.subCategoryName || '未分類'
}

// ---- 2. Computed 計算屬性 ----
const filteredReportData = computed(() => {
  let list = [...categoryReportList.value]

  // 按數量高到低排序
  list.sort((a, b) => (b.count || 0) - (a.count || 0))

  if (filterLimit.value !== 'ALL') {
    const limit = parseInt(filterLimit.value, 10)
    list = list.slice(0, limit)
  }

  return list
})

// 計算總工單筆數
const totalCount = computed(() => {
  return categoryReportList.value.reduce((sum, item) => sum + (item.count || 0), 0)
})

// 計算最高報修項目
const topCategory = computed(() => {
  if (categoryReportList.value.length === 0) return null
  const sorted = [...categoryReportList.value].sort((a, b) => (b.count || 0) - (a.count || 0))
  return sorted[0]
})

// 計算最高占比百分比
const topCategoryPercentage = computed(() => {
  if (!topCategory.value || totalCount.value === 0) return '0.0'
  return (((topCategory.value.count || 0) / totalCount.value) * 100).toFixed(1)
})

// 平均每個分類筆數
const avgCount = computed(() => {
  if (categoryReportList.value.length === 0) return 0
  return Math.round(totalCount.value / categoryReportList.value.length)
})

// 計算特定數量的百分比占比
const calculatePercentage = (count) => {
  if (!totalCount.value || totalCount.value === 0) return '0.0'
  return (((count || 0) / totalCount.value) * 100).toFixed(1)
}

// ---- 3. Chart.js 圓餅圖數據結構 ----
const chartData = computed(() => {
  const labels = filteredReportData.value.map(item => getDisplayName(item))
  const counts = filteredReportData.value.map(item => item.count || 0)
  const colors = paletteColors.slice(0, labels.length)

  return {
    labels,
    datasets: [
      {
        backgroundColor: colors,
        hoverBackgroundColor: colors,
        borderWidth: 2,
        borderColor: '#ffffff',
        data: counts
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
    let data = []
    if (filterDimension.value === 'SUBCATEGORY') {
      data = await getSubCategoryReport()
    } else {
      data = await getCategoryReport()
    }
    categoryReportList.value = Array.isArray(data) ? data : []
  } catch (err) {
    console.error('載入統計報表失敗：', err)
    notify.error('載入報表數據失敗，請稍後再試')
  } finally {
    loading.value = false
  }
}

// 當維度切換 (按大分類 vs 按細項分類) 時，自動重新向後端請求資料
watch(filterDimension, (newDim) => {
  if (newDim === 'SUBCATEGORY') {
    filterField.value = 'SUB_CATEGORY_NAME'
  } else {
    filterField.value = 'CATEGORY_NAME'
  }
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
  loadData()
  notify.info('已重置篩選條件')
}

const exportReport = () => {
  notify.success('報表下載指令已傳送（模擬匯出 CSV/PDF）')
}

// 頁面載入時自動拉取後端 API 數據
onMounted(() => {
  loadData()
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
