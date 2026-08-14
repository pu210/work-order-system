<template>
  <div class="main-layout">
    <AppNav />
    <main class="main-layout-content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import AppNav from '@/components/layout/AppNav.vue'
</script>

<style scoped>
.main-layout-content {
  max-width: 1240px;
  margin: 0 auto;
  padding: 28px 24px 64px;
}
</style>
