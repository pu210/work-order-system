import api from '@/plugins/axios.js'

// WorkOrderController 回應包在 ApiResponse { code, message, data } 裡，這裡直接解出 data 方便呼叫端使用
export function createWorkOrder(payload) {
  return api.post('/api/work-orders', payload).then((res) => res.data.data)
}

export function getWorkOrderById(id) {
  return api.get(`/api/work-orders/${id}`).then((res) => res.data.data)
}

export function getWorkOrderList(params) {
  return api.get('/api/work-orders', { params }).then((res) => res.data.data)
}

export function getMySubmissions(params) {
  return api.get('/api/work-orders/my-submissions', { params }).then((res) => res.data.data)
}

export function acceptWorkOrder(id, userId, feedback) {
  return api
    .post(`/api/work-orders/${id}/usercheck/accept`, { userId, feedback })
    .then((res) => res.data.data)
}
