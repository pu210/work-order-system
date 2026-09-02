export const WORK_ORDER_STATUS_OPTIONS = [
  { value: "PENDING_REVIEW", label: "待審核" },
  { value: "IN_PROGRESS", label: "進行中" },
  { value: "PENDING_USER_ACCEPTANCE", label: "待報修人驗收" },
  { value: "PENDING_ADMIN_ACCEPTANCE", label: "待管理員驗收" },
  { value: "COMPLETED", label: "已完成" },
  { value: "CANCELLED", label: "已取消" },
];

const STATUS_CONFIG = {
  PENDING_REVIEW: { label: "待審核", className: "wo-status-neutral" },
  IN_PROGRESS: { label: "進行中", className: "wo-status-primary" },
  PENDING_USER_ACCEPTANCE: {
    label: "待報修人驗收",
    className: "wo-status-warning",
  },
  PENDING_ADMIN_ACCEPTANCE: {
    label: "待管理員驗收",
    className: "wo-status-warning",
  },
  COMPLETED: { label: "已完成", className: "wo-status-success" },
  CANCELLED: { label: "已取消", className: "wo-status-danger" },
};

export function statusLabel(status) {
  return STATUS_CONFIG[status]?.label || status;
}

export function statusBadgeClass(status) {
  return STATUS_CONFIG[status]?.className || "wo-status-neutral";
}
