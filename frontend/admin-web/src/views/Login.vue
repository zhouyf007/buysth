<template>
  <div class="login-page">
    <el-card class="login-card">
      <h2>数码商城管理后台</h2>
      <p class="tip">演示管理员 admin / 123456</p>
      <el-form :model="form" label-position="top" @keyup.enter="submit">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="密码" />
        </el-form-item>
        <el-button type="primary" class="submit" :loading="loading" @click="submit">登录</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const form = reactive({ username: 'admin', password: '123456' })
const loading = ref(false)

const submit = async () => {
  loading.value = true
  try {
    await auth.login(form)
    router.push(route.query.redirect || '/admin/dashboard')
  } catch (e) {
    ElMessage.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #1f2937;
}

.login-card {
  width: 380px;
  padding: 12px;
}

h2 {
  margin: 0 0 4px;
}

.tip {
  color: #9ca3af;
  font-size: 12px;
  margin: 0 0 18px;
}

.submit {
  width: 100%;
}
</style>

