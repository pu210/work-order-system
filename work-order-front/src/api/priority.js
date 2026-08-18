import api from '@/plugins/axios.js'

// --- 優先級 ---
export function getPriorities(keyword) {
    return api.get('/api/priorities', { params: { keyword } }).then((res) => res.data.data)
}

export function createPriority(payload) {
    return api.post('/api/priorities', payload).then((res) => res.data.data)
}

export function updatePriority(id, payload) {
    return api.put(`/api/priorities/${id}`, payload).then((res) => res.data.data)
}

export function updatePriorityStatus(id, status) {
    return api.patch(`/api/priorities/${id}/status`, null, { params: { status } }).then((res) => res.data.data)
}