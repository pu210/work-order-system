import api from '@/plugins/axios.js'

// 1. 取得大分類統計報表
export function getCategoryReport() {
  return api.get('/api/reports/categories').then((res) => res.data.data)
}

// 2. 取得細項分類統計報表
export function getSubCategoryReport() {
  return api.get('/api/reports/subcategories').then((res) => res.data.data)
}

// 3. 依狀態統計報表
export function getStatusReport() {
  return api.get('/api/reports/statuses').then((res) => res.data.data)
}

// 4. 依工單建立者統計報表
export function getCreatorReport() {
  return api.get('/api/reports/creators').then((res) => res.data.data)
}

// 5. 依優先級統計報表
export function getPriorityReport() {
  return api.get('/api/reports/priorities').then((res) => res.data.data)
}

// 測試用 API：列出目前資料庫內的所有工單報表
export function getAllWorkOrdersReport() {
  return api.get('/api/reports/test-work-orders').then((res) => res.data.data)
}
