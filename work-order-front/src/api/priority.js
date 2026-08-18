import api from '@/plugins/axios.js'

// PriorityController 直接回傳陣列，不包在 ApiResponse 裡
export function getPriorities() {
  return api.get('/api/priorities').then((res) => res.data)
}
