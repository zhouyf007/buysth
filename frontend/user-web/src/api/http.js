import axios from 'axios'
import router from '../router'

const http = axios.create({
  baseURL: '',
  timeout: 15000
})

let refreshing = null

async function tryRefresh() {
  const refreshToken = localStorage.getItem('shop_refresh')
  if (!refreshToken) return false
  try {
    const resp = await axios.post('/api/auth/refresh', { refreshToken })
    const data = resp.data
    if (data && data.code === 0) {
      localStorage.setItem('shop_token', data.data.accessToken)
      localStorage.setItem('shop_refresh', data.data.refreshToken)
      localStorage.setItem('shop_user', JSON.stringify(data.data.user))
      return true
    }
  } catch (e) {
    // refresh failed
  }
  return false
}

function clearAuth() {
  localStorage.removeItem('shop_token')
  localStorage.removeItem('shop_refresh')
  localStorage.removeItem('shop_user')
}

async function redirectLogin() {
  if (!router.currentRoute.value.path.startsWith('/login')) {
    router.push({ path: '/login', query: { redirect: router.currentRoute.value.fullPath } })
  }
}

http.interceptors.request.use(config => {
  const token = localStorage.getItem('shop_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
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
          redirectLogin()
        }
        const error = new Error(body.message || '请求失败')
        error.code = body.code
        throw error
      }
      return body.data
    }
    return body
  },
  async error => {
    if (error.response && error.response.status === 401) {
      refreshing = refreshing || tryRefresh()
      const ok = await refreshing
      refreshing = null
      if (ok) {
        return http(error.config)
      }
      clearAuth()
      redirectLogin()
    }
    throw new Error(error.response?.data?.message || error.message || '网络异常')
  }
)

export default http
