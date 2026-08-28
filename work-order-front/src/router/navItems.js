// 頂端列與路由守衛共用的角色/選單設定，避免兩邊清單各自維護、跑掉不同步
//
// enabled: true 代表該頁面尚未建立元件、尚未在 router.js 註冊路由，
// 頂端列先不顯示這個項目，避免出現點了沒反應的死連結。
// 之後做到該角色流程時，建立對應 .vue 元件、在 router.js 註冊路由，再把這裡改成 true。
export const NAV_ITEMS = [
  {
    key: "dashboard",
    label: "首頁",
    path: "/dashboard",
    roles: ["ADMIN", "HANDLER", "EMPLOYEE"],
    enabled: true,
  },
  {
    key: "ticket-list",
    label: "工單列表",
    path: "/ticket-list",
    roles: ["ADMIN"],
    enabled: true,
  },
  {
    key: "my-tickets",
    label: "我的工單",
    path: "/my-tickets",
    roles: ["EMPLOYEE", "ADMIN", "HANDLER"],
    enabled: true,
  },
  {
    key: "ticket-create",
    label: "建立工單",
    path: "/ticket-create",
    roles: ["ADMIN", "HANDLER", "EMPLOYEE"],
    enabled: true,
  },
  {
    key: "ticket-assign",
    label: "管理員工作台",
    path: "/ticket-assign",
    roles: ["ADMIN"],
    enabled: true,
  },
  {
    key: "handler-workbench",
    label: "工程師工作台",
    path: "/handler-workbench",
    roles: ["HANDLER"],
    enabled: true,
  },
  {
    key: "ticket-stats",
    label: "統計報表",
    path: "/ticket-stats",
    roles: ["ADMIN"],
    enabled: true,
  },
  {
    key: "announcements",
    label: "公告",
    path: "/announcements",
    roles: ["ADMIN", "HANDLER", "EMPLOYEE"],
    enabled: true,
  },
  {
    key: "user-management",
    label: "帳號管理",
    path: "/user-management",
    roles: ["ADMIN"],
    enabled: true,
  },
  {
    key: "system-settings",
    label: "設備維修管理",
    path: "/system-settings",
    roles: ["ADMIN", "HANDLER"],
    enabled: true,
  },

];
