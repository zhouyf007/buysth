<template>
  <RouterLink :to="`/products/${product.id}`" class="product-card">
    <div class="image-wrap">
      <img :src="product.mainImage" :alt="product.name" />
      <span v-if="product.minPrice" class="price-flag">¥{{ product.minPrice }}</span>
    </div>
    <div class="info">
      <h3>{{ product.name }}</h3>
      <p>{{ product.subtitle }}</p>
      <div class="meta">
        <span>{{ product.region }}</span>
        <span>已售 {{ product.sales }}</span>
        <span>上新 {{ formatTime(product.publishDate) }}</span>
      </div>
    </div>
  </RouterLink>
</template>

<script setup>
defineProps({ product: { type: Object, required: true } })

const formatTime = t => (t || '').replace('T', ' ').slice(0, 10)
</script>

<style scoped>
.product-card {
  display: block;
  background: var(--card);
  border: 1px solid var(--line);
  border-radius: var(--radius);
  overflow: hidden;
  transition: transform .15s ease, box-shadow .15s ease;
}

.product-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(31, 35, 40, .08);
}

.image-wrap {
  position: relative;
  height: 150px;
  background: #fafafa;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-wrap img {
  width: 128px;
  height: 128px;
  object-fit: cover;
  display: block;
  border-radius: 6px;
}

.price-flag {
  position: absolute;
  left: 10px;
  bottom: 10px;
  background: var(--primary);
  color: #fff;
  font-weight: 700;
  border-radius: 4px;
  padding: 3px 8px;
  font-size: 14px;
}

.info {
  padding: 12px;
}

.info h3 {
  margin: 0;
  font-size: 14px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.info p {
  margin: 6px 0 0;
  font-size: 12px;
  color: var(--muted);
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.meta {
  margin-top: 10px;
  display: flex;
  justify-content: space-between;
  color: var(--muted);
  font-size: 12px;
}
</style>
