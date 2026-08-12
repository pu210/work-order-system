const TOKEN_KEY = 'access_token'
const USER_KEY = 'current_user'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function saveAuth(data) {
  localStorage.setItem(TOKEN_KEY, data.token)
  localStorage.setItem(USER_KEY, JSON.stringify({
    account: data.account,
    email: data.email,
    roleCodes: data.roleCodes || []
  }))
}

export function getCurrentUser() {
  const value = localStorage.getItem(USER_KEY)
  if (!value) return null

  try {
    return JSON.parse(value)
  } catch {
    clearAuth()
    return null
  }
}

export function clearAuth() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}
