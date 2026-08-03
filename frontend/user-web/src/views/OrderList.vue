<template>
  <div class="container page">
    <h1 class="page-title">我的订单</h1>
    <div class="tabs">
      <button :class="{ active: !status }" @click="switchStatus('')">全部</button>
      <button :class="{ active: status === 'PENDING_PAY' }" @click="switchStatus('PENDING_PAY')">待支付</button>
      <button :class="{ active: status === 'PAID' }" @click="switchStatus('PAID')">已支付</button>
      <button :class="{ active: status === 'SHIPPED' }" @click="switchStatus('SHIPPED')">已发货</button>
      <button :class="{ active: status === 'COMPLETED' }" @click="switchStatus('COMPLETED')">已完成</button>
    </div>
    <div v-if="orders.length" class="order-list">
      <div v-for="o in orders" :key="o.orderNo" class="panel order-card" @click="$router.push(`/orders/${o.orderNo}`)">
        <div class="order-head">
          <span>订单号 {{ o.orderNo }}</span>
          <span class="status">{{ o.statusText }}</span>
        </div>
        <div class="order-body">
          <div class="thumbnails">
            <img v-for="item in o.items.slice(0, 4)" :key="item.id" :src="item.image" :alt="item.productName" />
          </div>
          <div class="order-meta">
            <span>{{ o.items.length }} 件商品</span>
            <span class="price">¥{{ o.payAmount }}</span>
            <span class="muted">{{ formatTime(o.createTime) }}</span>
          </div>
        </div>
      </div>
    </div>
    <div v-else class="panel empty">暂无订单</div>
    <div class="pager">
      <button class="btn btn-ghost" :disabled="current <= 1" @click="load(current - 1)">上一页</button>
      <span class="muted">{{ current }} / {{ pages || 1 }}</span>
      <button class="btn btn-ghost" :disabled="current >= pages" @click="load(current + 1)">下一页</button>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { orderApi } from '../api'

const orders = ref([])
const status = ref('')
const current = ref(1)
const pages = ref(1)

const load = async page => {
  current.value = page
  const data = await orderApi.list({ current: page, size: 8, status: status.value || undefined })
  orders.value = data.records
  pages.value = data.pages
}

const switchStatus = s => {
  status.value = s
  load(1)
}

const formatTime = t => (t || '').replace('T', ' ').slice(0, 16)

onMounted(() => load(1))
</script>

<style scoped>
.page-title {
  margin: 0 0 18px;
  font-size: 22px;
}

.tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.tabs button {
  border: 1px solid var(--line);
  background: #fff;
  border-radius: 20px;
  padding: 8px 18px;
  cursor: pointer;
  font-size: 13px;
}

.tabs button.active {
  border-color: var(--primary);
  color: var(--primary);
  background: #fff7f2;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.order-card {
  cursor: pointer;
  transition: box-shadow .15s ease;
}

.order-card:hover {
  box-shadow: 0 6px 18px rgba(31, 35, 40, .07);
}

.order-head {
  display: flex;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid var(--line);
  font-size: 13px;
  color: var(--muted);
}

.order-head .status {
  color: var(--primary);
  font-weight: 600;
}

.order-body {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  gap: 16px;
}

.thumbnails {
  display: flex;
  gap: 8px;
}

.thumbnails img {
  width: 64px;
  height: 64px;
  object-fit: cover;
  border-radius: 6px;
  background: #fafafa;
}

.order-meta {
  display: flex;
  align-items: center;
  gap: 18px;
  font-size: 13px;
}

.pager {
  display: flex;
  justify-content: center;
  gap: 16px;
  align-items: center;
  margin-top: 20px;
}
</style>

