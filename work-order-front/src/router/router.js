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
import SystemSettings from "@/views/SystemSettings.vue";
import Profile from "@/views/Profile.vue";
import Notifications from "@/views/Notifications.vue";
import Forbidden from "@/views/Forbidden.vue";
import { hasValidToken, getCurrentUser, clearAuth } from "@/utils/auth.js";
import { refreshAccessToken } from "@/plugins/axios.js";
import { NAV_ITEMS } from "@/router/navItems.js";
import UserCreate from "@/views/UserCreate.vue";
import UserEdit from "@/views/UserEdit.vue";
import ForgotPassword from "@/views/ForgotPassword.vue";
import ResetPassword from "@/views/ResetPassword.vue";
import InitialPassword from "@/views/InitialPassword.vue";
import Register from "@/views/Register.vue";
import EquipmentHistory from "@/views/EquipmentHistory.vue";
import { useAuthStore } from "@/stores/auth.js";

function rolesFor(key) {
  return NAV_ITEMS.find((item) => item.key === key)?.roles ?? [];
}

// 3. 定義路由
const routes = [
  {
    path: "/",
    redirect: "/dashboard",
  },

  // 登入頁面（使用 AuthLayout）
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
        path: "register",
        name: "Register",
        component: Register,
        meta: { guestOnly: true },
      },
      {
        path: "forgot/password",
        name: "ForgotPassword",
        component: ForgotPassword,
        meta: { guestOnly: true },
      },
      {
        path: "reset-password",
        name: "ResetPassword",
        component: ResetPassword,
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
        path: "account/initial-password",
        name: "initial-password",
        component: InitialPassword,
      },
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
        path: "equipment/:targetNo/history",
        name: "equipment-history",
        component: EquipmentHistory,
        meta: { roles: ["ADMIN", "HANDLER"] },
      },
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
      {
        path: "equipment-create",
        name: "equipment-create",
        component: SystemSettings,
        meta: { roles: rolesFor("equipment-create") },
      },
      {
        path: "system-settings",
        name: "system-settings",
        component: SystemSettings,
        meta: { roles: rolesFor("system-settings") },
      },
      { path: "profile", name: "profile", component: Profile },
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
  // 預設不重置捲動位置：從內容較長的頁面切到較短的頁面（如新增工單）時，
  // 舊的捲動位置會超出新頁面範圍被瀏覽器夾回底部，導致 sticky 導覽列看起來往上移
  scrollBehavior() {
    return { top: 0 };
  },
});

router.beforeEach(async (to) => {
  // OAuth 完成後必須先進入 Login.vue，
  // 才能用後端 Session 換取本次登入的 JWT。
  const isOAuthCallback =
    to.name === "Login" &&
    (to.query.oauth === "success" || to.query.oauth === "failed");

  if (isOAuthCallback) {
    return true;
  }

  let isAuthenticated = hasValidToken();

  // 需要登入的頁面沒有有效 Access Token 時，嘗試刷新 Token。
  if (to.meta.requiresAuth && !isAuthenticated) {
    try {
      await refreshAccessToken();
      isAuthenticated = true;
    } catch {
      clearAuth();

      return {
        name: "Login",
        query: {
          returnUrl: to.fullPath,
        },
      };
    }
  }

  // 每次判斷時重新讀取，因為 refreshAccessToken() 可能已更新使用者資料。
  const mustChangePassword =
    isAuthenticated && getCurrentUser()?.mustChangePassword === true;

  if (mustChangePassword) {
    // 使用者從修改初始密碼頁返回登入頁，視為放棄登入。
    // 必須先登出及清除資料，否則下面的 guestOnly 會再次導向 Dashboard。
    if (to.name === "Login") {
      const authStore = useAuthStore();

      try {
        await authStore.logout();
      } catch (error) {
        // logout() 的 finally 已清除前端登入狀態，
        // 即使後端登出失敗，仍允許返回登入頁。
        console.warn("登出 API 呼叫失敗", error);
      }

      return true;
    }

    // 初始密碼尚未修改時，不允許前往其他系統頁面。
    if (to.name !== "initial-password") {
      return {
        name: "initial-password",
      };
    }
  }
  // 已完成初始密碼修改後，不允許再次進入初始密碼頁。
  if (
    isAuthenticated &&
    !mustChangePassword &&
    to.name === "initial-password"
  ) {
    return {
      name: "Dashboard",
    };
  }

  // 一般已登入使用者不能進入登入、註冊等訪客頁面。
  if (to.meta.guestOnly && isAuthenticated) {
    return {
      name: "Dashboard",
    };
  }

  // 檢查頁面角色權限。
  if (isAuthenticated && to.meta.roles) {
    const rawRoles = getCurrentUser()?.roleCodes ?? [];
    const userRoles = rawRoles.map((r) =>
      String(r)
        .replace(/^ROLE_/, "")
        .toUpperCase(),
    );

    const allowed = to.meta.roles.some((role) => {
      const cleanRole = String(role)
        .replace(/^ROLE_/, "")
        .toUpperCase();
      return (
        userRoles.includes(cleanRole) ||
        (cleanRole === "HANDLER" && userRoles.includes("ENGINEER")) ||
        (cleanRole === "ENGINEER" && userRoles.includes("HANDLER"))
      );
    });

    if (!allowed) {
      return {
        name: "forbidden",
      };
    }
  }
});

// 5. 匯出 router 實體，讓 main.js 匯入使用
export default router;
