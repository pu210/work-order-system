import api from '@/plugins/axios.js'

export function getWorkOrderDetail(workOrderId) {
    return api
        .get(`/api/work-orders/${workOrderId}/detail`)
        .then((response) => response.data.data)
}