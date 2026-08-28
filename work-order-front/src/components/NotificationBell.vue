<template>
  <div class="dropdown">
    <button 
      class="btn btn-icon text-slate-300 position-relative p-1 border-0" 
      type="button" 
      data-bs-toggle="dropdown"
    >
      <i class="bi bi-bell fs-5 text-light"></i>
      <span 
        v-if="notificationStore.unreadCount > 0" 
        class="position-absolute top-0 start-100 translate-middle badge bg-danger rounded-1"
        style="font-size: 10px; padding: 2px 4px; border-radius: 3px !important;"
      >
        {{ notificationStore.unreadCount > 99 ? '99+' : notificationStore.unreadCount }}
      </span>
    </button>
    <ul class="dropdown-menu dropdown-menu-end shadow-sm border-0 mt-2" style="width: 280px;">
      <li class="dropdown-header fw-bold">系統通知</li>
      <li><hr class="dropdown-divider"></li>
      <li>
        <router-link class="dropdown-item py-2" to="/notifications">
          <div class="small fw-semibold">前往通知中心</div>
          <div class="text-muted extra-small">查看所有未讀與系統通知...</div>
        </router-link>
      </li>
    </ul>
  </div>
</template>

<script setup>
import { useNotificationStore } from '@/stores/notification.js'
const notificationStore = useNotificationStore()
</script>