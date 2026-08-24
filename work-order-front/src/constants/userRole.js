export const USER_ROLE_OPTIONS = [
  { value: "EMPLOYEE", label: "一般員工" },
  { value: "HANDLER", label: "維修人員" },
  { value: "ADMIN", label: "管理員" },
];

export function userRoleLabel(roleCode) {
  const matchedRole = USER_ROLE_OPTIONS.find(
    (option) => option.value === roleCode
  );

  return matchedRole?.label || roleCode || "";
}
