import api from '@/plugins/axios.js'

// 取得優先級列表（管理頁面用）
export function getPriorities(keyword) {
    return api.get('/api/priorities', { params: { keyword } }).then((res) => res.data.data)
}

// 專門給下拉選單使用的「啟用中」列表
export function getActivePriorities() {
    return api.get('/api/priorities/active').then((res) => res.data.data)
}

// B 模組用：工單列表篩選下拉選單，只拿啟用中的優先級，跟上面的 getActivePriorities() 分開。
// 核心邏輯常常在異動，所以改自己打一隻。
export function getActivePrioritiesForB() {
    return api.get('/api/priorities/active-for-b').then((res) => res.data.data)
}

// 新增優先級
export function createPriority(payload) {
    return api.post('/api/priorities', payload).then((res) => res.data)
}

// 更新優先級
export function updatePriority(id, payload) {
    return api.put(`/api/priorities/${id}`, payload).then((res) => res.data)
}

// 更新優先級狀態
export function updatePriorityStatus(id, status) {
    return api.patch(`/api/priorities/${id}/status`, null, { params: { status } }).then((res) => res.data)
}