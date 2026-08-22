import api from '@/plugins/axios.js'

// 取得大分類統計報表
export function getCategoryReport() {
  return api.get('/api/reports/categories').then((res) => res.data.data)
}

// 取得細項分類統計報表
export function getSubCategoryReport() {
  return api.get('/api/reports/subcategories').then((res) => res.data.data)
}

// 測試用 API：列出目前資料庫內的所有工單報表
export function getAllWorkOrdersReport() {
  return api.get('/api/reports/test-work-orders').then((res) => res.data.data)
}
