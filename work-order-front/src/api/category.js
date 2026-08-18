import api from '@/plugins/axios.js'

// --- 報修大類 ---
export function getRepairCategories(keyword) {
  return api.get('/api/repair-categories', { params: { keyword } }).then((res) => res.data.data)
}

export function createRepairCategory(payload) {
  return api.post('/api/repair-categories', payload).then((res) => res.data.data)
}

export function updateRepairCategory(id, payload) {
  return api.put(`/api/repair-categories/${id}`, payload).then((res) => res.data.data)
}

export function updateRepairCategoryStatus(id, status) {
  return api.patch(`/api/repair-categories/${id}/status`, null, { params: { status } }).then((res) => res.data.data)
}

// --- 報修子類 ---
export function getSubCategories(keyword) {
  return api.get('/api/sub-categories', { params: { keyword } }).then((res) => res.data.data)
}

export function createSubCategory(payload) {
  return api.post('/api/sub-categories', payload).then((res) => res.data.data)
}

// ...如果後面還有 updateSubCategory / updateSubCategoryStatus 等函式，麻煩貼給我，我一併補上