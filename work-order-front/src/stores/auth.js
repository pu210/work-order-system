import { defineStore } from 'pinia'
import { getToken, getCurrentUser, saveAuth, clearAuth } from '@/utils/auth.js'

export const useAuthStore = defineStore('auth', {
  state: () => {
    const user = getCurrentUser()
    return {
      token: getToken(),
      userId: user?.userId ?? null,
      account: user?.account ?? null,
      name: user?.name ?? null,
      email: user?.email ?? null,
      roleCodes: user?.roleCodes ?? [],
    }
  },
  getters: {
    isAuthenticated: (state) => !!state.token,
    hasRole: (state) => (role) => state.roleCodes.includes(role),
  },
  actions: {
    login(data) {
      saveAuth(data)
      this.token = data.token
      this.userId = data.userId
      this.account = data.account
      this.name = data.name
      this.email = data.email
      this.roleCodes = data.roleCodes || []
    },
    logout() {
      clearAuth()
      this.token = null
      this.userId = null
      this.account = null
      this.name = null
      this.email = null
      this.roleCodes = []
    },
  },
})
