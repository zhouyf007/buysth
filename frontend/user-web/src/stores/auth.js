import { defineStore } from 'pinia'
import { authApi } from '../api'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('shop_token') || '',
    refreshToken: localStorage.getItem('shop_refresh') || '',
    user: JSON.parse(localStorage.getItem('shop_user') || 'null')
  }),
  getters: {
    isLogin: state => !!state.token
  },
  actions: {
    async login(form) {
      const data = await authApi.login(form)
      this.token = data.accessToken
      this.refreshToken = data.refreshToken
      this.user = data.user
      localStorage.setItem('shop_token', data.accessToken)
      localStorage.setItem('shop_refresh', data.refreshToken)
      localStorage.setItem('shop_user', JSON.stringify(data.user))
    },
    async register(form) {
      await authApi.register(form)
    },
    async fetchMe() {
      const user = await authApi.me()
      this.user = user
      localStorage.setItem('shop_user', JSON.stringify(user))
    },
    async logout() {
      try {
        await authApi.logout(this.refreshToken)
      } catch (e) {
        // ignore
      }
      this.token = ''
      this.refreshToken = ''
      this.user = null
      localStorage.removeItem('shop_token')
      localStorage.removeItem('shop_refresh')
      localStorage.removeItem('shop_user')
    }
  }
})

