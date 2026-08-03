<template>
  <div>
    <h2 class="page-title">公告管理</h2>
    <div class="table-toolbar">
      <el-input v-model="query.keyword" placeholder="标题/内容" clearable style="width: 220px" @keyup.enter="load(1)" />
      <el-button type="primary" @click="load(1)">查询</el-button>
      <el-button type="success" @click="openDialog()">发布公告</el-button>
    </div>
    <el-card>
      <el-table :data="records" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.type === 'ACTIVITY' ? 'success' : ''">{{ row.type === 'ACTIVITY' ? '活动' : '公告' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '发布' : '隐藏' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发布时间" width="170">
          <template #default="{ row }">{{ formatTime(row.publishTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-popconfirm title="确认删除？" @confirm="remove(row)">
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑公告' : '发布公告'" width="560px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="form.type">
            <el-radio label="NOTICE">公告</el-radio>
            <el-radio label="ACTIVITY">活动</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="5" /></el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { announceApi } from '../api'

const records = ref([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const form = reactive({ id: null, title: '', content: '', type: 'NOTICE', status: 1, publishTime: null })
const query = reactive({ current: 1, size: 10, keyword: '' })

const load = async page => {
  query.current = page || 1
  loading.value = true
  try {
    const data = await announceApi.page({
      current: query.current, size: query.size,
      keyword: query.keyword || undefined
    })
    records.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const openDialog = row => {
  Object.assign(form, row ? { ...row } : { id: null, title: '', content: '', type: 'NOTICE', status: 1, publishTime: null })
  dialogVisible.value = true
}

const save = async () => {
  if (form.id) {
    await announceApi.update(form.id, form)
  } else {
    await announceApi.create(form)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load(query.current)
}

const remove = async row => {
  await announceApi.remove(row.id)
  ElMessage.success('删除成功')
  load(query.current)
}

const formatTime = t => (t || '').replace('T', ' ').slice(0, 16)

onMounted(() => load(1))
</script>

