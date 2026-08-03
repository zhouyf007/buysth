<template>
  <div class="page">
    <section class="hero">
      <div class="container hero-inner">
        <div class="hero-copy">
          <p class="eyebrow">DIGITAL MALL · 2026 新品季</p>
          <h1>数码新品，快人一步</h1>
          <p>手机、笔记本、耳机、智能穿戴一站购齐，支持秒杀与优惠活动。</p>
          <div class="hero-actions">
            <RouterLink to="/seckill" class="btn btn-primary">立即秒杀</RouterLink>
            <RouterLink to="/products" class="btn btn-ghost">浏览商品</RouterLink>
          </div>
        </div>
        <div class="hero-visual">
          <img src="/images/hero-devices.svg" alt="数码设备" />
        </div>
      </div>
    </section>

    <section class="container split">
      <div class="panel announcements">
        <div class="block-head">
          <h2>平台公告</h2>
        </div>
        <div v-for="a in announcements" :key="a.id" class="announcement">
          <span class="tag">{{ a.type === 'ACTIVITY' ? '活动' : '公告' }}</span>
          <div>
            <strong>{{ a.title }}</strong>
            <p>{{ a.content }}</p>
          </div>
        </div>
      </div>
      <div class="panel seckill-teaser">
        <div class="block-head">
          <h2>限时秒杀</h2>
          <RouterLink to="/seckill">进入秒杀</RouterLink>
        </div>
        <div v-if="seckillProducts.length" class="teaser-grid">
          <div v-for="sp in seckillProducts" :key="sp.id" class="teaser-item">
            <img :src="sp.image" :alt="sp.productName" />
            <strong>{{ sp.productName }}</strong>
            <span class="price">¥{{ sp.seckillPrice }}</span>
          </div>
        </div>
        <p v-else class="muted">暂无进行中的秒杀活动</p>
      </div>
    </section>

    <section class="container categories">
      <RouterLink v-for="cat in categories" :key="cat.id" :to="{ path: '/products', query: { categoryId: cat.id } }" class="category">
        <img :src="cat.icon" :alt="cat.name" />
        <span>{{ cat.name }}</span>
      </RouterLink>
    </section>

    <section class="container block">
      <div class="block-head">
        <h2>热销好物</h2>
        <RouterLink to="/products">查看更多</RouterLink>
      </div>
      <div class="grid">
        <ProductCard v-for="p in hot" :key="p.id" :product="p" />
      </div>
    </section>

  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { notifyApi, productApi, seckillApi } from '../api'
import ProductCard from '../components/ProductCard.vue'

const categories = ref([])
const hot = ref([])
const announcements = ref([])
const seckillProducts = ref([])

onMounted(async () => {
  try {
    categories.value = await productApi.categories()
  } catch (e) { /* ignore */ }
  try {
    hot.value = await productApi.hot()
  } catch (e) { /* ignore */ }
  try {
    announcements.value = await notifyApi.announcements()
  } catch (e) { /* ignore */ }
  try {
    const page = await seckillApi.activities({ current: 1, size: 3, type: 'SECKILL' })
    const activity = page.records?.[0]
    seckillProducts.value = activity?.products?.slice(0, 3) || []
  } catch (e) { /* ignore */ }
})
</script>

<style scoped>
.hero {
  background: #fff;
  border-bottom: 1px solid var(--line);
}

.hero-inner {
  display: grid;
  grid-template-columns: 1.1fr .9fr;
  gap: 32px;
  align-items: center;
  padding-top: 48px;
  padding-bottom: 48px;
}

.eyebrow {
  color: var(--primary);
  font-weight: 700;
  letter-spacing: 1px;
  font-size: 13px;
}

.hero-copy h1 {
  font-size: 40px;
  margin: 12px 0;
  letter-spacing: 0;
}

.hero-copy > p:not(.eyebrow) {
  color: var(--muted);
  font-size: 15px;
  max-width: 460px;
  line-height: 1.7;
}

.hero-actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}

.hero-visual img {
  width: 100%;
  display: block;
}

.categories {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
  margin-top: 24px;
}

.category {
  background: #fff;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  padding: 16px 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  transition: border-color .15s ease;
}

.category:hover {
  border-color: var(--primary);
  color: var(--primary);
}

.category img {
  width: 40px;
  height: 40px;
}

.block {
  margin-top: 36px;
}

.block-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 16px;
}

.block-head h2 {
  font-size: 20px;
  margin: 0;
  letter-spacing: 0;
}

.block-head a {
  color: var(--primary);
  font-size: 13px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.split {
  display: grid;
  grid-template-columns: 1.2fr .8fr;
  gap: 20px;
  margin: 36px auto 0;
}

.panel {
  padding: 20px;
}

.announcement {
  display: flex;
  gap: 12px;
  padding: 12px 0;
  border-top: 1px solid var(--line);
}

.announcement:first-of-type {
  border-top: 0;
}

.tag {
  flex-shrink: 0;
  background: #fff2ec;
  color: var(--primary);
  font-size: 12px;
  border-radius: 4px;
  padding: 3px 8px;
  height: fit-content;
}

.announcement strong {
  font-size: 14px;
}

.announcement p {
  margin: 6px 0 0;
  font-size: 13px;
  color: var(--muted);
  line-height: 1.6;
}

.teaser-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.teaser-item {
  text-align: center;
}

.teaser-item img {
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
  border-radius: 6px;
  background: #fafafa;
}

.teaser-item strong {
  display: block;
  font-size: 12px;
  margin-top: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

@media (max-width: 900px) {
  .hero-inner, .split {
    grid-template-columns: 1fr;
  }
  .categories {
    grid-template-columns: repeat(3, 1fr);
  }
  .grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
