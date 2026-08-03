import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const http = axios.create({ baseURL: '', timeout: 15000 })

let refreshing = null

async function tryRefresh() {
  const refreshToken = localStorage.getItem('shop_admin_refresh')
  if (!refreshToken) return false
  try {
    const resp = await axios.post('/api/auth/refresh', { refreshToken })
    const data = resp.data
    if (data && data.code === 0) {
      localStorage.setItem('shop_admin_token', data.data.accessToken)
      localStorage.setItem('shop_admin_refresh', data.data.refreshToken)
      localStorage.setItem('shop_admin_user', JSON.stringify(data.data.user))
      return true
    }
  } catch (e) {
    // refresh failed
  }
  return false
}

function clearAuth() {
  localStorage.removeItem('shop_admin_token')
  localStorage.removeItem('shop_admin_refresh')
  localStorage.removeItem('shop_admin_user')
}

http.interceptors.request.use(config => {
  const token = localStorage.getItem('shop_admin_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

http.interceptors.response.use(
  async response => {
    const body = response.data
    if (body && typeof body.code !== 'undefined') {
      if (body.code !== 0) {
        if (body.code === 401) {
          refreshing = refreshing || tryRefresh()
          const ok = await refreshing
          refreshing = null
          if (ok) {
            return http(response.config)
          }
          clearAuth()
          router.push('/admin/login')
        }
        ElMessage.error(body.message || '请求失败')
        throw new Error(body.message || '请求失败')
      }
      return body.data
    }
    return body
  },
  async error => {
    if (error.response?.status === 401) {
      refreshing = refreshing || tryRefresh()
      const ok = await refreshing
      refreshing = null
      if (ok) {
        return http(error.config)
      }
      clearAuth()
      router.push('/admin/login')
    }
    ElMessage.error(error.response?.data?.message || error.message || '网络异常')
    throw error
  }
)

export default http
