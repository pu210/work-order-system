import api from "@/plugins/axios.js";

// WorkOrderController 回應包在 ApiResponse { code, message, data } 裡，這裡直接解出 data 方便呼叫端使用
//
// 建單與附件現在是同一個 API、同一個交易：request 欄位包成 JSON Blob 塞進 "request" part，
// files 逐一塞進 "files" part（可不帶）。任一張附件驗證失敗，後端會連工單本體一起 rollback，
// 不會出現「工單建立成功但附件缺漏」的情況。
export function createWorkOrder(payload, files = []) {
  const formData = new FormData();
  formData.append(
    "request",
    new Blob([JSON.stringify(payload)], { type: "application/json" }),
  );
  files.forEach((file) => formData.append("files", file));
  return api.post("/api/work-orders", formData).then((res) => res.data.data);
}

export function getWorkOrderById(id) {
  return api.get(`/api/work-orders/${id}`).then((res) => res.data.data);
}

export function getWorkOrderDetail(id) {
  return api.get(`/api/work-orders/${id}/detail`).then((res) => res.data.data);
}

export function getWorkOrderList(params) {
  return api.get("/api/work-orders", { params }).then((res) => res.data.data);
}

export function getMySubmissions(params) {
  return api
    .get("/api/work-orders/my-submissions", { params })
    .then((res) => res.data.data);
}

// 管理員審核編輯鎖

export function startEditSession(workOrderId) {
  return api
    .post(`/api/work-orders/${workOrderId}/review/edit-session`)
    .then((res) => res.data.data);
}

export function editSessionHeartbeat(workOrderId, sessionToken) {
  return api
    .patch(
      `/api/work-orders/${workOrderId}/review/edit-session/heartbeat`,
      null,
      {
        headers: {
          "X-Edit-Session-Token": sessionToken,
        },
      },
    )
    .then((res) => res.data.data);
}

export function releaseEditSession(workOrderId, sessionToken) {
  return api
    .delete(`/api/work-orders/${workOrderId}/review/edit-session`, {
      headers: {
        "X-Edit-Session-Token": sessionToken,
      },
    })
    .then((res) => res.data.data);
}

// 管理員初審／派工

export function reviewAccept(workOrderId, payload, sessionToken) {
  return api
    .post(`/api/work-orders/${workOrderId}/review/accept`, payload, {
      headers: {
        "X-Edit-Session-Token": sessionToken,
      },
    })
    .then((res) => res.data.data);
}

export function reviewReject(workOrderId, payload, sessionToken) {
  return api
    .post(`/api/work-orders/${workOrderId}/review/reject`, payload, {
      headers: {
        "X-Edit-Session-Token": sessionToken,
      },
    })
    .then((res) => res.data.data);
}

// 工程師處理

export function progressAccept(workOrderId, payload) {
  return api
    .post(`/api/work-orders/${workOrderId}/progress/accept`, payload)
    .then((res) => res.data.data);
}

export function progressReject(workOrderId, payload) {
  return api
    .post(`/api/work-orders/${workOrderId}/progress/reject`, payload)
    .then((res) => res.data.data);
}

// 申請人驗收

export function userCheckAccept(workOrderId, payload) {
  return api
    .post(`/api/work-orders/${workOrderId}/user-check/accept`, payload)
    .then((res) => res.data.data);
}

// 管理員最終驗收

export function adminCheckAccept(workOrderId, payload) {
  return api
    .post(`/api/work-orders/${workOrderId}/admin-check/accept`, payload)
    .then((res) => res.data.data);
}

export function adminCheckReject(workOrderId, payload) {
  return api
    .post(`/api/work-orders/${workOrderId}/admin-check/reject`, payload)
    .then((res) => res.data.data);
}

// 建單流程已改成 WorkOrderController.create() 同一支 API、同一交易帶附件，這支事後補上傳沒有呼叫端了。
// export function uploadAttachments(workOrderId, files) {
//   const formData = new FormData();
//   files.forEach((file) => formData.append("files", file));
//   return api
//     .post(`/api/work-orders/${workOrderId}/attachments`, formData)
//     .then((res) => res.data.data);
// }

export function getAttachments(workOrderId) {
  return api
    .get(`/api/work-orders/${workOrderId}/attachments`)
    .then((res) => res.data.data);
}

// 預覽 API 不包 ApiResponse，直接回檔案 binary；<img> 原生無法帶 Authorization header，改用 blob + object URL
export function getAttachmentPreview(attachmentId) {
  return api
    .get(`/api/work-orders/attachments/${attachmentId}/view`, {
      responseType: "blob",
    })
    .then((res) => res.data);
}

export function deleteAttachment(attachmentId) {
  return api
    .delete(`/api/work-orders/attachments/${attachmentId}`)
    .then((res) => res.data.data);
}
