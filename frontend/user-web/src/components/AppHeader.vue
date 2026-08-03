<template>
  <header class="header">
    <div class="container header-inner">
      <RouterLink to="/" class="brand">
        <span class="brand-mark">D</span>
        <span>数码商城</span>
      </RouterLink>
      <nav class="nav">
        <RouterLink to="/">首页</RouterLink>
        <RouterLink to="/products">全部商品</RouterLink>
        <RouterLink to="/seckill">限时秒杀</RouterLink>
        <RouterLink to="/promotions">优惠活动</RouterLink>
        <RouterLink v-if="auth.isLogin" to="/orders">我的订单</RouterLink>
        <RouterLink to="/messages" v-if="auth.isLogin">
          消息
          <span v-if="unread > 0" class="badge">{{ unread > 99 ? '99+' : unread }}</span>
        </RouterLink>
      </nav>
      <div class="header-actions">
        <form class="search" @submit.prevent="goSearch">
          <Search :size="16" />
          <input v-model="keyword" placeholder="搜索手机、笔记本、耳机..." />
        </form>
        <RouterLink to="/cart" class="icon-btn" title="购物车">
          <ShoppingCart :size="20" />
        </RouterLink>
        <template v-if="auth.isLogin">
          <RouterLink to="/profile" class="user-chip">{{ auth.user?.nickname || auth.user?.username }}</RouterLink>
          <button class="icon-btn" title="退出登录" @click="logout">
            <LogOut :size="18" />
          </button>
        </template>
        <template v-else>
          <RouterLink to="/login" class="login-link">登录</RouterLink>
          <RouterLink to="/register" class="login-link">注册</RouterLink>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { LogOut, Search, ShoppingCart } from 'lucide-vue-next'
import { notifyApi } from '../api'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const keyword = ref('')
const unread = ref(0)

const goSearch = () => {
  router.push({ path: '/products', query: { name: keyword.value } })
}

const logout = async () => {
  await auth.logout()
  router.push('/')
}

const loadUnread = async () => {
  if (!auth.isLogin) return
  try {
    unread.value = await notifyApi.unreadCount()
  } catch (e) {
    unread.value = 0
  }
}

onMounted(loadUnread)
watch(() => auth.isLogin, loadUnread)
</script>

<style scoped>
.header {
  background: #fff;
  border-bottom: 1px solid var(--line);
  position: sticky;
  top: 0;
  z-index: 20;
}

.header-inner {
  height: 64px;
  display: flex;
  align-items: center;
  gap: 28px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: 800;
  letter-spacing: 0;
}

.brand-mark {
  width: 34px;
  height: 34px;
  border-radius: 8px;
  background: var(--primary);
  color: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 900;
}

.nav {
  display: flex;
  gap: 22px;
  font-size: 14px;
  color: var(--ink);
}

.nav a {
  position: relative;
  padding: 4px 2px;
}

.nav a.router-link-active {
  color: var(--primary);
  font-weight: 600;
}

.badge {
  position: absolute;
  top: -6px;
  right: -14px;
  background: var(--primary);
  color: #fff;
  font-size: 11px;
  border-radius: 9px;
  padding: 1px 5px;
  min-width: 18px;
  text-align: center;
}

.header-actions {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 12px;
}

.search {
  display: flex;
  align-items: center;
  gap: 8px;
  background: var(--bg);
  border: 1px solid var(--line);
  border-radius: 20px;
  padding: 0 14px;
  height: 38px;
  width: 240px;
}

.search input {
  border: 0;
  background: transparent;
  outline: none;
  width: 100%;
  font-size: 13px;
}

.icon-btn {
  width: 38px;
  height: 38px;
  border: 1px solid var(--line);
  background: #fff;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.icon-btn:hover {
  border-color: var(--primary);
  color: var(--primary);
}

.user-chip {
  font-size: 14px;
  color: var(--ink);
}

.login-link {
  color: var(--primary);
  font-weight: 600;
  font-size: 14px;
}

@media (max-width: 900px) {
  .nav { display: none; }
  .search { width: 160px; }
}
</style>
