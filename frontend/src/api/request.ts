import axios, { type AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiResponse } from '@/types/api'
import router from '@/router'

const TOKEN_KEY = 'jh_token'
const ADMIN_TOKEN_KEY = 'jh_admin_token'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY)
}

export function getAdminToken() {
  return localStorage.getItem(ADMIN_TOKEN_KEY)
}

export function setAdminToken(token: string) {
  localStorage.setItem(ADMIN_TOKEN_KEY, token)
}

export function clearAdminToken() {
  localStorage.removeItem(ADMIN_TOKEN_KEY)
}

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api/v1',
  timeout: 30000,
})

request.interceptors.request.use((config) => {
  const isAdmin = config.url?.startsWith('/admin') || config.headers?.['X-Admin'] === '1'
  const token = isAdmin ? getAdminToken() : getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (res) => {
    const body = res.data as ApiResponse
    if (body && typeof body.code === 'number' && body.code !== 0) {
      handleBizError(body.code, body.message)
      return Promise.reject(body)
    }
    return res
  },
  (err) => {
    const msg = err.response?.data?.message || err.message || '网络异常'
    const code = err.response?.data?.code
    if (code) handleBizError(code, msg)
    else ElMessage.error(msg)
    return Promise.reject(err)
  },
)

function handleBizError(code: number, message: string) {
  if (code === 40100 || code === 40101) {
    const path = router.currentRoute.value.path
    if (path.startsWith('/admin')) {
      clearAdminToken()
      router.push({ name: 'admin-login', query: { redirect: path } })
    } else {
      clearToken()
      router.push({ name: 'login', query: { redirect: path } })
    }
    ElMessage.warning(message || '请重新登录')
    return
  }
  if (code === 40301) {
    ElMessage.error(message || '账号已被封禁')
    return
  }
  ElMessage.error(message || '请求失败')
}

export async function http<T>(config: AxiosRequestConfig): Promise<T> {
  const res = await request(config)
  return (res.data as ApiResponse<T>).data
}

export default request
