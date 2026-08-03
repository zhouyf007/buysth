<template>
  <div class="container page">
    <h1 class="page-title">确认订单</h1>
    <div v-if="lines.length" class="checkout-layout">
      <div class="left">
        <section class="panel section">
          <h2>收货信息</h2>
          <div class="form-grid">
            <label>收货人<input v-model="address.receiverName" class="input" placeholder="姓名" /></label>
            <label>联系电话<input v-model="address.receiverPhone" class="input" placeholder="手机号" /></label>
            <label class="full">收货地址<input v-model="address.receiverAddress" class="input" placeholder="省市区 + 详细地址" /></label>
            <label class="full">订单备注<input v-model="remark" class="input" placeholder="选填" /></label>
          </div>
        </section>
        <section class="panel section">
          <h2>商品清单</h2>
          <div v-for="line in lines" :key="line.skuId" class="line">
            <img :src="line.image" :alt="line.productName" />
            <div class="line-info">
              <strong>{{ line.productName }}</strong>
              <p>{{ line.skuSpec }}</p>
            </div>
            <span>¥{{ line.price }} × {{ line.quantity }}</span>
          </div>
        </section>
      </div>
      <aside class="panel summary">
        <h2>结算</h2>
        <div class="row"><span>商品金额</span><span>¥{{ total }}</span></div>
        <div class="row"><span>优惠</span><span>-¥{{ discount }}</span></div>
        <label class="promo">优惠码
          <input v-model="promotionCode" class="input" placeholder="如 back-to-school" />
        </label>
        <div class="row pay"><span>应付</span><span class="price big">¥{{ payAmount }}</span></div>
        <button class="btn btn-primary submit" :disabled="submitting" @click="submit">提交订单</button>
        <p class="error-text">{{ error }}</p>
      </aside>
    </div>
    <div v-else class="panel empty">没有可结算的商品</div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { orderApi, productApi } from '../api'

const route = useRoute()
const router = useRouter()
const lines = ref([])
const address = ref({ receiverName: '', receiverPhone: '', receiverAddress: '' })
const remark = ref('')
const promotionCode = ref('')
const submitting = ref(false)
const error = ref('')
const discount = ref(0)

const total = computed(() => lines.value.reduce((s, l) => s + Number(l.price) * l.quantity, 0).toFixed(2))
const payAmount = computed(() => (Number(total.value) - Number(discount.value)).toFixed(2))

const loadDirect = async () => {
  const skuId = Number(route.query.skuId)
  const quantity = Number(route.query.quantity || 1)
  if (!skuId) return
  const product = await productApi.detail(route.query.productId || 1)
  const sku = product.skus.find(s => String(s.id) === String(skuId))
  if (sku) {
    lines.value = [{
      skuId: sku.id,
      productId: product.id,
      productName: product.name,
      skuSpec: `${sku.specName} ${sku.specValue}`,
      image: sku.image || product.mainImage,
      price: Number(sku.price),
      quantity
    }]
  }
}

const loadCart = async () => {
  const items = await orderApi.cart()
  lines.value = items.filter(i => i.checked).map(i => ({
    skuId: i.skuId,
    productId: i.productId,
    productName: i.productName,
    skuSpec: i.skuSpec,
    image: i.image,
    price: Number(i.price),
    quantity: i.quantity
  }))
}

const submit = async () => {
  if (!address.value.receiverName || !address.value.receiverPhone || !address.value.receiverAddress) {
    error.value = '请完整填写收货信息'
    return
  }
  submitting.value = true
  error.value = ''
  try {
    const data = await orderApi.create({
      items: lines.value.map(l => ({ skuId: l.skuId, quantity: l.quantity })),
      address: address.value,
      remark: remark.value,
      promotionCode: promotionCode.value || undefined
    })
    router.push({ path: `/orders/${data.orderNo}`, query: { created: 1 } })
  } catch (e) {
    error.value = e.message
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  try {
    if (route.query.skuId) {
      await loadDirect()
    } else {
      await loadCart()
    }
  } catch (e) {
    error.value = e.message
  }
})
</script>

<style scoped>
.page-title {
  margin: 0 0 18px;
  font-size: 22px;
}

.checkout-layout {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 20px;
  align-items: start;
}

.section {
  padding: 20px;
  margin-bottom: 16px;
}

.section h2, .summary h2 {
  margin: 0 0 16px;
  font-size: 16px;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

.form-grid label {
  font-size: 13px;
  color: var(--muted);
  display: flex;
  flex-direction: column;
  gap: 6px;
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
  width: 56px;
  height: 56px;
  object-fit: cover;
  border-radius: 6px;
  background: #fafafa;
}

.line-info {
  flex: 1;
}

.line-info strong {
  font-size: 14px;
}

.line-info p {
  margin: 4px 0 0;
  color: var(--muted);
  font-size: 12px;
}

.summary {
  padding: 20px;
  position: sticky;
  top: 84px;
}

.row {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  padding: 8px 0;
}

.row.pay {
  border-top: 1px solid var(--line);
  margin-top: 8px;
  padding-top: 14px;
  align-items: baseline;
}

.price.big {
  font-size: 22px;
}

.promo {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 13px;
  color: var(--muted);
  margin: 8px 0 4px;
}

.submit {
  width: 100%;
  margin-top: 16px;
}

@media (max-width: 860px) {
  .checkout-layout { grid-template-columns: 1fr; }
  .form-grid { grid-template-columns: 1fr; }
}
</style>
