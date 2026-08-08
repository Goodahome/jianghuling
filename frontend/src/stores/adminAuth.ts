import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import * as adminApi from '@/api/admin'
import { clearAdminToken, getAdminToken, setAdminToken } from '@/api/request'
import type { AdminAuthResult } from '@/types/models'

export const useAdminAuthStore = defineStore('adminAuth', () => {
  const token = ref(getAdminToken() || '')
  const admin = ref<AdminAuthResult['admin'] | null>(null)
  const loading = ref(false)

  const isLoggedIn = computed(() => !!token.value)

  /** 权限码精确匹配；含 `*` 通配则全放行 */
  function hasPermission(code: string) {
    const perms = admin.value?.permissions || []
    if (perms.includes('*')) return true
    return perms.includes(code)
  }

  async function login(username: string, password: string) {
    loading.value = true
    try {
      const result = await adminApi.adminLogin(username, password)
      token.value = result.token
      admin.value = result.admin
      setAdminToken(result.token)
      return result
    } finally {
      loading.value = false
    }
  }

  async function fetchMe() {
    if (!token.value) return null
    admin.value = await adminApi.adminMe()
    return admin.value
  }

  async function logout() {
    try {
      if (token.value) await adminApi.adminLogout()
    } catch {
      /* ignore */
    }
    token.value = ''
    admin.value = null
    clearAdminToken()
  }

  return { token, admin, loading, isLoggedIn, hasPermission, login, fetchMe, logout }
})
