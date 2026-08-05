import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import * as authApi from '@/api/auth'
import { clearToken, getToken, setToken } from '@/api/request'
import type { LoginType } from '@/types/api'
import type { MeProfile, OfficeBrief, UserBrief } from '@/types/models'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(getToken() || '')
  const user = ref<UserBrief | MeProfile | null>(null)
  const me = ref<MeProfile | null>(null)
  const loading = ref(false)

  const isLoggedIn = computed(() => !!token.value)
  const hasOffice = computed(() => {
    return activeOffices.value.length > 0
  })

  const hasDecreeOffice = computed(() => hasOfficeCode('DECREE_REVIEWER'))
  const hasFeatOffice = computed(() => hasOfficeCode('FEAT_REVIEWER'))

  const activeOffices = computed(() => {
    const raw = (me.value?.offices || user.value?.offices || []) as Array<
      OfficeBrief | string | { code?: string; status?: string }
    >
    return raw
      .map((o) => {
        if (typeof o === 'string') {
          return { code: o, name: o, status: 'ACTIVE' as const }
        }
        return {
          code: String(o.code || ''),
          name: String((o as OfficeBrief).name || o.code || ''),
          status: (o.status as OfficeBrief['status']) || 'ACTIVE',
        }
      })
      .filter((o) => o.code && o.status === 'ACTIVE')
  })

  function hasOfficeCode(code: string) {
    return activeOffices.value.some((o) => o.code === code)
  }

  function applyAuth(result: { token: string; user: UserBrief }) {
    token.value = result.token
    user.value = result.user
    setToken(result.token)
  }

  async function login(payload: {
    loginType: LoginType
    username?: string
    password?: string
    phone?: string
    smsCode?: string
  }) {
    loading.value = true
    try {
      const result = await authApi.login(payload)
      applyAuth(result)
      await fetchMe()
      return result
    } finally {
      loading.value = false
    }
  }

  async function register(payload: Parameters<typeof authApi.register>[0]) {
    loading.value = true
    try {
      const result = await authApi.register(payload)
      applyAuth(result)
      await fetchMe()
      return result
    } finally {
      loading.value = false
    }
  }

  async function fetchMe() {
    if (!token.value) return null
    me.value = await authApi.fetchMe()
    user.value = me.value
    return me.value
  }

  async function logout() {
    try {
      if (token.value) await authApi.logout()
    } catch {
      /* ignore */
    }
    token.value = ''
    user.value = null
    me.value = null
    clearToken()
  }

  return {
    token,
    user,
    me,
    loading,
    isLoggedIn,
    hasOffice,
    hasDecreeOffice,
    hasFeatOffice,
    activeOffices,
    hasOfficeCode,
    login,
    register,
    fetchMe,
    logout,
    applyAuth,
  }
})
