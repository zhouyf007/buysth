<template>
  <el-container class="admin-layout">
    <el-aside width="220px" class="aside">
      <div class="logo">数码商城管理端</div>
      <el-menu :default-active="activeMenu" router background-color="#1f2937" text-color="#cbd5e1" active-text-color="#ffffff">
        <el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span>{{ currentTitle }}</span>
        <div class="user-box">
          <el-avatar :size="30">{{ auth.user?.nickname?.slice(0, 1) || '管' }}</el-avatar>
          <span>{{ auth.user?.nickname || auth.user?.username }}</span>
          <el-button link type="danger" @click="logout">退出</el-button>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const menus = [
  { path: '/admin/dashboard', title: '工作台', icon: 'Odometer' },
  { path: '/admin/categories', title: '分类管理', icon: 'Folder' },
  { path: '/admin/products', title: '商品管理', icon: 'Goods' },
  { path: '/admin/orders', title: '订单管理', icon: 'Tickets' },
  { path: '/admin/payments', title: '支付管理', icon: 'Wallet' },
  { path: '/admin/activities', title: '活动管理', icon: 'Promotion' },
  { path: '/admin/logistics', title: '物流管理', icon: 'Van' },
  { path: '/admin/users', title: '用户管理', icon: 'User' },
  { path: '/admin/announcements', title: '公告管理', icon: 'Bell' },
  { path: '/admin/messages', title: '消息记录', icon: 'ChatDotRound' },
  { path: '/admin/roles', title: '角色管理', icon: 'Setting' }
]

const activeMenu = computed(() => {
  if (route.path.startsWith('/admin/products')) return '/admin/products'
  if (route.path.startsWith('/admin/activities')) return '/admin/activities'
  return route.path
})
const currentTitle = computed(() => menus.find(m => m.path === activeMenu.value)?.title || route.meta.title || '')

const logout = () => {
  auth.logout()
  router.push('/admin/login')
}
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
}

.aside {
  background: #1f2937;
}

.logo {
  color: #fff;
  font-weight: 700;
  padding: 18px 16px;
  font-size: 16px;
  border-bottom: 1px solid #374151;
}

.aside :deep(.el-menu) {
  border-right: 0;
}

.header {
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
}

.user-box {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 400;
}
</style>

