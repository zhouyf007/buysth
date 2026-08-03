<template>
  <div class="auth-page">
    <div class="auth-card panel">
      <h1>登录数码商城</h1>
      <p class="muted">演示账号 user / 123456</p>
      <form @submit.prevent="submit">
        <label>用户名<input v-model="form.username" class="input" autocomplete="username" /></label>
        <label>密码<input v-model="form.password" class="input" type="password" autocomplete="current-password" /></label>
        <p class="error-text">{{ error }}</p>
        <button class="btn btn-primary submit" :disabled="loading">{{ loading ? '登录中...' : '登录' }}</button>
      </form>
      <div class="auth-foot">
        <span class="muted">还没有账号？</span>
        <RouterLink to="/register">立即注册</RouterLink>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const form = reactive({ username: '', password: '' })
const error = ref('')
const loading = ref(false)

const submit = async () => {
  error.value = ''
  loading.value = true
  try {
    await auth.login(form)
    router.push(route.query.redirect || '/')
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 70vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}

.auth-card {
  width: 380px;
  padding: 32px;
}

.auth-card h1 {
  margin: 0 0 6px;
  font-size: 22px;
}

form {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-top: 20px;
}

form label {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 13px;
  color: var(--muted);
}

.submit {
  margin-top: 4px;
}

.auth-foot {
  display: flex;
  justify-content: center;
  gap: 6px;
  margin-top: 16px;
  font-size: 13px;
}

.auth-foot a {
  color: var(--primary);
}
</style>

