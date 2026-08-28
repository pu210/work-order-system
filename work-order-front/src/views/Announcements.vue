<template>
  <div class="announcements-page">
    <!-- 頁面標題區塊 -->
    <div class="page-header d-flex justify-content-between align-items-end mb-4">
      <div>
        <h1 class="h3 fw-bold text-dark mb-1">系統公告</h1>
        <p class="text-muted small mb-0">重要通知、系統維護與功能更新</p>
      </div>
      <button 
        v-if="authStore.hasRole('ADMIN')" 
        class="btn btn-primary" 
        @click="showAddModal = true"
      >
        <i class="bi bi-plus-lg me-1"></i> 發布公告
      </button>
    </div>

    <!-- 公告列表區塊 -->
    <div class="announce-list">
      <div 
        v-for="a in announcements" 
        :key="a.announcementId" 
        class="card card-pad mb-3 shadow-sm border"
      >
        <div class="card-body">
          <div class="d-flex justify-content-between align-items-start mb-2">
            <div>
              <div class="d-flex align-items-center gap-2 mb-2">
                <!-- 📌 置頂標籤 (支援 Boolean 與 Number) -->
                <span v-if="a.isPinned" class="badge bg-danger">📌 置頂</span>
                <!-- 分類標籤 -->
                <span class="badge" :class="getCategoryBadgeClass(a.category)">
                  {{ getCategoryLabel(a.category) }}
                </span>
              </div>
              <!-- 公告標題：Vue 3 雙大括號 {{ }} 天生防注入攻擊 -->
              <h3 class="h5 fw-bold text-dark mb-0">{{ a.title }}</h3>
            </div>
            <!-- 操作按鈕區 (編輯與刪除：僅管理員可見) -->
            <div v-if="authStore.hasRole('ADMIN')" class="d-flex gap-2">
              <button class="btn btn-outline-primary btn-sm" @click="openEditModal(a)">
                編輯
              </button>
              <button class="btn btn-outline-danger btn-sm" @click="deleteAnn(a.announcementId)">
                刪除
              </button>
            </div>
          </div>

          <!-- 公告內容：Vue 3 {{ }} 自動安全渲染文字 -->
          <p class="card-text text-secondary mt-2 mb-3" style="line-height: 1.8; white-space: pre-line;">
            {{ a.content }}
          </p>

          <!-- 建立資訊 -->
          <div class="text-muted small">
            發布人：管理者 (名稱: {{ getPublisherName(a.createdBy) }}) · 
            {{ formatTime(a.createdTime) }}
          </div>
        </div>
      </div>

      <!-- 無資料時的空狀態 -->
      <div v-if="announcements.length === 0" class="text-center py-5 text-muted card shadow-sm">
        <i class="bi bi-inbox fs-1 d-block mb-2"></i>
        <h5 class="fw-bold">目前沒有公告</h5>
        <p class="small mb-0">資料庫中尚無發布的公告事項</p>
      </div>
    </div>

    <!-- 1. 新增公告彈出對話框 (Add Modal) -->
    <div 
      v-if="showAddModal" 
      class="modal fade show d-block" 
      style="background: rgba(0, 0, 0, 0.5);"
    >
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content shadow-lg border-0">
          <div class="modal-header border-bottom">
            <h5 class="modal-title fw-bold">發布新公告</h5>
            <button type="button" class="btn-close" @click="showAddModal = false"></button>
          </div>
          <div class="modal-body">
            <!-- 標題輸入框：使用 Vue v-model 雙向綁定 -->
            <div class="mb-3">
              <label class="form-label fw-bold small">公告標題 <span class="text-danger">*</span></label>
              <input 
                v-model="form.title" 
                type="text" 
                class="form-control" 
                placeholder="請輸入公告標題..." 
              />
            </div>

            <!-- 內容輸入框：使用 Vue v-model 雙向綁定 -->
            <div class="mb-3">
              <label class="form-label fw-bold small">公告內容 <span class="text-danger">*</span></label>
              <textarea 
                v-model="form.content" 
                class="form-control" 
                rows="4" 
                placeholder="請輸入詳細內容..."
              ></textarea>
            </div>

            <div class="row g-3">
              <!-- 分類選擇 -->
              <div class="col-md-6">
                <label class="form-label fw-bold small">公告類型</label>
                <select v-model="form.category" class="form-select">
                  <option value="GENERAL">一般</option>
                  <option value="MAINTENANCE">系統維護</option>
                  <option value="FEATURE">功能更新</option>
                  <option value="URGENT">緊急</option>
                </select>
              </div>

              <!-- 是否置頂 (對應 SQL BIT 型態) -->
              <div class="col-md-6">
                <label class="form-label fw-bold small">是否置頂</label>
                <select v-model="form.isPinned" class="form-select">
                  <option :value="false">否</option>
                  <option :value="true">是</option>
                </select>
              </div>
            </div>
          </div>
          <div class="modal-footer border-top">
            <button type="button" class="btn btn-light" @click="showAddModal = false">取消</button>
            <button type="button" class="btn btn-primary" @click="createAnnouncement">發布公告</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 2. 編輯修改公告彈出對話框 (Edit Modal) -->
    <div 
      v-if="showEditModal" 
      class="modal fade show d-block" 
      style="background: rgba(0, 0, 0, 0.5);"
    >
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content shadow-lg border-0">
          <div class="modal-header border-bottom">
            <h5 class="modal-title fw-bold">編輯公告</h5>
            <button type="button" class="btn-close" @click="showEditModal = false"></button>
          </div>
          <div class="modal-body">
            <div class="mb-3">
              <label class="form-label fw-bold small">公告標題 <span class="text-danger">*</span></label>
              <input 
                v-model="editForm.title" 
                type="text" 
                class="form-control" 
              />
            </div>

            <div class="mb-3">
              <label class="form-label fw-bold small">公告內容 <span class="text-danger">*</span></label>
              <textarea 
                v-model="editForm.content" 
                class="form-control" 
                rows="4" 
              ></textarea>
            </div>

            <div class="row g-3">
              <div class="col-md-6">
                <label class="form-label fw-bold small">公告類型</label>
                <select v-model="editForm.category" class="form-select">
                  <option value="GENERAL">一般</option>
                  <option value="MAINTENANCE">系統維護</option>
                  <option value="FEATURE">功能更新</option>
                  <option value="URGENT">緊急</option>
                </select>
              </div>

              <div class="col-md-6">
                <label class="form-label fw-bold small">是否置頂</label>
                <select v-model="editForm.isPinned" class="form-select">
                  <option :value="false">否</option>
                  <option :value="true">是</option>
                </select>
              </div>
            </div>
          </div>
          <div class="modal-footer border-top">
            <button type="button" class="btn btn-light" @click="showEditModal = false">取消</button>
            <button type="button" class="btn btn-primary" @click="updateAnnouncement">儲存修改</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import Swal from 'sweetalert2'
import { useAuthStore } from '@/stores/auth.js'
import {
  getAnnouncements,
  createAnnouncement as apiCreateAnnouncement,
  updateAnnouncement as apiUpdateAnnouncement,
  deleteAnnouncement as apiDeleteAnnouncement
} from '@/api/announcement.js'
import { getUsers } from '@/api/user.js'

const authStore = useAuthStore()

// 響應式狀態變數
const announcements = ref([])
const showAddModal = ref(false)
const showEditModal = ref(false)

// 使用者對照 Map (userId -> name)
const userMap = ref({})

const loadUsersMap = async () => {
  // 非管理員無權限呼叫 /api/users，避免觸發 403 轉址到 /forbidden
  if (!authStore.hasRole('ADMIN')) return

  try {
    const res = await getUsers({ size: 1000 })
    const list = res?.content || (Array.isArray(res) ? res : [])
    const map = {}
    list.forEach(u => {
      if (u.userId != null) {
        map[u.userId] = u.name || u.account
      }
    })
    userMap.value = map
  } catch (err) {
    console.warn('載入使用者名單失敗，使用備用對照', err)
  }
}

const getPublisherName = (createdBy) => {
  if (userMap.value[createdBy]) {
    return userMap.value[createdBy]
  }
  if (createdBy === 1) return '系統管理員'
  return createdBy ? `使用者 ${createdBy}` : '管理者'
}

// 1. 新增用表單
const form = reactive({
  title: '',
  content: '',
  category: 'GENERAL',
  isPinned: false,
  createdBy: 1
})

// 2. 編輯用表單
const editForm = reactive({
  announcementId: null,
  title: '',
  content: '',
  category: 'GENERAL',
  isPinned: false,
  createdBy: 1
})

// 分類標籤顏色 Mapping
const getCategoryBadgeClass = (cat) => {
  const map = {
    GENERAL: 'bg-secondary',
    MAINTENANCE: 'bg-primary',
    FEATURE: 'bg-success',
    URGENT: 'bg-danger'
  }
  return map[cat] || 'bg-secondary'
}

// 分類標籤文字 Mapping
const getCategoryLabel = (cat) => {
  const map = {
    GENERAL: '一般',
    MAINTENANCE: '系統維護',
    FEATURE: '功能更新',
    URGENT: '緊急'
  }
  return map[cat] || cat
}

// 格式化時間顯示 (例如：2026-08-28 16:12)
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  return String(timeStr).replace('T', ' ').slice(0, 16)
}

// ---- API 1: 載入所有公告 (GET) ----
const loadAnnouncements = async () => {
  try {
    const data = await getAnnouncements()
    announcements.value = data || []
  } catch (error) {
    console.error('載入公告失敗：', error)
    Swal.fire('錯誤', '無法載入公告資料，請確認後端是否運作中', 'error')
  }
}

// ---- API 2: 新增發布公告 (POST) ----
const createAnnouncement = async () => {
  if (!form.title.trim() || !form.content.trim()) {
    Swal.fire('提示', '請填寫公告標題與內容', 'warning')
    return
  }

  try {
    await apiCreateAnnouncement(form)
    Swal.fire('成功', '公告發布成功！', 'success')
    showAddModal.value = false
    
    // 清空表單
    form.title = ''
    form.content = ''
    form.category = 'GENERAL'
    form.isPinned = false
    
    // 重新刷新列表
    loadAnnouncements()
  } catch (error) {
    console.error('新增公告失敗：', error)
    Swal.fire('錯誤', '發布公告失敗，請確認後端 Spring Boot 是否啟動', 'error')
  }
}

// ---- API 3: 打開編輯視窗並載入原資料 ----
const openEditModal = (a) => {
  editForm.announcementId = a.announcementId
  editForm.title = a.title
  editForm.content = a.content
  editForm.category = a.category
  editForm.isPinned = Boolean(a.isPinned)
  editForm.createdBy = a.createdBy
  showEditModal.value = true
}

// ---- API 4: 儲存修改公告 (PUT 請求) ----
const updateAnnouncement = async () => {
  if (!editForm.title.trim() || !editForm.content.trim()) {
    Swal.fire('提示', '請填寫公告標題與內容', 'warning')
    return
  }

  try {
    await apiUpdateAnnouncement(editForm.announcementId, editForm)
    Swal.fire('成功', '公告修改成功！', 'success')
    showEditModal.value = false
    loadAnnouncements()
  } catch (error) {
    console.error('修改公告失敗：', error)
    Swal.fire('錯誤', '修改失敗，請確認後端 Spring Boot 是否啟動', 'error')
  }
}

// ---- API 5: 刪除公告 (DELETE) ----
const deleteAnn = async (id) => {
  const result = await Swal.fire({
    title: '確定要刪除這筆公告嗎？',
    icon: 'warning',
    showCancelButton: true,
    confirmButtonText: '刪除',
    cancelButtonText: '取消',
    confirmButtonColor: '#d33'
  })

  if (result.isConfirmed) {
    try {
      await apiDeleteAnnouncement(id)
      Swal.fire('已刪除', '公告已成功刪除', 'success')
      loadAnnouncements()
    } catch (error) {
      console.error('刪除公告失敗：', error)
      Swal.fire('錯誤', '刪除失敗', 'error')
    }
  }
}

// 組件載入完畢自動呼叫
onMounted(() => {
  loadUsersMap()
  loadAnnouncements()
})
</script>

<style scoped>
.eyebrow {
  font-size: 0.75rem;
  letter-spacing: 0.08em;
}
.card-pad {
  border-radius: 12px;
  transition: transform 0.15s ease-in-out, box-shadow 0.15s ease-in-out;
}
.card-pad:hover {
  transform: translateY(-2px);
  box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.08) !important;
}
</style>
