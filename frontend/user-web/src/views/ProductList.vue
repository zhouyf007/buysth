<template>
  <div class="container page">
    <div class="list-layout">
      <aside class="panel filter-panel">
        <h3>筛选</h3>
        <label>关键词</label>
        <input v-model="filters.name" class="input" placeholder="商品名称" @keyup.enter="applyFilters" />
        <label>分类</label>
        <select v-model="filters.categoryId" class="select" @change="applyFilters">
          <option :value="null">全部分类</option>
          <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option>
        </select>
        <label>地域</label>
        <select v-model="filters.region" class="select" @change="applyFilters">
          <option value="">全部地域</option>
          <option v-for="r in regions" :key="r" :value="r">{{ r }}</option>
        </select>
        <label>价格区间</label>
        <div class="price-row">
          <input v-model.number="filters.minPrice" class="input" placeholder="最低" />
          <span>-</span>
          <input v-model.number="filters.maxPrice" class="input" placeholder="最高" />
        </div>
        <label>品牌</label>
        <input v-model="filters.brand" class="input" placeholder="品牌" @keyup.enter="applyFilters" />
        <label>上新日期</label>
        <input v-model="filters.startDate" class="input" type="datetime-local" />
        <button class="btn btn-primary search-btn" @click="applyFilters">搜索</button>
        <button class="btn btn-ghost search-btn" @click="resetFilters">重置</button>
      </aside>

      <section class="list-main">
        <div class="list-toolbar">
          <span class="muted">共 {{ total }} 件商品</span>
          <select v-model="filters.sort" class="select sort-select" @change="load(1)">
            <option value="newest">按上新时间</option>
            <option value="sales">按销量</option>
            <option value="rating">按评分</option>
            <option value="priceAsc">价格从低到高</option>
            <option value="priceDesc">价格从高到低</option>
          </select>
        </div>
        <div v-if="products.length" class="grid">
          <ProductCard v-for="p in products" :key="p.id" :product="p" />
        </div>
        <div v-else class="empty">没有找到符合条件的商品</div>
        <div class="pager">
          <button class="btn btn-ghost" :disabled="current <= 1" @click="load(current - 1)">上一页</button>
          <span class="muted">{{ current }} / {{ pages || 1 }}</span>
          <button class="btn btn-ghost" :disabled="current >= pages" @click="load(current + 1)">下一页</button>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { productApi } from '../api'
import ProductCard from '../components/ProductCard.vue'

const route = useRoute()
const router = useRouter()
const categories = ref([])
const products = ref([])
const total = ref(0)
const pages = ref(0)
const current = ref(1)
const regions = ['深圳', '上海', '北京', '南京', '苏州']

const filters = reactive({
  name: route.query.name || '',
  categoryId: route.query.categoryId ? Number(route.query.categoryId) : null,
  region: route.query.region || '',
  minPrice: route.query.minPrice ? Number(route.query.minPrice) : null,
  maxPrice: route.query.maxPrice ? Number(route.query.maxPrice) : null,
  brand: route.query.brand || '',
  startDate: route.query.startDate || '',
  sort: route.query.sort || 'newest'
})

const load = async (page = 1) => {
  current.value = page
  const params = { current: page, size: 12, sort: filters.sort }
  if (filters.name) params.name = filters.name
  if (filters.categoryId) params.categoryId = filters.categoryId
  if (filters.region) params.region = filters.region
  if (filters.minPrice) params.minPrice = filters.minPrice
  if (filters.maxPrice) params.maxPrice = filters.maxPrice
  if (filters.brand) params.brand = filters.brand
  if (filters.startDate) params.startDate = new Date(filters.startDate).toLocaleString('sv-SE').replace('T', ' ')
  const data = await productApi.list(params)
  products.value = data.records
  total.value = data.total
  pages.value = data.pages
}

const applyFilters = () => {
  const query = {}
  if (filters.name) query.name = filters.name
  if (filters.categoryId) query.categoryId = filters.categoryId
  if (filters.region) query.region = filters.region
  if (filters.minPrice) query.minPrice = filters.minPrice
  if (filters.maxPrice) query.maxPrice = filters.maxPrice
  if (filters.brand) query.brand = filters.brand
  if (filters.startDate) query.startDate = filters.startDate
  if (filters.sort !== 'newest') query.sort = filters.sort
  router.replace({ path: '/products', query })
  load(1)
}

const resetFilters = () => {
  Object.assign(filters, {
    name: '', categoryId: null, region: '', minPrice: null,
    maxPrice: null, brand: '', startDate: '', sort: 'newest'
  })
  router.replace({ path: '/products' })
  load(1)
}

onMounted(async () => {
  try {
    categories.value = await productApi.categories()
  } catch (e) { /* ignore */ }
  load(1)
})
</script>

<style scoped>
.list-layout {
  display: grid;
  grid-template-columns: 240px 1fr;
  gap: 20px;
}

.filter-panel {
  padding: 18px;
  height: fit-content;
  position: sticky;
  top: 84px;
}

.filter-panel h3 {
  margin: 0 0 14px;
  font-size: 16px;
}

.filter-panel label {
  display: block;
  font-size: 12px;
  color: var(--muted);
  margin: 12px 0 6px;
}

.price-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.search-btn {
  width: 100%;
  margin-top: 12px;
}

.list-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.sort-select {
  width: 160px;
}

.pager {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-top: 24px;
}

@media (max-width: 760px) {
  .list-layout {
    grid-template-columns: 1fr;
  }
  .filter-panel {
    position: static;
  }
}
</style>

