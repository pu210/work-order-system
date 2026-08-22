import api from "@/plugins/axios.js";

// 取得工單詳細資訊
export function getWorkOrderDetail(workOrderId) {
  return api
    .get(`/api/work-orders/${workOrderId}/detail`)
    .then((response) => response.data.data);
}

// 取得聯繫紀錄
export function getContactRecords(workOrderId) {
  return api
    .get(`/api/work-orders/${workOrderId}/contact-records`)
    .then((response) => response.data.data);
}
