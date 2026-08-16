<!-- src/components/UserDropdown.vue -->
<template>
  <div class="dropdown">
    <a 
      href="#" 
      class="d-flex align-items-center text-white text-decoration-none dropdown-toggle gap-2" 
      data-bs-toggle="dropdown"
    >
      <div class="avatar-circle">陳</div>
      <span class="d-none d-md-inline small">陳工程師</span>
    </a>
    <ul class="dropdown-menu dropdown-menu-end shadow-sm border-0 mt-2">
      <li><a class="dropdown-item" href="#"><i class="bi bi-person me-2"></i>個人設定</a></li>
      <li><hr class="dropdown-divider"></li>
      <li><a class="dropdown-item text-danger" href="#"><i class="bi bi-box-arrow-right me-2"></i>登出</a></li>
    </ul>
  </div>
</template>

<style scoped>
.avatar-circle {
  width: 30px;
  height: 30px;
  background-color: #3b82f6;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
}
</style>