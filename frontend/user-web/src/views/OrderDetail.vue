<template>
  <div class="container page" v-if="order">
    <div class="detail-head panel">
      <div>
        <h1>订单详情</h1>
        <p class="muted">订单号 {{ order.orderNo }} · {{ formatTime(order.createTime) }}</p>
      </div>
      <span class="status-badge">{{ order.statusText }}</span>
    </div>

    <section v-if="order.orderType === 'SECKILL' && order.status === 'PENDING_PAY' && !order.receiverName" class="panel section">
      <h2>秒杀订单需要补充收货信息</h2>
      <div class="form-grid">
        <label>收货人<input v-model="address.receiverName" class="input" placeholder="姓名" /></label>
        <label>联系电话<input v-model="address.receiverPhone" class="input" placeholder="手机号" /></label>
        <label class="full">收货地址<input v-model="address.receiverAddress" class="input" placeholder="详细地址" /></label>
      </div>
      <button class="btn btn-primary" :disabled="!address.receiverName || !address.receiverPhone || !address.receiverAddress" @click="saveAddress">保存并支付</button>
      <p class="error-text">{{ error }}</p>
    </section>

    <section class="panel section">
      <h2>商品信息</h2>
      <div v-for="item in order.items" :key="item.id" class="line">
        <img :src="item.image" :alt="item.productName" />
        <div class="line-info">
          <strong>{{ item.productName }}</strong>
          <p>{{ item.skuSpec }}</p>
        </div>
        <span>¥{{ item.price }} × {{ item.quantity }}</span>
        <span class="price">¥{{ item.subtotal }}</span>
      </div>
      <div class="amount-line">
        <span>商品金额 ¥{{ order.totalAmount }}</span>
        <span v-if="Number(order.discountAmount) > 0">优惠 -¥{{ order.discountAmount }}</span>
        <span class="price big">应付 ¥{{ order.payAmount }}</span>
      </div>
    </section>

    <section class="panel section">
      <h2>收货信息</h2>
      <p class="address">{{ order.receiverName }} {{ order.receiverPhone }}</p>
      <p class="muted">{{ order.receiverAddress }}</p>
    </section>

    <section v-if="shipment" class="panel section">
      <h2>物流信息</h2>
      <p>{{ shipment.companyName }} · 运单号 {{ shipment.trackingNo }} · {{ shipment.status }}</p>
      <div class="tracks">
        <div v-for="t in shipment.tracks" :key="t.id" class="track">
          <span>{{ t.description }}</span>
          <span class="muted">{{ formatTime(t.trackTime) }}</span>
        </div>
      </div>
    </section>

    <section class="panel section actions">
      <template v-if="order.status === 'PENDING_PAY'">
        <button class="btn btn-primary" @click="pay">去支付</button>
        <button class="btn btn-ghost" @click="cancel">取消订单</button>
      </template>
      <button v-if="order.status === 'SHIPPED'" class="btn btn-primary" @click="confirm">确认收货</button>
      <p class="error-text">{{ error }}</p>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { logisticsApi, orderApi, payApi } from '../api'

const route = useRoute()
const order = ref(null)
const shipment = ref(null)
const address = ref({ receiverName: '', receiverPhone: '', receiverAddress: '' })
const error = ref('')

const formatTime = t => (t || '').replace('T', ' ').slice(0, 16)

const load = async () => {
  order.value = await orderApi.detail(route.params.orderNo)
  if (order.value.receiverName) {
    address.value = {
      receiverName: order.value.receiverName,
      receiverPhone: order.value.receiverPhone,
      receiverAddress: order.value.receiverAddress
    }
  }
  try {
    shipment.value = await logisticsApi.track(route.params.orderNo)
  } catch (e) {
    shipment.value = null
  }
}

const saveAddress = async () => {
  error.value = ''
  try {
    await orderApi.address(order.value.orderNo, address.value)
    await load()
  } catch (e) {
    error.value = e.message
  }
}

const pay = async () => {
  error.value = ''
  try {
    const data = await payApi.create(order.value.orderNo)
    const redirect = encodeURIComponent(`${window.location.origin}/orders`)
    window.open(`${data.payUrl}?redirect=${redirect}`, '_blank')
    await waitPaid()
  } catch (e) {
    error.value = e.message
  }
}

const waitPaid = async () => {
  for (let i = 0; i < 30; i++) {
    await new Promise(r => setTimeout(r, 2000))
    const status = await payApi.status(order.value.orderNo)
    if (status?.status === 'SUCCESS') {
      await load()
      return
    }
  }
}

const cancel = async () => {
  try {
    await orderApi.cancel(order.value.orderNo)
    await load()
  } catch (e) {
    error.value = e.message
  }
}

const confirm = async () => {
  try {
    await orderApi.confirm(order.value.orderNo)
    await load()
  } catch (e) {
    error.value = e.message
  }
}

onMounted(load)
</script>

<style scoped>
.detail-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  margin-bottom: 16px;
}

.detail-head h1 {
  margin: 0;
  font-size: 20px;
}

.status-badge {
  background: #fff2ec;
  color: var(--primary);
  font-weight: 700;
  border-radius: 6px;
  padding: 6px 14px;
}

.section {
  padding: 20px;
  margin-bottom: 16px;
}

.section h2 {
  margin: 0 0 16px;
  font-size: 16px;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
  margin-bottom: 16px;
}

.form-grid label {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 13px;
  color: var(--muted);
}

.form-grid .full {
  grid-column: 1 / -1;
}

.line {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  border-top: 1px solid var(--line);
  font-size: 13px;
}

.line img {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 6px;
}

.line-info {
  flex: 1;
}

.line-info p {
  margin: 4px 0 0;
  color: var(--muted);
  font-size: 12px;
}

.amount-line {
  display: flex;
  justify-content: flex-end;
  gap: 20px;
  align-items: baseline;
  border-top: 1px solid var(--line);
  margin-top: 10px;
  padding-top: 14px;
}

.price.big {
  font-size: 20px;
}

.address {
  font-weight: 600;
  margin: 0;
}

.tracks {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.track {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  border-left: 2px solid var(--primary);
  padding-left: 12px;
}

.actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

@media (max-width: 720px) {
  .form-grid { grid-template-columns: 1fr; }
}
</style>
