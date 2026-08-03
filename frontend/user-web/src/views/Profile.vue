<template>
  <div class="container page">
    <h1 class="page-title">个人资料</h1>
    <div class="panel profile-card">
      <div class="profile-head">
        <img v-if="form.avatar" :src="form.avatar" class="avatar" alt="头像" />
        <div v-else class="avatar">{{ (auth.user?.nickname || auth.user?.username || 'U').slice(0, 1) }}</div>
        <div>
          <h2>{{ auth.user?.nickname || auth.user?.username }}</h2>
          <p class="muted">用户名：{{ auth.user?.username }}</p>
          <label class="upload-label">
            更换头像
            <input type="file" accept="image/*" hidden @change="uploadAvatar" />
          </label>
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

      <hr class="divider" />
      <h3>修改密码</h3>
      <form @submit.prevent="changePassword">
        <label>原密码<input v-model="pwd.oldPassword" class="input" type="password" autocomplete="current-password" /></label>
        <label>新密码<input v-model="pwd.newPassword" class="input" type="password" placeholder="6-20位" autocomplete="new-password" /></label>
        <p class="notice-text">{{ pwdNotice }}</p>
        <p class="error-text">{{ pwdError }}</p>
        <button class="btn btn-primary" :disabled="pwdSaving" type="submit">{{ pwdSaving ? '修改中...' : '修改密码' }}</button>
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
const pwdSaving = ref(false)
const pwdError = ref('')
const pwdNotice = ref('')
const form = reactive({ nickname: '', phone: '', email: '', avatar: '' })
const pwd = reactive({ oldPassword: '', newPassword: '' })

const uploadAvatar = async event => {
  const file = event.target.files?.[0]
  if (!file) return
  error.value = ''
  const formData = new FormData()
  formData.append('file', file)
  try {
    form.avatar = await authApi.uploadAvatar(formData)
    notice.value = '头像已上传，保存资料后生效'
  } catch (e) {
    error.value = e.message
  }
}

const changePassword = async () => {
  pwdError.value = ''
  pwdNotice.value = ''
  if (!pwd.oldPassword || pwd.newPassword.length < 6 || pwd.newPassword.length > 20) {
    pwdError.value = '请填写原密码，新密码长度需在6-20位'
    return
  }
  pwdSaving.value = true
  try {
    await authApi.changePassword(pwd)
    pwdNotice.value = '密码修改成功'
    pwd.oldPassword = ''
    pwd.newPassword = ''
  } catch (e) {
    pwdError.value = e.message
  } finally {
    pwdSaving.value = false
  }
}

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

.upload-label {
  display: inline-block;
  color: var(--primary);
  border: 1px solid var(--primary);
  border-radius: 6px;
  padding: 5px 12px;
  font-size: 12px;
  cursor: pointer;
  margin-top: 6px;
}

h3 {
  margin: 24px 0 12px;
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

.divider {
  border: 0;
  border-top: 1px solid var(--line);
  margin: 24px 0;
}
</style>
