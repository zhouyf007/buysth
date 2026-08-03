<template>
  <div class="container page">
    <h1 class="page-title">个人资料</h1>
    <div class="panel profile-card">
      <div class="profile-head">
        <div class="avatar">{{ (auth.user?.nickname || auth.user?.username || 'U').slice(0, 1) }}</div>
        <div>
          <h2>{{ auth.user?.nickname || auth.user?.username }}</h2>
          <p class="muted">用户名：{{ auth.user?.username }}</p>
        </div>
      </div>
      <form @submit.prevent="save">
        <label>昵称<input v-model="form.nickname" class="input" /></label>
        <label>手机号<input v-model="form.phone" class="input" /></label>
        <label>邮箱<input v-model="form.email" class="input" type="email" /></label>
        <label>头像地址<input v-model="form.avatar" class="input" placeholder="可选" /></label>
        <p v-if="notice" class="notice-text">{{ notice }}</p>
        <p class="error-text">{{ error }}</p>
        <button class="btn btn-primary" :disabled="saving" type="submit">{{ saving ? '保存中...' : '保存资料' }}</button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { authApi } from '../api'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const saving = ref(false)
const error = ref('')
const notice = ref('')
const form = reactive({ nickname: '', phone: '', email: '', avatar: '' })

const save = async () => {
  error.value = ''
  notice.value = ''
  saving.value = true
  try {
    const user = await authApi.updateProfile(form)
    auth.user = user
    localStorage.setItem('shop_user', JSON.stringify(user))
    notice.value = '资料已更新'
  } catch (e) {
    error.value = e.message
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  if (!auth.user) {
    try {
      await auth.fetchMe()
    } catch (e) { /* ignore */ }
  }
  Object.assign(form, {
    nickname: auth.user?.nickname || '',
    phone: auth.user?.phone || '',
    email: auth.user?.email || '',
    avatar: auth.user?.avatar || ''
  })
})
</script>

<style scoped>
.page-title {
  margin: 0 0 18px;
  font-size: 22px;
}

.profile-card {
  max-width: 520px;
  padding: 28px;
}

.profile-head {
  display: flex;
  gap: 16px;
  align-items: center;
  margin-bottom: 24px;
}

.avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: var(--primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 700;
}

.profile-head h2 {
  margin: 0;
}

form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

form label {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 13px;
  color: var(--muted);
}

form .btn {
  align-self: flex-end;
  min-width: 120px;
}

.notice-text {
  color: #16a34a;
  font-size: 13px;
  margin: 0;
}
</style>
