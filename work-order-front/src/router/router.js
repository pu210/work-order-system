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

// 將原本的三個獨立組件換成一個整合設定頁
import SystemSettings from "@/views/SystemSettings.vue";

import Profile from "@/views/Profile.vue";
import Settings from "@/views/Settings.vue";
import Notifications from "@/views/Notifications.vue";
import Forbidden from "@/views/Forbidden.vue";
import { hasValidToken, getCurrentUser } from "@/utils/auth.js";
import { NAV_ITEMS } from "@/router/navItems.js";
import UserCreate from "@/views/UserCreate.vue";
import UserEdit from "@/views/UserEdit.vue";
import ForgotPassword from "@/views/ForgotPassword.vue";

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
    redirect: "/auth/login",
    children: [
      {
        path: "login",
        name: "Login",
        component: Login,
        meta: { guestOnly: true },
      },
      {
        path: "forgot/password",
        name: "ForgotPassword",
        component: ForgotPassword,
        meta: { guestOnly: true },
      },
    ],
  },
  {
    path: "/",
    component: MainLayout,
    meta: { requiresAuth: true },
    children: [
      {
        path: "dashboard",
        name: "Dashboard",
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
        meta: { roles: rolesFor("user-management") },
        children: [
          {
            path: "",
            name: "user-management",
            component: UserManagement,
          },
          {
            path: "new",
            name: "user-create",
            component: UserCreate,
          },
          {
            path: ":id/edit",
            name: "user-edit",
            component: UserEdit,
          },
        ],
      },
      // 合併為單一路由指向系統設定頁
      {
        path: "settings/system",
        name: "system-settings",
        component: SystemSettings,
        meta: { roles: rolesFor("system-settings") },
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
  const isAuthenticated = hasValidToken();

  if (to.meta.requiresAuth && !isAuthenticated) {
    return {
      name: "Login",
      query: { returnUrl: to.fullPath },
    };
  }

  if (to.meta.guestOnly && isAuthenticated) {
    return { name: "Dashboard" };
  }

  if (isAuthenticated && to.meta.roles) {
    const roleCodes = getCurrentUser()?.roleCodes ?? [];
    const allowed = to.meta.roles.some((role) => roleCodes.includes(role));
    if (!allowed) {
      return { name: "forbidden" };
    }
  }
});

// 5. 匯出 router 實體
export default router;