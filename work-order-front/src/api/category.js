import api from '@/plugins/axios.js'

// --- 報修大類 ---
export function getRepairCategories(keyword) {
  return api.get('/api/repair-categories', { params: { keyword } }).then((res) => res.data)
}

export function createRepairCategory(payload) {
  return api.post('/api/repair-categories', payload).then((res) => res.data)
}

export function updateRepairCategory(id, payload) {
  return api.put(`/api/repair-categories/${id}`, payload).then((res) => res.data)
}

// 確保這裡有加上 export，並且名稱正確
export function updateRepairCategoryStatus(id, status) {
  return api.patch(`/api/repair-categories/${id}/status`, null, { params: { status } }).then((res) => res.data)
}

// --- 報修子類 ---
export function getSubCategories(keyword) {
  return api.get('/api/sub-categories', { params: { keyword } }).then((res) => res.data)
}

export function createSubCategory(payload) {
  return api.post('/api/sub-categories', payload).then((res) => res.data)
}
export function updateSubCategoryStatus(id, status) {
  return api.patch(`/api/sub-categories/${id}/status`, null, { params: { status } }).then((res) => res.data)
}
export function updateSubCategory(id, payload) {
  return api.put(`/api/sub-categories/${id}`, payload).then((res) => res.data)
}