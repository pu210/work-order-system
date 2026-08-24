import api from '@/plugins/axios.js'

// 取得該筆工單的所有對話與聯絡紀錄
export function getContactRecords(workOrderId) {
  return api.get(`/api/work-orders/${workOrderId}/contact-records`).then((res) => res.data.data)
}

// 新增該筆工單的對話/留言紀錄
export function createContactRecord(workOrderId, payload) {
  return api.post(`/api/work-orders/${workOrderId}/contact-records`, payload).then((res) => res.data.data)
}
