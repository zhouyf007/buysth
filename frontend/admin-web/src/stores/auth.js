import { defineStore } from 'pinia'
import { authApi } from '../api'

export const useAuthStore = defineStore('adminAuth', {
  state: () => ({
    token: localStorage.getItem('shop_admin_token') || '',
    refreshToken: localStorage.getItem('shop_admin_refresh') || '',
    user: JSON.parse(localStorage.getItem('shop_admin_user') || 'null')
  }),
  getters: {
    isLogin: state => !!state.token
  },
  actions: {
    async login(form) {
      const data = await authApi.login(form)
      if (!(data.user.roles || []).some(r => ['SUPER_ADMIN', 'OPERATOR'].includes(r))) {
        throw new Error('该账号不是管理端账号')
      }
      this.token = data.accessToken
      this.refreshToken = data.refreshToken
      this.user = data.user
      localStorage.setItem('shop_admin_token', data.accessToken)
      localStorage.setItem('shop_admin_refresh', data.refreshToken)
      localStorage.setItem('shop_admin_user', JSON.stringify(data.user))
    },
    async fetchMe() {
      this.user = await authApi.me()
      localStorage.setItem('shop_admin_user', JSON.stringify(this.user))
    },
    logout() {
      this.token = ''
      this.refreshToken = ''
      this.user = null
      localStorage.removeItem('shop_admin_token')
      localStorage.removeItem('shop_admin_refresh')
      localStorage.removeItem('shop_admin_user')
    }
  }
})
