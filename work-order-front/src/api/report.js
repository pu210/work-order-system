import api from '@/plugins/axios.js'

// 1. 取得大分類統計報表
export function getCategoryReport(params) {
  return api.get('/api/reports/categories', { params }).then((res) => res.data.data)
}

// 2. 取得細項分類統計報表
export function getSubCategoryReport(params) {
  return api.get('/api/reports/subcategories', { params }).then((res) => res.data.data)
}

// 3. 依狀態統計報表
export function getStatusReport(params) {
  return api.get('/api/reports/statuses', { params }).then((res) => res.data.data)
}

// 4. 依工單建立者統計報表
export function getCreatorReport(params) {
  return api.get('/api/reports/creators', { params }).then((res) => res.data.data)
}

// 5. 依優先級統計報表
export function getPriorityReport(params) {
  return api.get('/api/reports/priorities', { params }).then((res) => res.data.data)
}

// 5.1 依設備型號統計報表
export function getEquipmentModelReport(params) {
  return api.get('/api/reports/equipment-models', { params }).then((res) => res.data.data)
}

// 6. 依月份統計報表 (折線圖用)
export function getMonthlyReport(params) {
  return api.get('/api/reports/monthly', { params }).then((res) => res.data.data)
}

// 7. 依每日統計報表 (折線圖用)
export function getDailyReport(params) {
  return api.get('/api/reports/daily', { params }).then((res) => res.data.data)
}

// 測試用 API：列出目前資料庫內的所有工單報表
export function getAllWorkOrdersReport() {
  return api.get('/api/reports/test-work-orders').then((res) => res.data.data)
}

// 8. 依工程師處理 KPI 統計報表
export function getEngineerKpiReport(params) {
  return api.get('/api/reports/engineer-kpi', { params }).then((res) => res.data.data)
}
