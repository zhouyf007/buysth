<template>
  <div>
    <h2 class="page-title">消息记录</h2>
    <div class="table-toolbar">
      <el-input v-model="query.userId" placeholder="用户ID" clearable style="width: 160px" @keyup.enter="load(1)" />
      <el-button type="primary" @click="load(1)">查询</el-button>
    </div>
    <el-card>
      <el-table :data="records" v-loading="loading">
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="orderNo" label="订单号" min-width="180" />
        <el-table-column prop="type" label="类型" width="130" />
        <el-table-column prop="title" label="标题" min-width="160" />
        <el-table-column prop="content" label="内容" min-width="260" show-overflow-tooltip />
        <el-table-column label="已读" width="90">
          <template #default="{ row }">
            <el-tag :type="row.readStatus === 1 ? 'success' : 'info'">{{ row.readStatus === 1 ? '已读' : '未读' }}</el-tag>
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
import { messageApi } from '../api'

const records = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ current: 1, size: 10, userId: '' })

const load = async page => {
  query.current = page || 1
  loading.value = true
  try {
    const data = await messageApi.page({
      current: query.current, size: query.size,
      userId: query.userId || undefined
    })
    records.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

onMounted(() => load(1))
</script>

