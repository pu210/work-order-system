// 1. 匯入 vue-router 所需函式
import { createRouter, createWebHistory } from "vue-router";

// 2. 匯入組件
import Login from "@/views/Login.vue";
import AuthLayout from "@/layouts/AuthLayout.vue";
import MainLayout from "@/layouts/MainLayout.vue";
import Announcements from "@/views/Announcements.vue";
import Dashboard from "@/views/Dashboard.vue";
import TicketList from "@/views/TicketList.vue";
import MyTickets from "@/views/MyTickets.vue";
import TicketDetail from "@/views/TicketDetail.vue";
import TicketCreate from "@/views/TicketCreate.vue";
import TicketAssign from "@/views/TicketAssign.vue";
import HandlerWorkbench from "@/views/HandlerWorkbench.vue";
import TicketStats from "@/views/TicketStats.vue";
import UserManagement from "@/views/UserManagement.vue";
import EquipmentCreate from "@/views/EquipmentCreate.vue";
import Profile from "@/views/Profile.vue";
import Settings from "@/views/Settings.vue";
import Notifications from "@/views/Notifications.vue";
import Forbidden from "@/views/Forbidden.vue";
import { getToken, getCurrentUser } from "@/utils/auth.js";
import { NAV_ITEMS } from "@/router/navItems.js";

function rolesFor(key) {
  return NAV_ITEMS.find((item) => item.key === key)?.roles ?? [];
}

// 3. 定義路由
const routes = [
  {
    path: "/",
    redirect: "/dashboard",
  },
  {
    path: "/auth",
    component: AuthLayout,
    children: [
      {
        path: "login",
        name: "login",
        component: Login,
        meta: { guestOnly: true },
      },
    ],
  },
  // 登入後的系統頁面，全部走 MainLayout（頂端列 + 內容），都需要登入
  {
    path: "/",
    component: MainLayout,
    meta: { requiresAuth: true },
    children: [
      {
        path: "dashboard",
        name: "dashboard",
        component: Dashboard,
        meta: { roles: rolesFor("dashboard") },
      },
      {
        path: "ticket-list",
        name: "ticket-list",
        component: TicketList,
        meta: { roles: rolesFor("ticket-list") },
      },
      {
        path: "my-tickets",
        name: "my-tickets",
        component: MyTickets,
        meta: { roles: rolesFor("my-tickets") },
      },
      { path: "tickets/:id", name: "ticket-detail", component: TicketDetail },
      {
        path: "ticket-create",
        name: "ticket-create",
        component: TicketCreate,
        meta: { roles: rolesFor("ticket-create") },
      },
      {
        path: "ticket-assign",
        name: "ticket-assign",
        component: TicketAssign,
        meta: { roles: rolesFor("ticket-assign") },
      },
      {
        path: "handler-workbench",
        name: "handler-workbench",
        component: HandlerWorkbench,
        meta: { roles: rolesFor("handler-workbench") },
      },
      {
        path: "ticket-stats",
        name: "ticket-stats",
        component: TicketStats,
        meta: { roles: rolesFor("ticket-stats") },
      },
      {
        path: "announcements",
        name: "announcements",
        component: Announcements,
        meta: { roles: rolesFor("announcements") },
      },
      {
        path: "user-management",
        name: "user-management",
        component: UserManagement,
        meta: { roles: rolesFor("user-management") },
      },
      {
        path: "equipment-create",
        name: "equipment-create",
        component: EquipmentCreate,
        meta: { roles: rolesFor("equipment-create") },
      },
      { path: "profile", name: "profile", component: Profile },
      { path: "settings", name: "settings", component: Settings },
      {
        path: "notifications",
        name: "notifications",
        component: Notifications,
      },
    ],
  },
  { path: "/forbidden", name: "forbidden", component: Forbidden },
];

// 4. 建立 router
const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to) => {
  const isAuthenticated = Boolean(getToken());

  if (to.meta.requiresAuth && !isAuthenticated) {
    return {
      name: "login",
      query: { returnUrl: to.fullPath },
    };
  }

  if (to.meta.guestOnly && isAuthenticated) {
    return { name: "dashboard" };
  }

  if (isAuthenticated && to.meta.roles) {
    const roleCodes = getCurrentUser()?.roleCodes ?? [];
    const allowed = to.meta.roles.some((role) => roleCodes.includes(role));
    if (!allowed) {
      return { name: "forbidden" };
    }
  }
});

// 5. 匯出 router 實體，讓 main.js 匯入使用
export default router;
