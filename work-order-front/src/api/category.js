import api from '@/plugins/axios.js'

// --- 報修大類 ---
export function getRepairCategories(keyword) {
  return api.get('/api/repair-categories', { params: { keyword } }).then((res) => res.data)
}


export function getActiveRepairCategories() {
  return api.get('/api/repair-categories/active').then((res) => res.data)
}
// B 模組用：新增工單頁下拉選單，只拿啟用中的大類，跟上面給設定頁用的 getRepairCategories() 分開。
// export function getActiveRepairCategories() {
//   return api.get('/api/repair-categories/active').then((res) => res.data.data)
// }

// B 模組用：需要「全部大類」的地方（例如工單列表篩選下拉選單）改打這支，不要打 getRepairCategories()。
export function getAllRepairCategoriesWithPriority() {
  return api.get('/api/repair-categories/all-with-priority').then((res) => res.data.data)
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

// --- 報修細項 ---
export function getSubCategories(keyword) {
  return api.get('/api/sub-categories', { params: { keyword } }).then((res) => res.data)
}

// B 模組用：新增工單頁下拉選單，只拿啟用中的細項，跟上面給設定頁用的 getSubCategories() 分開。
// 這支後端包了 ApiResponse，要多解一層 res.data.data，跟這支檔案其他函式的解包方式不一樣
export function getActiveSubCategories() {
  return api.get('/api/sub-categories/active').then((res) => res.data.data)
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