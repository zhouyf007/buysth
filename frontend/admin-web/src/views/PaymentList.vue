<template>
  <div>
    <h2 class="page-title">支付管理</h2>
    <div class="table-toolbar">
      <el-input v-model="query.keyword" placeholder="支付单号/订单号" clearable style="width: 220px" @keyup.enter="load(1)" />
      <el-input v-model="query.userId" placeholder="用户ID" clearable style="width: 130px" @keyup.enter="load(1)" />
      <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 140px">
        <el-option label="待支付" value="PENDING" />
        <el-option label="已支付" value="SUCCESS" />
        <el-option label="已关闭" value="CLOSED" />
      </el-select>
      <el-button type="primary" @click="load(1)">查询</el-button>
      <el-button type="danger" :disabled="!selected.length" @click="batchDelete">批量删除</el-button>
    </div>
    <el-card>
      <el-table :data="records" v-loading="loading" @selection-change="onSelection">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="payNo" label="支付单号" min-width="190" />
        <el-table-column prop="orderNo" label="订单号" min-width="190" />
        <el-table-column prop="userId" label="用户ID" width="110" />
        <el-table-column label="金额" width="110">
          <template #default="{ row }">¥{{ row.amount }}</template>
        </el-table-column>
        <el-table-column prop="channel" label="渠道" width="120" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'SUCCESS' ? 'success' : row.status === 'PENDING' ? 'warning' : 'info'">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="支付时间" width="170">
          <template #default="{ row }">{{ formatTime(row.paidTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-popconfirm title="确认删除该支付单？" @confirm="remove(row)">
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
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { paymentApi } from '../api'

const records = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ current: 1, size: 10, keyword: '', status: '', userId: '' })
const selected = ref([])

const load = async page => {
  query.current = page || 1
  loading.value = true
  try {
    const data = await paymentApi.page({
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

const formatTime = t => (t || '').replace('T', ' ').slice(0, 16)

const onSelection = rows => {
  selected.value = rows
}

const remove = async row => {
  await paymentApi.deletePayment(row.id)
  ElMessage.success('支付单已删除')
  load(query.current)
}

const batchDelete = async () => {
  const ids = selected.value.map(r => r.id)
  await paymentApi.batchDelete(ids)
  ElMessage.success(`已删除 ${ids.length} 笔支付`)
  load(query.current)
}

onMounted(() => load(1))
</script>
