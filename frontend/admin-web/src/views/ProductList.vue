<template>
  <div>
    <h2 class="page-title">商品管理</h2>
    <el-alert v-if="loadError" :title="loadError" type="error" show-icon closable class="error-alert" @close="loadError = ''" />
    <div class="table-toolbar">
      <el-input v-model="query.keyword" placeholder="商品名称/品牌" clearable style="width: 220px" @keyup.enter="load(1)" />
      <el-select v-model="query.categoryId" placeholder="全部分类" clearable style="width: 160px">
        <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
      <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 140px">
        <el-option label="上架" :value="1" />
        <el-option label="下架" :value="0" />
        <el-option label="草稿" :value="2" />
      </el-select>
      <el-button type="primary" @click="load(1)">查询</el-button>
      <el-button type="success" @click="$router.push('/admin/products/edit')">新增商品</el-button>
    </div>
    <el-card>
      <el-table :data="records" v-loading="loading">
        <el-table-column label="商品" min-width="260">
          <template #default="{ row }">
            <div class="product-cell">
              <img :src="row.mainImage" class="thumb" />
              <div>
                <div>{{ row.name }}</div>
                <div class="sub">{{ row.subtitle }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类" width="110" />
        <el-table-column prop="region" label="地域" width="90" />
        <el-table-column label="价格" width="110">
          <template #default="{ row }">¥{{ row.minPrice ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="totalStock" label="库存" width="90" />
        <el-table-column prop="sales" label="销量" width="90" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : row.status === 0 ? 'info' : 'warning'">
              {{ row.status === 1 ? '上架' : row.status === 0 ? '下架' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="$router.push(`/admin/products/edit/${row.id}`)">编辑</el-button>
            <el-button link :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-popconfirm title="确认删除该商品？" @confirm="remove(row)">
              <template #reference>
                <el-button link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination
          layout="total, prev, pager, next"
          :total="total"
          :page-size="query.size"
          :current-page="query.current"
          @current-change="load"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { categoryApi, productApi } from '../api'

const categories = ref([])
const records = ref([])
const total = ref(0)
const loading = ref(false)
const loadError = ref('')
const query = reactive({ current: 1, size: 10, keyword: '', categoryId: null, status: null })

const load = async page => {
  query.current = page || 1
  loading.value = true
  loadError.value = ''
  try {
    const data = await productApi.page({
      current: query.current, size: query.size,
      keyword: query.keyword || undefined,
      categoryId: query.categoryId || undefined,
      status: query.status === null ? undefined : query.status
    })
    records.value = data.records
    total.value = data.total
  } catch (e) {
    loadError.value = e.message || '商品列表加载失败'
  } finally {
    loading.value = false
  }
}

const toggleStatus = async row => {
  await productApi.status(row.id, row.status === 1 ? 0 : 1)
  ElMessage.success('状态已更新')
  load(query.current)
}

const remove = async row => {
  await productApi.remove(row.id)
  ElMessage.success('删除成功')
  load(query.current)
}

onMounted(async () => {
  try {
    categories.value = await categoryApi.list()
  } catch (e) { /* ignore */ }
  load(1)
})
</script>

<style scoped>
.product-cell {
  display: flex;
  gap: 10px;
  align-items: center;
}

.sub {
  color: #9ca3af;
  font-size: 12px;
  max-width: 240px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.error-alert {
  margin-bottom: 14px;
}
</style>
