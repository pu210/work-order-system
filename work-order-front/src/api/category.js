import api from '@/plugins/axios.js'

// RepairCategoryController / SubCategoryController 直接回傳陣列，不包在 ApiResponse 裡
export function getRepairCategories() {
  return api.get('/api/repair-categories').then((res) => res.data)
}

export function getSubCategories() {
  return api.get('/api/sub-categories').then((res) => res.data)
}
