import api from '@/plugins/axios.js'

// 取得當前登入使用者的專屬通知列表
export function getMyNotifications() {
  return api.get('/api/notifications/my').then((res) => res.data.data)
}

// 依使用者 ID 取得通知
export function getNotificationsByUser(receiverId) {
  return api.get(`/api/notifications/user/${receiverId}`).then((res) => res.data.data)
}

// 標示單筆通知為已讀
export function markNotificationAsRead(notificationId) {
  return api.patch(`/api/notifications/read/${notificationId}`).then((res) => res.data.data)
}

// 測試用發送通知 API
export function testSendNotification(params) {
  return api.post('/api/notifications/test-send', null, { params }).then((res) => res.data.data)
}
