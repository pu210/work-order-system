export const NAV_ITEMS = [
  { key: 'dashboard', label: '儀表板', path: '/dashboard', roles: ['ADMIN', 'HANDLER', 'EMPLOYEE'], enabled: true },
  { key: 'ticket-list', label: '工單列表', path: '/ticket-list', roles: ['ADMIN', 'HANDLER'], enabled: true },
  { key: 'my-tickets', label: '我的工單', path: '/my-tickets', roles: ['EMPLOYEE', 'ADMIN', 'HANDLER'], enabled: true },
  { key: 'ticket-create', label: '建立工單', path: '/ticket-create', roles: ['ADMIN', 'HANDLER', 'EMPLOYEE'], enabled: true },
  { key: 'ticket-assign', label: '指派工單', path: '/ticket-assign', roles: ['ADMIN'], enabled: true },
  { key: 'handler-workbench', label: '工程師工作台', path: '/handler-workbench', roles: ['HANDLER'], enabled: true },
  { key: 'ticket-stats', label: '統計報表', path: '/ticket-stats', roles: ['ADMIN'], enabled: true },
  { key: 'announcements', label: '公告', path: '/announcements', roles: ['ADMIN', 'HANDLER', 'EMPLOYEE'], enabled: true },
  { key: 'user-management', label: '使用者管理', path: '/user-management', roles: ['ADMIN'], enabled: true },

  // 改成單一的系統設定選單
  { key: 'system-settings', label: '設備管理', path: '/settings/system', roles: ['ADMIN'], enabled: true },
];