 <!-- src/components/NotificationBell.vue -->
<template>
  <div class="dropdown">
    <button 
      class="btn btn-icon text-slate-300 position-relative p-1 border-0" 
      type="button" 
      data-bs-toggle="dropdown"
    >
      <i class="bi bi-bell fs-5 text-light"></i>
      <span class="position-absolute top-0 start-100 translate-middle p-1 bg-danger rounded-circle"></span>
    </button>
    <ul class="dropdown-menu dropdown-menu-end shadow-sm border-0 mt-2" style="width: 280px;">
      <li class="dropdown-header fw-bold">系統通知</li>
      <li><hr class="dropdown-divider"></li>
      <li>
        <a class="dropdown-item py-2" href="#">
          <div class="small fw-semibold">緊急報修通知</div>
          <div class="text-muted extra-small">3號生產線發電機過熱...</div>
        </a>
      </li>
    </ul>
  </div>
</template>