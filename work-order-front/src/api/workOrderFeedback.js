import api from "@/plugins/axios.js";

// 完整流程回饋僅限管理員查詢
export function getWorkOrderFeedbackRecords(workOrderId) {
  return api
    .get(`/api/work-orders/${workOrderId}/feedback-records`)
    .then((response) => response.data.data);
}
