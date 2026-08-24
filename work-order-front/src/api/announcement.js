import api from '@/plugins/axios.js'

// 取得所有公告列表
export function getAnnouncements() {
  return api.get('/api/announcements').then((res) => res.data.data)
}

// 依 ID 取得單筆公告
export function getAnnouncementById(id) {
  return api.get(`/api/announcements/${id}`).then((res) => res.data.data)
}

// 依分類取得公告
export function getAnnouncementsByCategory(category) {
  return api.get(`/api/announcements/category/${category}`).then((res) => res.data.data)
}

// 發布新公告
export function createAnnouncement(payload) {
  return api.post('/api/announcements', payload).then((res) => res.data.data)
}

// 修改公告
export function updateAnnouncement(id, payload) {
  return api.put(`/api/announcements/${id}`, payload).then((res) => res.data.data)
}

// 刪除公告
export function deleteAnnouncement(id) {
  return api.delete(`/api/announcements/${id}`).then((res) => res.data.data)
}
