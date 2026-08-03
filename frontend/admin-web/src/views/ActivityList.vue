<template>
  <div>
    <h2 class="page-title">活动管理</h2>
    <div class="table-toolbar">
      <el-select v-model="query.type" placeholder="全部类型" clearable style="width: 140px">
        <el-option label="秒杀" value="SECKILL" />
        <el-option label="优惠" value="PROMOTION" />
      </el-select>
      <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 140px">
        <el-option label="草稿" value="DRAFT" />
        <el-option label="进行中" value="ONLINE" />
        <el-option label="已结束" value="ENDED" />
      </el-select>
      <el-button type="primary" @click="load(1)">查询</el-button>
      <el-button type="success" @click="$router.push('/admin/activities/edit')">新增活动</el-button>
    </div>
    <el-card>
      <el-table :data="records" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="活动名称" min-width="180" />
        <el-table-column label="类型" width="90">
          <template #default="{ row }">
            <el-tag :type="row.type === 'SECKILL' ? 'danger' : 'success'">{{ row.type === 'SECKILL' ? '秒杀' : '优惠' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="promotionCode" label="优惠码" width="140">
          <template #default="{ row }">{{ row.promotionCode || '-' }}</template>
        </el-table-column>
        <el-table-column label="时间" min-width="220">
          <template #default="{ row }">{{ formatTime(row.startTime) }} ~ {{ formatTime(row.endTime) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ONLINE' ? 'success' : row.status === 'DRAFT' ? 'warning' : 'info'">
              {{ row.status === 'ONLINE' ? '进行中' : row.status === 'DRAFT' ? '草稿' : '已结束' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="$router.push(`/admin/activities/edit/${row.id}`)">编辑</el-button>
            <el-button link :type="row.status === 'ONLINE' ? 'warning' : 'success'" @click="toggleStatus(row)">
              {{ row.status === 'ONLINE' ? '下线' : '上线' }}
            </el-button>
            <el-button v-if="row.type === 'SECKILL'" link @click="preload(row)">预热库存</el-button>
            <el-popconfirm title="确认删除该活动？" @confirm="remove(row)">
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
import { ElMessage } from 'element-plus'
import { activityApi } from '../api'

const records = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ current: 1, size: 10, type: '', status: '' })

const load = async page => {
  query.current = page || 1
  loading.value = true
  try {
    const data = await activityApi.page({
      current: query.current, size: query.size,
      type: query.type || undefined,
      status: query.status || undefined
    })
    records.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const toggleStatus = async row => {
  const next = row.status === 'ONLINE' ? 'ENDED' : 'ONLINE'
  await activityApi.status(row.id, next)
  ElMessage.success(next === 'ONLINE' ? '活动已上线并预热库存' : '活动已下线')
  load(query.current)
}

const preload = async row => {
  await activityApi.preload(row.id)
  ElMessage.success('秒杀库存已预热到 Redis')
}

const remove = async row => {
  await activityApi.remove(row.id)
  ElMessage.success('删除成功')
  load(query.current)
}

const formatTime = t => (t || '').replace('T', ' ').slice(0, 16)

onMounted(() => load(1))
</script>

