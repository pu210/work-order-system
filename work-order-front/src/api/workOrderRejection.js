import api from "@/plugins/axios.js";

// 取得目前登入者有權查看的工單退回紀錄
export function getWorkOrderRejectionRecords(workOrderId) {
  return api
    .get(`/api/work-orders/${workOrderId}/rejection-records`)
    .then((response) => response.data.data);
}
