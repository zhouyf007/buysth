<template>
  <div class="auth-page">
    <div class="auth-card panel">
      <h1>注册新账号</h1>
      <form @submit.prevent="submit">
        <label>用户名<input v-model="form.username" class="input" placeholder="4-20位" /></label>
        <label>密码<input v-model="form.password" class="input" type="password" placeholder="6-20位" /></label>
        <label>昵称<input v-model="form.nickname" class="input" /></label>
        <label>手机号<input v-model="form.phone" class="input" /></label>
        <label>邮箱<input v-model="form.email" class="input" type="email" /></label>
        <p class="error-text">{{ error }}</p>
        <button class="btn btn-primary submit" :disabled="loading">{{ loading ? '注册中...' : '注册' }}</button>
      </form>
      <div class="auth-foot">
        <span class="muted">已有账号？</span>
        <RouterLink to="/login">去登录</RouterLink>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const form = reactive({ username: '', password: '', nickname: '', phone: '', email: '' })
const error = ref('')
const loading = ref(false)

const submit = async () => {
  error.value = ''
  loading.value = true
  try {
    await auth.register(form)
    router.push('/login')
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
  width: 420px;
  padding: 32px;
}

.auth-card h1 {
  margin: 0 0 6px;
  font-size: 22px;
}

form {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 18px;
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

