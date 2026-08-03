<template>
  <div>
    <h2 class="page-title">用户管理</h2>
    <div class="table-toolbar">
      <el-input v-model="query.keyword" placeholder="用户名/昵称/手机号" clearable style="width: 220px" @keyup.enter="load(1)" />
      <el-button type="primary" @click="load(1)">查询</el-button>
    </div>
    <el-card>
      <el-table :data="records" v-loading="loading">
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="nickname" label="昵称" width="140" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="email" label="邮箱" min-width="160" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button link type="primary" @click="openRole(row)">分配角色</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination layout="total, prev, pager, next" :total="total" :page-size="query.size" :current-page="query.current" @current-change="load" />
      </div>
    </el-card>

    <el-dialog v-model="roleVisible" title="分配角色" width="420px">
      <el-checkbox-group v-model="roleIds">
        <el-checkbox v-for="r in roles" :key="r.id" :label="r.id">{{ r.name }}</el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="roleVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRole">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { roleApi, userApi } from '../api'

const records = ref([])
const total = ref(0)
const loading = ref(false)
const roles = ref([])
const roleVisible = ref(false)
const currentUser = ref(null)
const roleIds = ref([])
const query = reactive({ current: 1, size: 10, keyword: '' })

const load = async page => {
  query.current = page || 1
  loading.value = true
  try {
    const data = await userApi.page({
      current: query.current, size: query.size,
      keyword: query.keyword || undefined
    })
    records.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const toggleStatus = async row => {
  await userApi.status(row.id, row.status === 1 ? 0 : 1)
  ElMessage.success('状态已更新')
  load(query.current)
}

const openRole = async row => {
  currentUser.value = row
  roleIds.value = []
  roleVisible.value = true
  try {
    roles.value = await roleApi.list()
  } catch (e) { /* ignore */ }
}

const saveRole = async () => {
  await userApi.role(currentUser.value.id, roleIds.value)
  ElMessage.success('角色已更新')
  roleVisible.value = false
}

onMounted(() => load(1))
</script>

