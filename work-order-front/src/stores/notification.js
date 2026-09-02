import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from '@/plugins/axios.js'
import { notify } from '@/plugins/notify.js'
import { useAuthStore } from '@/stores/auth.js'
import router from '@/router/router.js'

export const useNotificationStore = defineStore('notification', () => {
  // ---- 1. State (狀態：用 ref 定義) ----
  const notifications = ref([])    // 通知列表
  const unreadCount = ref(0)       // 未讀通知數量
  const socket = ref(null)         // WebSocket 連線物件
  const isConnected = ref(false)   // 是否連線中

  // ---- 2. Getters (計算屬性：用 computed 定義) ----
  // 是否有未讀通知 (用於頂部導覽列 AppNav.vue 顯示紅點)
  const hasUnread = computed(() => unreadCount.value > 0)

  // ---- 3. Actions (動作：用普通 function 定義) ----

  // A. 初始化從後端 API 取得該使用者的歷史通知列表
  const fetchNotifications = async () => {
    try {
      const authStore = useAuthStore()
      const userId = authStore.userId

      let response
      try {
        response = await axios.get('/api/notifications/my')
      } catch (err) {
        // 若舊版端點發送 400，備用降級呼叫專屬使用者通知端點
        if (userId && err.response?.status === 400) {
          response = await axios.get(`/api/notifications/user/${userId}`)
        } else {
          throw err
        }
      }

      const rawData = response.data?.data !== undefined ? response.data.data : response.data
      const list = Array.isArray(rawData) ? rawData : []
      notifications.value = list
      unreadCount.value = list.filter(n => !n.isRead).length
    } catch (error) {
      console.error('取得歷史通知失敗：', error)
    }
  }

  // B. 建立 WebSocket 連線與即時推播監聽
  const connectWebSocket = (userId) => {
    const authStore = useAuthStore()
    const token = authStore.token

    if (!token && !userId) return

    // 💡 每次連線時，若已有舊的 Socket 物件則先關閉，確保切換帳號時能帶著最新 Token/userId 連線
    if (socket.value) {
      try {
        socket.value.close()
      } catch (e) {
        console.warn('關閉舊 WebSocket 失敗：', e)
      }
      socket.value = null
    }

    const wsUrl = token
      ? `ws://localhost:8080/ws/notifications?token=${token}`
      : `ws://localhost:8080/ws/notifications?userId=${userId}`

    console.log('🔌 正在建立安全的 JWT WebSocket 連線：', wsUrl)
    socket.value = new WebSocket(wsUrl)

    // 連線成功
    socket.value.onopen = () => {
      console.log('🟢 WebSocket 即時通知服務連線成功！')
      isConnected.value = true
    }

    // 收到後端即時推播訊息
    socket.value.onmessage = (event) => {
      try {
        const newNotification = JSON.parse(event.data)
        console.log('🔔 收到即時推播通知：', newNotification)

        // 1. 最新通知放最前面 (unshift)
        notifications.value.unshift(newNotification)

        // 2. 未讀計數 + 1
        unreadCount.value++

        // 3. 組裝包含工單單號的標題 (如：工單:WO-2026-0015 管理員退回驗收，請重新處理！)
        const workOrderNoStr = newNotification.workOrderNo
          ? `工單:${newNotification.workOrderNo} `
          : newNotification.workOrderId
            ? `工單:#${newNotification.workOrderId} `
            : ''
        const notificationTitle = newNotification.title || '工單狀態異動通知'
        const targetWorkOrderId = newNotification.workOrderId || newNotification.work_order_id

        // 4. 彈出 Toast 小浮窗提示 (點擊直達該工單詳情頁)
        notify.info(`🔔 ${workOrderNoStr}${notificationTitle}`, {
          onClick: () => {
            if (targetWorkOrderId) {
              const targetPath = `/tickets/${targetWorkOrderId}`
              const currentPath = router.currentRoute.value?.path
              if (currentPath === targetPath) {
                // 若當前已經在該工單詳情頁，強制刷新以載入最新狀態與操作按鈕
                window.location.reload()
              } else {
                router.push(targetPath)
              }
            }
          }
        })
      } catch (err) {
        console.error('解析 WebSocket 推播訊息失敗：', err)
      }
    }

    // 連線中斷
    socket.value.onclose = () => {
      console.log('🔴 WebSocket 連線已中斷。')
      isConnected.value = false
    }

    // 連線發生錯誤
    socket.value.onerror = (error) => {
      console.error('⚠️ WebSocket 連線發生錯誤：', error)
      isConnected.value = false
    }
  }

  // C. 中斷 WebSocket 連線並清空通知快取 (例如使用者登出時)
  const disconnectWebSocket = () => {
    if (socket.value) {
      socket.value.close()
      socket.value = null
      isConnected.value = false
    }
    notifications.value = []
    unreadCount.value = 0
  }

  // D. 標記單筆已讀
  const markAsRead = async (item) => {
    if (item.isRead) return
    try {
      await axios.patch(`/api/notifications/read/${item.notificationId}`)
      item.isRead = true
      if (unreadCount.value > 0) unreadCount.value--
    } catch (error) {
      console.error('標記已讀失敗：', error)
    }
  }

  // E. 全部標記為已讀
  const markAllAsRead = async () => {
    for (const item of notifications.value) {
      if (!item.isRead) {
        await axios.patch(`/api/notifications/read/${item.notificationId}`)
        item.isRead = true
      }
    }
    unreadCount.value = 0
  }

  // F. 從前端與資料庫移除單筆通知
  const removeNotification = async (notificationId) => {
    const index = notifications.value.findIndex(n => n.notificationId === notificationId)
    if (index !== -1) {
      const item = notifications.value[index]
      if (!item.isRead && unreadCount.value > 0) {
        unreadCount.value--
      }
      notifications.value.splice(index, 1)
    }

    try {
      await axios.delete(`/api/notifications/${notificationId}`)
    } catch (error) {
      console.error('刪除後端通知失敗：', error)
    }
  }

  // ---- 4. 匯出要對外公開的狀態與方法 ----
  return {
    notifications,
    unreadCount,
    socket,
    isConnected,
    hasUnread,
    fetchNotifications,
    connectWebSocket,
    disconnectWebSocket,
    markAsRead,
    markAllAsRead,
    removeNotification
  }
})
