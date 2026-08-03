import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const routes = [
  { path: '/', redirect: '/admin/login' },
  { path: '/admin/login', name: 'admin-login', component: () => import('../views/Login.vue') },
  {
    path: '/admin',
    component: () => import('../layout/AdminLayout.vue'),
    redirect: '/admin/dashboard',
    meta: { auth: true },
    children: [
      { path: 'dashboard', component: () => import('../views/Dashboard.vue'), meta: { title: '工作台' } },
      { path: 'categories', component: () => import('../views/CategoryList.vue'), meta: { title: '分类管理' } },
      { path: 'products', component: () => import('../views/ProductList.vue'), meta: { title: '商品管理' } },
      { path: 'products/edit/:id?', component: () => import('../views/ProductEdit.vue'), meta: { title: '商品编辑' } },
      { path: 'orders', component: () => import('../views/OrderList.vue'), meta: { title: '订单管理' } },
      { path: 'payments', component: () => import('../views/PaymentList.vue'), meta: { title: '支付管理' } },
      { path: 'activities', component: () => import('../views/ActivityList.vue'), meta: { title: '活动管理' } },
      { path: 'activities/edit/:id?', component: () => import('../views/ActivityEdit.vue'), meta: { title: '活动编辑' } },
      { path: 'logistics', component: () => import('../views/LogisticsList.vue'), meta: { title: '物流管理' } },
      { path: 'users', component: () => import('../views/UserList.vue'), meta: { title: '用户管理' } },
      { path: 'announcements', component: () => import('../views/AnnouncementList.vue'), meta: { title: '公告管理' } },
      { path: 'roles', component: () => import('../views/RoleList.vue'), meta: { title: '角色管理' } },
      { path: 'messages', component: () => import('../views/MessageList.vue'), meta: { title: '消息记录' } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(to => {
  const auth = useAuthStore()
  if (to.meta.auth && !auth.isLogin) {
    return { path: '/admin/login', query: { redirect: to.fullPath } }
  }
  if (to.path === '/admin/login' && auth.isLogin) {
    return '/admin/dashboard'
  }
  return true
})

export default router
