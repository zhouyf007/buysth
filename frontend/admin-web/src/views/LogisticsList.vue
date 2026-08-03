<template>
  <div>
    <h2 class="page-title">物流管理</h2>
    <div class="table-toolbar">
      <el-input v-model="query.keyword" placeholder="订单号/运单号" clearable style="width: 220px" @keyup.enter="load(1)" />
      <el-input v-model="query.userId" placeholder="用户ID" clearable style="width: 130px" @keyup.enter="load(1)" />
      <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 150px">
        <el-option label="已创建" value="CREATED" />
        <el-option label="已揽收" value="PICKED" />
        <el-option label="运输中" value="IN_TRANSIT" />
        <el-option label="已派送" value="DELIVERED" />
        <el-option label="已签收" value="SIGNED" />
      </el-select>
      <el-button type="primary" @click="load(1)">查询</el-button>
      <el-button type="success" @click="createVisible = true">手动创建运单</el-button>
      <el-button type="danger" :disabled="!selected.length" @click="batchDelete">批量删除</el-button>
    </div>
    <el-card>
      <el-table :data="records" v-loading="loading" @selection-change="onSelection">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="shipmentNo" label="运单号" min-width="180" />
        <el-table-column prop="orderNo" label="订单号" min-width="180" />
        <el-table-column prop="userId" label="用户ID" width="110" />
        <el-table-column prop="companyName" label="物流公司" width="120" />
        <el-table-column prop="trackingNo" label="快递单号" min-width="180" />
        <el-table-column prop="status" label="状态" width="110" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openTrack(row)">添加轨迹</el-button>
            <el-select
              v-model="row.status"
              size="small"
              style="width: 110px"
              @change="updateStatus(row)"
            >
              <el-option label="已创建" value="CREATED" />
              <el-option label="已揽收" value="PICKED" />
              <el-option label="运输中" value="IN_TRANSIT" />
              <el-option label="已派送" value="DELIVERED" />
              <el-option label="已签收" value="SIGNED" />
            </el-select>
            <el-popconfirm title="确认删除该运单？" @confirm="remove(row)">
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

    <el-dialog v-model="createVisible" title="创建运单" width="420px">
      <el-input v-model="createOrderNo" placeholder="输入订单号（已支付订单）" />
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" @click="createShipment">创建</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="trackVisible" title="添加物流轨迹" width="420px">
      <el-form label-width="80px">
        <el-form-item label="状态">
          <el-select v-model="trackForm.status" style="width: 100%">
            <el-option label="已揽收" value="PICKED" />
            <el-option label="运输中" value="IN_TRANSIT" />
            <el-option label="已派送" value="DELIVERED" />
            <el-option label="已签收" value="SIGNED" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述"><el-input v-model="trackForm.description" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="trackVisible = false">取消</el-button>
        <el-button type="primary" @click="addTrack">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { logisticsApi } from '../api'

const records = ref([])
const total = ref(0)
const loading = ref(false)
const createVisible = ref(false)
const createOrderNo = ref('')
const trackVisible = ref(false)
const currentRow = ref(null)
const trackForm = reactive({ status: 'IN_TRANSIT', description: '' })
const query = reactive({ current: 1, size: 10, keyword: '', status: '', userId: '' })
const selected = ref([])

const load = async page => {
  query.current = page || 1
  loading.value = true
  try {
    const data = await logisticsApi.page({
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

const createShipment = async () => {
  await logisticsApi.create(createOrderNo.value)
  ElMessage.success('运单创建成功')
  createVisible.value = false
  createOrderNo.value = ''
  load(query.current)
}

const openTrack = row => {
  currentRow.value = row
  trackForm.description = ''
  trackVisible.value = true
}

const addTrack = async () => {
  await logisticsApi.track(currentRow.value.id, trackForm)
  ElMessage.success('轨迹已添加')
  trackVisible.value = false
  load(query.current)
}

const updateStatus = async row => {
  await logisticsApi.status(row.id, row.status)
  ElMessage.success('状态已更新')
}

const onSelection = rows => {
  selected.value = rows
}

const remove = async row => {
  await logisticsApi.deleteShipment(row.id)
  ElMessage.success('运单已删除')
  load(query.current)
}

const batchDelete = async () => {
  const ids = selected.value.map(r => r.id)
  await logisticsApi.batchDelete(ids)
  ElMessage.success(`已删除 ${ids.length} 个运单`)
  load(query.current)
}

onMounted(() => load(1))
</script>
