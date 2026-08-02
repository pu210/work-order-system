// 1. 匯入 vue-router 所需函式
import { createRouter, createWebHistory } from 'vue-router'

// 2. 匯入組件
import Home from '@/views/Home.vue'
import Login from '@/views/Login.vue';

// 3. 定義路由
const routes = [
    { path: '/home', 
      component: Home,
      name: 'home' },
    { path: '/', 
      component: Login,
      name: 'login' }
];

// 4. 建立 router
const router = createRouter({
    history: createWebHistory(),
    routes
});

// 5. 匯出 router 實體，讓 main.js 匯入使用
export default router