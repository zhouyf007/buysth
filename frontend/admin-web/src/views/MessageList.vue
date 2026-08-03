<template>
  <div>
    <h2 class="page-title">消息记录</h2>
    <div class="table-toolbar">
      <el-input v-model="query.userId" placeholder="用户ID" clearable style="width: 160px" @keyup.enter="load(1)" />
      <el-button type="primary" @click="load(1)">查询</el-button>
      <el-button type="danger" :disabled="!selected.length" @click="batchDelete">批量删除</el-button>
    </div>
    <el-card>
      <el-table :data="records" v-loading="loading" @selection-change="onSelection">
        <el-table-column type="selection" width="50" />
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
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-popconfirm title="确认删除该消息？" @confirm="remove(row)">
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
import { messageApi } from '../api'

const records = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ current: 1, size: 10, userId: '' })
const selected = ref([])

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

const onSelection = rows => {
  selected.value = rows
}

const remove = async row => {
  await messageApi.deleteMessage(row.id)
  ElMessage.success('消息已删除')
  load(query.current)
}

const batchDelete = async () => {
  const ids = selected.value.map(r => r.id)
  await messageApi.batchDelete(ids)
  ElMessage.success(`已删除 ${ids.length} 条消息`)
  load(query.current)
}

onMounted(() => load(1))
</script>
