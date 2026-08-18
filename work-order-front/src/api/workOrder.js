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

// 圖片限定、單檔 10MB，後端欄位名固定叫 files；不手動設 Content-Type，讓瀏覽器自動帶 boundary
export function uploadAttachments(workOrderId, files) {
  const formData = new FormData()
  files.forEach((file) => formData.append('files', file))
  return api
    .post(`/api/work-orders/${workOrderId}/attachments`, formData)
    .then((res) => res.data.data)
}

export function getAttachments(workOrderId) {
  return api.get(`/api/work-orders/${workOrderId}/attachments`).then((res) => res.data.data)
}

// 預覽 API 不包 ApiResponse，直接回檔案 binary；<img> 原生無法帶 Authorization header，改用 blob + object URL
export function getAttachmentPreview(attachmentId) {
  return api
    .get(`/api/work-orders/attachments/${attachmentId}/view`, { responseType: 'blob' })
    .then((res) => res.data)
}

export function deleteAttachment(attachmentId) {
  return api.delete(`/api/work-orders/attachments/${attachmentId}`).then((res) => res.data.data)
}
