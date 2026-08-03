import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const routes = [
  { path: '/', name: 'home', component: () => import('../views/Home.vue') },
  { path: '/products', name: 'products', component: () => import('../views/ProductList.vue') },
  { path: '/products/:id', name: 'product-detail', component: () => import('../views/ProductDetail.vue') },
  { path: '/seckill', name: 'seckill', component: () => import('../views/Seckill.vue'), props: { type: 'SECKILL' } },
  { path: '/promotions', name: 'promotions', component: () => import('../views/Seckill.vue'), props: { type: 'PROMOTION' } },
  { path: '/cart', name: 'cart', component: () => import('../views/Cart.vue'), meta: { auth: true } },
  { path: '/checkout', name: 'checkout', component: () => import('../views/Checkout.vue'), meta: { auth: true } },
  { path: '/orders', name: 'orders', component: () => import('../views/OrderList.vue'), meta: { auth: true } },
  { path: '/orders/:orderNo', name: 'order-detail', component: () => import('../views/OrderDetail.vue'), meta: { auth: true } },
  { path: '/messages', name: 'messages', component: () => import('../views/Messages.vue'), meta: { auth: true } },
  { path: '/profile', name: 'profile', component: () => import('../views/Profile.vue'), meta: { auth: true } },
  { path: '/login', name: 'login', component: () => import('../views/Login.vue') },
  { path: '/register', name: 'register', component: () => import('../views/Register.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

router.beforeEach(to => {
  const auth = useAuthStore()
  if (to.meta.auth && !auth.isLogin) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  return true
})

export default router
