<template>
    <main class="container py-4">
        <div class="d-flex justify-content-between align-items-center">
            <div>
                <h3>首頁</h3>
                <p v-if="user" class="text-secondary mb-0">{{ user.account }}（{{ user.email }}）</p>
            </div>
            <button type="button" class="btn btn-outline-secondary" @click="logout">登出</button>
        </div>
    </main>
</template>
    
<script setup>
import { useRouter } from 'vue-router'
import { clearAuth, getCurrentUser } from '@/utils/auth.js'

const router = useRouter()
const user = getCurrentUser()

function logout() {
    clearAuth()
    router.replace({ name: 'login' })
}
</script>
    
<style>
    
</style>
