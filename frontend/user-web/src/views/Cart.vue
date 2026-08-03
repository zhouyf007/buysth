<template>
  <div class="container page">
    <h1 class="page-title">购物车</h1>
    <p v-if="error" class="error-text">{{ error }}</p>
    <div v-if="items.length" class="cart-layout">
      <div class="cart-list">
        <div class="cart-head">
          <label class="check">
            <input type="checkbox" :checked="allChecked" @change="toggleAll" />
            <span>全选</span>
          </label>
          <span>商品</span>
          <span>单价</span>
          <span>数量</span>
          <span>小计</span>
          <span>操作</span>
        </div>
        <div v-for="item in items" :key="item.id" class="cart-row">
          <label class="check">
            <input type="checkbox" :checked="!!item.checked" @change="toggle(item)" />
          </label>
          <div class="cart-product" @click="$router.push(`/products/${item.productId}`)">
            <img :src="item.image" :alt="item.productName" />
            <div>
              <strong>{{ item.productName }}</strong>
              <p>{{ item.skuSpec }}</p>
            </div>
          </div>
          <span class="price">¥{{ item.price }}</span>
          <div class="stepper">
            <button :disabled="item.quantity <= 1" title="至少保留1件" @click="changeQty(item, -1)">-</button>
            <span>{{ item.quantity }}</span>
            <button @click="changeQty(item, 1)">+</button>
          </div>
          <span class="price">¥{{ item.subtotal }}</span>
          <button class="remove" @click="remove(item)">删除</button>
        </div>
      </div>
      <aside class="panel cart-summary">
        <h3>结算</h3>
        <div class="summary-line"><span>已选商品</span><span>{{ checkedCount }} 件</span></div>
        <div class="summary-line total"><span>合计</span><span class="price">¥{{ checkedTotal }}</span></div>
        <button class="btn btn-primary checkout-btn" :disabled="!checkedCount" @click="$router.push('/checkout')">
          去结算
        </button>
      </aside>
    </div>
    <div v-else class="panel empty">
      购物车还是空的
      <div style="margin-top: 16px">
        <RouterLink to="/products" class="btn btn-primary">去逛逛</RouterLink>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { orderApi } from '../api'

const items = ref([])
const error = ref('')

const checkedItems = computed(() => items.value.filter(i => i.checked))
const checkedCount = computed(() => checkedItems.value.reduce((n, i) => n + i.quantity, 0))
const checkedTotal = computed(() => checkedItems.value.reduce((s, i) => s + Number(i.subtotal), 0).toFixed(2))
const allChecked = computed(() => items.value.length > 0 && checkedItems.value.length === items.value.length)

const load = async () => {
  error.value = ''
  items.value = await orderApi.cart()
}

const toggle = async item => {
  const next = item.checked ? 0 : 1
  item.checked = next
  try {
    await orderApi.updateCart(item.id, { checked: next })
  } catch (e) {
    item.checked = next ? 0 : 1
    error.value = e.message
  }
}

const toggleAll = async () => {
  const target = allChecked.value ? 0 : 1
  items.value.forEach(i => { i.checked = target })
  try {
    await Promise.all(items.value.map(i => orderApi.updateCart(i.id, { checked: target })))
  } catch (e) {
    error.value = e.message
    load()
  }
}

const changeQty = async (item, delta) => {
  const quantity = item.quantity + delta
  if (quantity <= 0) return
  item.quantity = quantity
  item.subtotal = (Number(item.price) * quantity).toFixed(2)
  try {
    await orderApi.updateCart(item.id, { quantity })
  } catch (e) {
    error.value = e.message
    load()
  }
}

const remove = async item => {
  try {
    await orderApi.deleteCart(item.id)
    items.value = items.value.filter(i => i.id !== item.id)
  } catch (e) {
    error.value = e.message
  }
}

onMounted(load)
</script>

<style scoped>
.page-title {
  font-size: 22px;
  margin: 0 0 18px;
  letter-spacing: 0;
}

.cart-layout {
  display: grid;
  grid-template-columns: 1fr 260px;
  gap: 20px;
  align-items: start;
}

.cart-list {
  background: #fff;
  border: 1px solid var(--line);
  border-radius: var(--radius);
}

.cart-head, .cart-row {
  display: grid;
  grid-template-columns: 60px 1fr 90px 130px 110px 60px;
  align-items: center;
  gap: 10px;
  padding: 14px 18px;
  font-size: 13px;
}

.cart-head {
  color: var(--muted);
  border-bottom: 1px solid var(--line);
}

.cart-row {
  border-bottom: 1px solid var(--line);
}

.cart-row:last-child {
  border-bottom: 0;
}

.check {
  display: flex;
  align-items: center;
  gap: 8px;
}

.cart-product {
  display: flex;
  gap: 12px;
  cursor: pointer;
}

.cart-product img {
  width: 72px;
  height: 72px;
  object-fit: cover;
  border-radius: 6px;
  background: #fafafa;
}

.cart-product strong {
  font-size: 14px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.cart-product p {
  color: var(--muted);
  font-size: 12px;
  margin: 6px 0 0;
}

.stepper {
  display: flex;
  border: 1px solid var(--line);
  border-radius: 6px;
  width: fit-content;
  overflow: hidden;
}

.stepper button {
  width: 30px;
  height: 30px;
  border: 0;
  background: #fff;
  cursor: pointer;
}

.stepper span {
  width: 42px;
  text-align: center;
  line-height: 30px;
  font-size: 13px;
}

.remove {
  border: 0;
  background: transparent;
  color: var(--muted);
  cursor: pointer;
}

.remove:hover {
  color: #d33;
}

.cart-summary {
  padding: 20px;
  position: sticky;
  top: 84px;
}

.cart-summary h3 {
  margin: 0 0 16px;
}

.summary-line {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  padding: 8px 0;
}

.summary-line.total {
  border-top: 1px solid var(--line);
  margin-top: 8px;
  padding-top: 14px;
  font-weight: 700;
}

.checkout-btn {
  width: 100%;
  margin-top: 16px;
}

@media (max-width: 900px) {
  .cart-layout { grid-template-columns: 1fr; }
  .cart-head { display: none; }
  .cart-row { grid-template-columns: 40px 1fr 90px; }
}
</style>
