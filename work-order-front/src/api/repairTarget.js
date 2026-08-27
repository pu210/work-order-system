import api from '@/plugins/axios.js'

// 取得設備目標列表（支援 keyword 參數，沒傳就是全部）
export function getRepairTargets(keyword) {
    return api.get('/api/repair-targets', { params: { keyword } }).then((res) => res.data)
}

// 新增設備目標
export function createRepairTarget(payload) {
    return api.post('/api/repair-targets', payload).then((res) => res.data)
}

// 更新設備目標
export function updateRepairTarget(id, payload) {
    return api.put(`/api/repair-targets/${id}`, payload).then((res) => res.data)
}

// 更新設備目標狀態
export function updateRepairTargetStatus(id, status) {
    return api.patch(`/api/repair-targets/${id}/status`, null, { params: { status } }).then((res) => res.data)
}