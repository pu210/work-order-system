import api from '@/plugins/axios.js'

// 取得優先級列表
export function getPriorities(keyword) {
    return api.get('/api/priorities', { params: { keyword } }).then((res) => res.data)
}

// 新增優先級
export function createPriority(payload) {
    return api.post('/api/priorities', payload).then((res) => res.data)
}

// 更新優先級
export function updatePriority(id, payload) {
    return api.put(`/api/priorities/${id}`, payload).then((res) => res.data)
}

// 更新優先級狀態（補上這行就能解決錯誤）
export function updatePriorityStatus(id, status) {
    return api.patch(`/api/priorities/${id}/status`, null, { params: { status } }).then((res) => res.data)
}