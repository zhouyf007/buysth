<template>
  <div class="container page">
    <div class="seckill-head">
      <div>
        <h1>{{ type === 'SECKILL' ? '限时秒杀' : '优惠活动' }}</h1>
        <p class="muted">{{ type === 'SECKILL' ? '限时限量，抢完即止' : '支付时输入活动优惠码即可享受折扣' }}</p>
      </div>
      <RouterLink v-if="type === 'PROMOTION'" to="/seckill" class="btn btn-ghost">查看秒杀</RouterLink>
      <RouterLink v-else to="/promotions" class="btn btn-ghost">查看优惠</RouterLink>
    </div>

    <div v-if="activities.length" class="activity-list">
      <div v-for="activity in activities" :key="activity.id" class="panel activity-card">
        <div class="activity-info">
          <span class="type-tag">{{ activity.type === 'SECKILL' ? '秒杀' : '优惠' }}</span>
          <h2>{{ activity.name }}</h2>
          <p>{{ activity.description }}</p>
          <div class="time-row">
            <span v-if="type === 'SECKILL'">距结束</span>
            <strong>{{ countdown(activity.endTime) }}</strong>
          </div>
          <span v-if="activity.type === 'PROMOTION' && activity.discountType === 'PERCENT'" class="promo-tip">
            优惠码：{{ activity.promotionCode }} · 享 {{ activity.discountValue }} 折
          </span>
          <span v-if="activity.type === 'PROMOTION' && activity.discountType === 'FIXED'" class="promo-tip">
            优惠码：{{ activity.promotionCode }} · 立减 ¥{{ activity.discountValue }}
          </span>
        </div>
        <div v-if="activity.products?.length" class="product-grid">
          <div v-for="p in activity.products" :key="p.id" class="seckill-product">
            <img :src="p.image" :alt="p.productName" />
            <strong>{{ p.productName }}</strong>
            <p v-if="p.skuSpec" class="muted">{{ p.skuSpec }}</p>
            <div class="price-row">
              <span class="price big">¥{{ p.seckillPrice }}</span>
              <span v-if="p.remainStock !== null" class="muted">剩余 {{ p.remainStock }}</span>
            </div>
            <button
              v-if="activity.type === 'SECKILL'"
              class="btn btn-primary buy-btn"
              :disabled="buyingId === p.id"
              @click="buy(activity.id, p.id)"
            >{{ buyingId === p.id ? '抢购中' : '立即抢购' }}</button>
          </div>
        </div>
        <p v-else-if="activity.type === 'SECKILL'" class="muted">秒杀商品配置中</p>
      </div>
    </div>
    <div v-if="type === 'PROMOTION' && promoProducts.length" class="panel activity-card">
      <div class="activity-info">
        <span class="type-tag">优惠商品</span>
        <h2>可享优惠的商品</h2>
        <p>加入购物车后在结算页输入活动优惠码，即可自动享受折扣。</p>
      </div>
      <div class="product-grid">
        <div v-for="p in promoProducts" :key="p.id" class="seckill-product">
          <RouterLink :to="`/products/${p.id}`">
            <img :src="p.mainImage" :alt="p.name" />
          </RouterLink>
          <RouterLink :to="`/products/${p.id}`" class="promo-link">
            <strong>{{ p.name }}</strong>
          </RouterLink>
          <p v-if="p.region" class="muted">{{ p.region }}</p>
          <div class="price-row">
            <span class="muted strike">¥{{ p.minPrice }}</span>
            <span class="price big">¥{{ discounted(p.minPrice) }}</span>
          </div>
          <button class="btn btn-primary buy-btn" @click="buyPromo(p)">加入购物车</button>
        </div>
      </div>
    </div>
    <div v-if="!activities.length && !(type === 'PROMOTION' && promoProducts.length)" class="panel empty">暂无进行中的活动</div>

    <div v-if="resultOrderNo" class="panel success-box">
      <h3>抢购成功！</h3>
      <p>订单号 {{ resultOrderNo }}，请尽快补充收货信息并完成支付。</p>
      <RouterLink :to="`/orders/${resultOrderNo}`" class="btn btn-primary">去完成支付</RouterLink>
    </div>
    <p v-if="error" class="error-text center">{{ error }}</p>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { orderApi, productApi, seckillApi } from '../api'
import { useAuthStore } from '../stores/auth'

const props = defineProps({ type: { type: String, default: 'SECKILL' } })
const router = useRouter()
const auth = useAuthStore()
const activities = ref([])
const promoProducts = ref([])
const buyingId = ref(null)
const resultOrderNo = ref('')
const error = ref('')
const now = ref(Date.now())

const filtered = computed(() => activities.value.filter(a => a.type === props.type))
const promoActivity = computed(() => activities.value.find(a => a.type === 'PROMOTION'))

let timer

const countdown = end => {
  const diff = new Date(end).getTime() - now.value
  if (diff <= 0) return '已结束'
  const h = Math.floor(diff / 3600000)
  const m = Math.floor(diff % 3600000 / 60000)
  const s = Math.floor(diff % 60000 / 1000)
  return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

const buy = async (activityId, productId) => {
  error.value = ''
  resultOrderNo.value = ''
  if (!auth.isLogin) {
    router.push('/login')
    return
  }
  buyingId.value = productId
  try {
    const result = await seckillApi.buy(activityId, productId)
    if (result.success) {
      resultOrderNo.value = result.orderNo
    } else {
      error.value = result.message
    }
  } catch (e) {
    error.value = e.message
  } finally {
    buyingId.value = null
    load()
  }
}

const discounted = price => {
  const a = promoActivity.value
  const n = Number(price || 0)
  if (!a || !n) return n
  if (a.discountType === 'PERCENT') {
    return (n * Number(a.discountValue) / 100).toFixed(2)
  }
  if (a.discountType === 'FIXED') {
    return Math.max(n - Number(a.discountValue), 0).toFixed(2)
  }
  return n.toFixed(2)
}

const buyPromo = async product => {
  if (!auth.isLogin) {
    router.push('/login')
    return
  }
  error.value = ''
  const sku = product.skus?.[0]
  if (!sku) {
    error.value = '该商品暂无可售规格'
    return
  }
  try {
    await orderApi.addCart({ skuId: sku.id, quantity: 1 })
    router.push('/cart')
  } catch (e) {
    error.value = e.message
  }
}

const load = async () => {
  const data = await seckillApi.activities({ current: 1, size: 20, type: props.type })
  activities.value = data.records
  if (props.type === 'PROMOTION') {
    try {
      promoProducts.value = await productApi.hot()
    } catch (e) {
      promoProducts.value = []
    }
  }
}

watch(() => props.type, () => {
  activities.value = []
  promoProducts.value = []
  error.value = ''
  load()
})

onMounted(() => {
  load()
  timer = setInterval(() => { now.value = Date.now() }, 1000)
})

onUnmounted(() => clearInterval(timer))
</script>

<style scoped>
.seckill-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.seckill-head h1 {
  margin: 0;
  font-size: 26px;
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.activity-card {
  padding: 22px;
}

.type-tag {
  background: var(--primary);
  color: #fff;
  font-size: 12px;
  border-radius: 4px;
  padding: 3px 8px;
}

.activity-info h2 {
  margin: 10px 0 6px;
  font-size: 18px;
}

.activity-info p {
  color: var(--muted);
  margin: 0;
  font-size: 13px;
}

.time-row {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-top: 12px;
  color: var(--muted);
  font-size: 13px;
}

.time-row strong {
  color: var(--primary);
  font-size: 18px;
}

.promo-tip {
  display: inline-block;
  margin-top: 10px;
  background: #fff7f2;
  color: var(--primary);
  border-radius: 6px;
  padding: 6px 10px;
  font-size: 13px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
  margin-top: 18px;
}

.seckill-product {
  border: 1px solid var(--line);
  border-radius: var(--radius);
  padding: 12px;
  text-align: center;
}

.seckill-product img {
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
  border-radius: 6px;
  background: #fafafa;
}

.seckill-product strong {
  display: block;
  margin-top: 10px;
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.seckill-product p {
  margin: 4px 0;
  font-size: 12px;
}

.price-row {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin: 8px 0;
}

.strike {
  text-decoration: line-through;
}

.promo-link {
  display: block;
  color: inherit;
}

.price.big {
  font-size: 20px;
}

.buy-btn {
  width: 100%;
}

.success-box {
  margin-top: 18px;
  padding: 22px;
  text-align: center;
  border-color: #ffc9a8;
}

.success-box h3 {
  color: var(--primary);
  margin: 0 0 8px;
}

.center {
  text-align: center;
}

@media (max-width: 760px) {
  .product-grid { grid-template-columns: 1fr; }
}
</style>
