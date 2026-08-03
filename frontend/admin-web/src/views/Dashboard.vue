<template>
  <div class="dashboard">
    <h2 class="page-title">数据大屏</h2>
    <el-row :gutter="16">
      <el-col v-for="card in cards" :key="card.label" :xs="12" :sm="6">
        <el-card class="stat-card">
          <div class="stat-label">{{ card.label }}</div>
          <div class="stat-value">{{ card.value }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :xs="24" :md="16">
        <el-card>
          <template #header>月度销售趋势</template>
          <div ref="monthlyRef" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="8">
        <el-card>
          <template #header>订单状态分布</template>
          <div ref="statusRef" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :xs="24" :md="12">
        <el-card>
          <template #header>分类销量分布</template>
          <div ref="categoryRef" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card>
          <template #header>热销商品排行</template>
          <div ref="topRef" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import * as echarts from 'echarts'
import { orderApi, paymentApi, productApi, userApi } from '../api'

const monthlyRef = ref(null)
const statusRef = ref(null)
const categoryRef = ref(null)
const topRef = ref(null)
const charts = []
const cards = reactive([
  { label: '商品总数', value: 0 },
  { label: '订单总数', value: 0 },
  { label: '注册用户', value: 0 },
  { label: '支付单数', value: 0 }
])

const statusText = {
  PENDING_PAY: '待支付',
  PAID: '已支付',
  SHIPPED: '已发货',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
  CLOSED: '已关闭'
}

const initChart = (el, option) => {
  if (!el) return
  const chart = echarts.init(el)
  chart.setOption(option)
  charts.push(chart)
}

const render = (monthly, statusStats, categorySales, hotProducts) => {
  initChart(monthlyRef.value, {
    tooltip: { trigger: 'axis' },
    legend: { data: ['订单数', '销售额'] },
    grid: { left: 40, right: 50, top: 40, bottom: 30 },
    xAxis: { type: 'category', data: monthly.map(m => m.month) },
    yAxis: [
      { type: 'value', name: '订单数' },
      { type: 'value', name: '销售额' }
    ],
    series: [
      { name: '订单数', type: 'bar', data: monthly.map(m => m.orderCount), itemStyle: { color: '#409eff' } },
      {
        name: '销售额',
        type: 'line',
        yAxisIndex: 1,
        smooth: true,
        data: monthly.map(m => Number(m.totalAmount)),
        itemStyle: { color: '#ff5a1f' }
      }
    ]
  })

  initChart(statusRef.value, {
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie',
      radius: ['42%', '68%'],
      center: ['50%', '46%'],
      data: statusStats.map(s => ({
        name: statusText[s.status] || s.status,
        value: Number(s.count)
      }))
    }]
  })

  initChart(categoryRef.value, {
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie',
      radius: '62%',
      data: categorySales.map(c => ({ name: c.categoryName, value: c.sales }))
    }]
  })

  initChart(topRef.value, {
    tooltip: { trigger: 'axis' },
    grid: { left: 90, right: 30, top: 20, bottom: 30 },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: hotProducts.map(p => p.name).reverse() },
    series: [{
      type: 'bar',
      data: hotProducts.map(p => p.sales).reverse(),
      itemStyle: { color: '#67c23a' },
      label: { show: true, position: 'right' }
    }]
  })
}

const handleResize = () => charts.forEach(c => c.resize())

onMounted(async () => {
  const [products, orders, users, payments, monthly, statusStats, categorySales, hotProducts] = await Promise.allSettled([
    productApi.page({ current: 1, size: 1 }),
    orderApi.page({ current: 1, size: 1 }),
    userApi.page({ current: 1, size: 1 }),
    paymentApi.page({ current: 1, size: 1 }),
    orderApi.monthly(),
    orderApi.statusStats(),
    productApi.categorySales(),
    productApi.hot()
  ])
  cards[0].value = products.status === 'fulfilled' ? products.value.total : '-'
  cards[1].value = orders.status === 'fulfilled' ? orders.value.total : '-'
  cards[2].value = users.status === 'fulfilled' ? users.value.total : '-'
  cards[3].value = payments.status === 'fulfilled' ? payments.value.total : '-'
  render(
    monthly.status === 'fulfilled' ? monthly.value : [],
    statusStats.status === 'fulfilled' ? statusStats.value : [],
    categorySales.status === 'fulfilled' ? categorySales.value : [],
    hotProducts.status === 'fulfilled' ? hotProducts.value : []
  )
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  charts.forEach(c => c.dispose())
})
</script>

<style scoped>
.stat-card {
  margin-bottom: 16px;
}

.stat-label {
  color: #6b7280;
  font-size: 13px;
}

.stat-value {
  font-size: 26px;
  font-weight: 700;
  margin-top: 6px;
  color: #ff5a1f;
}

.chart-row {
  margin-bottom: 16px;
}

.chart {
  width: 100%;
  height: 320px;
}
</style>

