<template>
  <div class="auth-page">
    <div class="auth-card panel">
      <h1>注册新账号</h1>
      <form @submit.prevent="submit">
        <div class="avatar-row">
          <img v-if="form.avatar" :src="form.avatar" class="avatar-preview" alt="头像" />
          <div v-else class="avatar-placeholder">头像</div>
          <label class="upload-label">
            选择头像
            <input type="file" accept="image/*" hidden @change="uploadAvatar" />
          </label>
        </div>
        <label>用户名<input v-model="form.username" class="input" placeholder="4-20位" /></label>
        <label>密码<input v-model="form.password" class="input" type="password" placeholder="6-20位" /></label>
        <label>昵称<input v-model="form.nickname" class="input" /></label>
        <label>手机号<input v-model="form.phone" class="input" placeholder="11位手机号（选填）" /></label>
        <label>邮箱<input v-model="form.email" class="input" type="email" placeholder="邮箱（选填）" /></label>
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
import { authApi } from '../api'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const form = reactive({ username: '', password: '', nickname: '', phone: '', email: '', avatar: '' })
const error = ref('')
const loading = ref(false)

const uploadAvatar = async event => {
  const file = event.target.files?.[0]
  if (!file) return
  error.value = ''
  const formData = new FormData()
  formData.append('file', file)
  try {
    form.avatar = await authApi.uploadAvatar(formData)
  } catch (e) {
    error.value = e.message
  }
}

const submit = async () => {
  error.value = ''
  if (form.username.length < 4 || form.username.length > 20) {
    error.value = '用户名长度需在4-20位'
    return
  }
  if (form.password.length < 6 || form.password.length > 20) {
    error.value = '密码长度需在6-20位'
    return
  }
  if (form.phone && !/^1[3-9]\d{9}$/.test(form.phone)) {
    error.value = '手机号格式不正确'
    return
  }
  if (form.email && !/^[\w.+-]+@[\w-]+(\.[\w-]+)+$/.test(form.email)) {
    error.value = '邮箱格式不正确'
    return
  }
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

.avatar-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar-preview,
.avatar-placeholder {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  object-fit: cover;
}

.avatar-placeholder {
  background: var(--bg);
  color: var(--muted);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}

.upload-label {
  color: var(--primary);
  border: 1px solid var(--primary);
  border-radius: 6px;
  padding: 8px 14px;
  font-size: 13px;
  cursor: pointer;
}
</style>
