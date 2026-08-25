import api from "@/plugins/axios.js";

/**
 * 查詢指定設備的歷史工單。
 *
 * @param {string} targetNo 設備編號
 * @param {{ page?: number, size?: number }} params 分頁參數
 * @returns {Promise<object>} 設備資料與歷史工單分頁資料
 */
export function getEquipmentHistory(targetNo, params = {}) {
  return api
    .get(`/api/equipment/${encodeURIComponent(targetNo)}/work-orders`, {
      params: {
        page: params.page ?? 0,
        size: params.size ?? 20,
      },
    })
    .then((response) => response.data.data);
}
