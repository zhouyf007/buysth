<template>
  <div>
    <h2 class="page-title">订单管理</h2>
    <div class="table-toolbar">
      <el-input v-model="query.keyword" placeholder="订单号/收货人/电话" clearable style="width: 220px" @keyup.enter="load(1)" />
      <el-input v-model="query.userId" placeholder="用户ID" clearable style="width: 130px" @keyup.enter="load(1)" />
      <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 140px">
        <el-option label="待支付" value="PENDING_PAY" />
        <el-option label="已支付" value="PAID" />
        <el-option label="已发货" value="SHIPPED" />
        <el-option label="已完成" value="COMPLETED" />
        <el-option label="已取消" value="CANCELLED" />
      </el-select>
      <el-button type="primary" @click="load(1)">查询</el-button>
      <el-button type="danger" :disabled="!selected.length" @click="batchDelete">批量删除</el-button>
    </div>
    <el-card>
      <el-table :data="records" v-loading="loading" @selection-change="onSelection">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="orderNo" label="订单号" min-width="190" />
        <el-table-column prop="userId" label="用户ID" width="90" />
        <el-table-column label="类型" width="90">
          <template #default="{ row }">
            <el-tag :type="row.orderType === 'SECKILL' ? 'warning' : ''">{{ row.orderType === 'SECKILL' ? '秒杀' : '普通' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="110">
          <template #default="{ row }">¥{{ row.payAmount }}</template>
        </el-table-column>
        <el-table-column prop="statusText" label="状态" width="100" />
        <el-table-column prop="receiverName" label="收货人" width="110" />
        <el-table-column label="下单时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="view(row)">详情</el-button>
            <el-popconfirm v-if="row.status === 'PENDING_PAY'" title="确认取消该订单？" @confirm="cancel(row)">
              <template #reference>
                <el-button link type="danger">取消</el-button>
              </template>
            </el-popconfirm>
            <el-popconfirm title="确认删除该订单？" @confirm="remove(row)">
              <template #reference>
                <el-button link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination layout="total, prev, pager, next" :total="total" :page-size="query.size" :current-page="query.current" @current-change="load" />
      </div>
    </el-card>

    <el-drawer v-model="drawerVisible" title="订单详情" size="480px">
      <template v-if="current">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="订单号">{{ current.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ current.statusText }}</el-descriptions-item>
          <el-descriptions-item label="应付金额">¥{{ current.payAmount }}</el-descriptions-item>
          <el-descriptions-item label="收货人">{{ current.receiverName }} {{ current.receiverPhone }}</el-descriptions-item>
          <el-descriptions-item label="地址">{{ current.receiverAddress }}</el-descriptions-item>
          <el-descriptions-item label="备注">{{ current.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
        <el-divider>商品明细</el-divider>
        <div v-for="item in current.items" :key="item.id" class="drawer-line">
          <img :src="item.image" class="thumb" />
          <div class="line-info">
            <div>{{ item.productName }}</div>
            <div class="sub">{{ item.skuSpec }} × {{ item.quantity }}</div>
          </div>
          <span>¥{{ item.subtotal }}</span>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { orderApi } from '../api'

const records = ref([])
const total = ref(0)
const loading = ref(false)
const drawerVisible = ref(false)
const current = ref(null)
const query = reactive({ current: 1, size: 10, keyword: '', status: '', userId: '' })
const selected = ref([])

const load = async page => {
  query.current = page || 1
  loading.value = true
  try {
    const data = await orderApi.page({
      current: query.current, size: query.size,
      keyword: query.keyword || undefined,
      status: query.status || undefined,
      userId: query.userId || undefined
    })
    records.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const view = async row => {
  current.value = await orderApi.detail(row.orderNo)
  drawerVisible.value = true
}

const cancel = async row => {
  await orderApi.cancel(row.orderNo)
  ElMessage.success('订单已取消')
  load(query.current)
}

const onSelection = rows => {
  selected.value = rows
}

const remove = async row => {
  await orderApi.deleteOrder(row.orderNo)
  ElMessage.success('订单已删除')
  load(query.current)
}

const batchDelete = async () => {
  const orderNos = selected.value.map(r => r.orderNo)
  await orderApi.batchDelete(orderNos)
  ElMessage.success(`已删除 ${orderNos.length} 个订单`)
  load(query.current)
}

const formatTime = t => (t || '').replace('T', ' ').slice(0, 16)

onMounted(() => load(1))
</script>

<style scoped>
.drawer-line {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 0;
  border-bottom: 1px solid #f3f4f6;
  font-size: 13px;
}

.line-info {
  flex: 1;
}

.sub {
  color: #9ca3af;
  font-size: 12px;
}
</style>
