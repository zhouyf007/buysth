<template>
  <div class="container page" v-if="product">
    <nav class="crumbs">
      <RouterLink to="/products">全部商品</RouterLink>
      <span>/</span>
      <span>{{ product.categoryName }}</span>
      <span>/</span>
      <span>{{ product.name }}</span>
    </nav>
    <div class="detail-layout">
      <div class="gallery">
        <img :src="selectedImage" :alt="product.name" />
      </div>
      <div class="detail-info panel">
        <h1>{{ product.name }}</h1>
        <p class="subtitle">{{ product.subtitle }}</p>
        <div class="price-box">
          <span class="label">价格</span>
          <span class="price big">¥{{ currentPrice }}</span>
          <span v-if="sku?.stock !== null" class="muted">库存 {{ sku?.stock }}</span>
        </div>
        <dl class="facts">
          <div><dt>品牌</dt><dd>{{ product.brand }}</dd></div>
          <div><dt>产地</dt><dd>{{ product.region }}</dd></div>
          <div><dt>上新</dt><dd>{{ formatTime(product.publishDate) }}</dd></div>
          <div><dt>销量</dt><dd>{{ product.sales }}</dd></div>
          <div><dt>评分</dt><dd>{{ product.rating }}</dd></div>
        </dl>
        <div v-if="product.skus?.length" class="sku-group">
          <span class="sku-label">{{ product.skus[0].specName }}</span>
          <div class="sku-options">
            <button
              v-for="s in product.skus"
              :key="s.id"
              :class="['sku-option', { active: sku?.id === s.id }]"
              @click="selectSku(s)"
            >{{ s.specValue }}</button>
          </div>
        </div>
        <div class="qty-row">
          <span>数量</span>
          <div class="stepper">
            <button @click="quantity = Math.max(1, quantity - 1)">-</button>
            <span>{{ quantity }}</span>
            <button @click="quantity += 1">+</button>
          </div>
        </div>
        <div class="detail-actions">
          <button class="btn btn-ghost" @click="addToCart(false)">加入购物车</button>
          <button class="btn btn-primary" @click="addToCart(true)">立即购买</button>
        </div>
        <p v-if="error" class="error-text">{{ error }}</p>
      </div>
    </div>
    <section class="panel description">
      <h2>商品详情</h2>
      <div v-html="product.detail"></div>
    </section>

    <section class="panel reviews">
      <h2>商品评价（{{ reviewTotal }}）</h2>
      <div v-if="auth.isLogin" class="review-form">
        <div class="stars">
          <span>评分</span>
          <button
            v-for="n in 5"
            :key="n"
            :class="{ active: n <= formRating }"
            @click="formRating = n"
          >★</button>
        </div>
        <textarea v-model="reviewContent" class="textarea" rows="3" placeholder="分享你的使用体验（500字以内）"></textarea>
        <div class="review-actions">
          <button class="btn btn-primary" :disabled="submittingReview" @click="submitReview">
            {{ submittingReview ? '提交中...' : '发表评价' }}
          </button>
        </div>
        <p class="error-text">{{ reviewError }}</p>
      </div>
      <p v-else class="muted">登录后可以发表评价</p>
      <div v-if="reviews.length" class="review-list">
        <div v-for="r in reviews" :key="r.id" class="review-item">
          <div class="review-head">
            <strong>{{ r.nickname }}</strong>
            <span class="stars-inline">{{ '★'.repeat(r.rating) }}</span>
            <span class="muted">{{ formatTime(r.createTime) }}</span>
          </div>
          <p>{{ r.content }}</p>
        </div>
      </div>
      <p v-else class="muted">还没有评价，来抢沙发</p>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { orderApi, productApi } from '../api'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const product = ref(null)
const sku = ref(null)
const quantity = ref(1)
const error = ref('')
const reviews = ref([])
const reviewTotal = ref(0)
const formRating = ref(5)
const reviewContent = ref('')
const reviewError = ref('')
const submittingReview = ref(false)

const selectedImage = computed(() => sku.value?.image || product.value?.mainImage)
const currentPrice = computed(() => sku.value?.price ?? product.value?.minPrice ?? '')

const selectSku = s => { sku.value = s }

const formatTime = t => (t || '').replace('T', ' ').slice(0, 16)

const loadReviews = async () => {
  try {
    const data = await productApi.reviews(route.params.id, { current: 1, size: 20 })
    reviews.value = data.records
    reviewTotal.value = data.total
  } catch (e) {
    reviewError.value = e.message
  }
}

const submitReview = async () => {
  reviewError.value = ''
  if (!auth.isLogin) {
    router.push('/login')
    return
  }
  if (!reviewContent.value.trim()) {
    reviewError.value = '请填写评价内容'
    return
  }
  submittingReview.value = true
  try {
    await productApi.addReview(route.params.id, {
      rating: formRating.value,
      content: reviewContent.value
    })
    reviewContent.value = ''
    formRating.value = 5
    product.value = await productApi.detail(route.params.id)
    await loadReviews()
  } catch (e) {
    reviewError.value = e.message
  } finally {
    submittingReview.value = false
  }
}

const addToCart = async (buyNow) => {
  if (!sku.value) {
    error.value = '请先选择商品规格'
    return
  }
  error.value = ''
  if (!auth.isLogin) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  try {
    await orderApi.addCart({ skuId: sku.value.id, quantity: quantity.value })
    if (buyNow) {
      router.push({ path: '/checkout', query: { skuId: sku.value.id, quantity: quantity.value, productId: product.value.id } })
    } else {
      router.push('/cart')
    }
  } catch (e) {
    error.value = e.message
  }
}

onMounted(async () => {
  try {
    product.value = await productApi.detail(route.params.id)
    sku.value = product.value.skus?.[0] || null
    await loadReviews()
  } catch (e) {
    error.value = e.message
  }
})
</script>

<style scoped>
.crumbs {
  display: flex;
  gap: 8px;
  color: var(--muted);
  font-size: 13px;
  margin-bottom: 18px;
}

.detail-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.gallery {
  background: #fff;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  overflow: hidden;
  aspect-ratio: 1 / 1;
}

.gallery img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.detail-info {
  padding: 24px;
}

.detail-info h1 {
  margin: 0;
  font-size: 24px;
  letter-spacing: 0;
  line-height: 1.35;
}

.subtitle {
  color: var(--muted);
  margin: 8px 0 16px;
}

.price-box {
  background: #fff7f2;
  border-radius: var(--radius);
  padding: 14px 16px;
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.price.big {
  font-size: 28px;
}

.facts {
  margin: 18px 0;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.facts div {
  background: var(--bg);
  border-radius: 6px;
  padding: 8px 10px;
}

.facts dt {
  font-size: 12px;
  color: var(--muted);
}

.facts dd {
  margin: 4px 0 0;
  font-size: 13px;
  font-weight: 600;
}

.sku-group {
  margin-bottom: 16px;
}

.sku-label {
  font-size: 13px;
  color: var(--muted);
}

.sku-options {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 8px;
}

.sku-option {
  border: 1px solid var(--line);
  background: #fff;
  border-radius: 6px;
  padding: 8px 14px;
  cursor: pointer;
  font-size: 13px;
}

.sku-option.active {
  border-color: var(--primary);
  color: var(--primary);
  background: #fff7f2;
}

.qty-row {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 20px;
  font-size: 13px;
  color: var(--muted);
}

.stepper {
  display: flex;
  align-items: center;
  border: 1px solid var(--line);
  border-radius: 6px;
  overflow: hidden;
}

.stepper button {
  width: 34px;
  height: 34px;
  border: 0;
  background: #fff;
  cursor: pointer;
  font-size: 16px;
}

.stepper span {
  width: 44px;
  text-align: center;
  font-size: 14px;
}

.detail-actions {
  display: flex;
  gap: 12px;
}

.description {
  margin-top: 24px;
  padding: 24px;
}

.description h2 {
  margin: 0 0 14px;
  font-size: 18px;
}

.description :deep(p) {
  color: var(--muted);
  line-height: 1.8;
}

.reviews {
  margin-top: 24px;
  padding: 24px;
}

.reviews h2 {
  margin: 0 0 16px;
  font-size: 18px;
}

.review-form {
  background: var(--bg);
  border-radius: var(--radius);
  padding: 16px;
  margin-bottom: 18px;
}

.stars {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  color: var(--muted);
  font-size: 13px;
}

.stars button {
  border: 0;
  background: transparent;
  color: #d1d5db;
  font-size: 22px;
  cursor: pointer;
  padding: 0;
}

.stars button.active {
  color: #f59e0b;
}

.review-actions {
  margin-top: 10px;
  text-align: right;
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.review-item {
  border-top: 1px solid var(--line);
  padding-top: 14px;
}

.review-head {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
}

.stars-inline {
  color: #f59e0b;
}

.review-item p {
  margin: 8px 0 0;
  color: #374151;
  line-height: 1.7;
  font-size: 14px;
}

@media (max-width: 820px) {
  .detail-layout {
    grid-template-columns: 1fr;
  }
}
</style>
