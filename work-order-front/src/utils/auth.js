const TOKEN_KEY = "access_token";
const USER_KEY = "current_user";

export function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function hasValidToken() {
  const token = getToken();
  if (!token) return false;

  try {
    const parts = token.split(".");
    if (parts.length !== 3) {
      clearAuth();
      return false;
    }

    const base64 = parts[1].replace(/-/g, "+").replace(/_/g, "/");
    const payload = JSON.parse(atob(base64));
    const now = Math.floor(Date.now() / 1000);

    if (typeof payload.exp !== "number" || payload.exp <= now) {
      clearAuth();
      return false;
    }

    return true;
  } catch {
    clearAuth();
    return false;
  }
}

export function saveAuth(data) {
  localStorage.setItem(TOKEN_KEY, data.token);
  localStorage.setItem(
    USER_KEY,
    JSON.stringify({
      userId: data.userId,
      name: data.name,
      account: data.account,
      email: data.email,
      roleCodes: data.roleCodes || [],
      mustChangePassword: data.mustChangePassword ?? false,
    }),
  );
}

export function getCurrentUser() {
  const value = localStorage.getItem(USER_KEY);
  if (!value) return null;

  try {
    return JSON.parse(value);
  } catch {
    clearAuth();
    return null;
  }
}

export function clearAuth() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}
export function markPasswordChanged() {
  const user = getCurrentUser();

  if (!user) return;

  localStorage.setItem(
    USER_KEY,
    JSON.stringify({
      ...user,
      mustChangePassword: false,
    }),
  );
}
