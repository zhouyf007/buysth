<template>
  <div>
    <h2 class="page-title">分类管理</h2>
    <div class="table-toolbar">
      <el-button type="primary" @click="openDialog()">新增分类</el-button>
    </div>
    <el-card>
      <el-table :data="categories" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="分类名称" />
        <el-table-column label="图标">
          <template #default="{ row }">
            <img v-if="row.icon" :src="row.icon" class="thumb" :alt="row.name" />
            <span v-else class="icon-fallback">{{ row.name.slice(0, 1) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="90" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-popconfirm title="确认删除该分类？" @confirm="remove(row)">
              <template #reference>
                <el-button link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑分类' : '新增分类'" width="480px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="图标">
          <div class="icon-upload">
            <el-upload :show-file-list="false" :http-request="uploadIcon">
              <el-button>上传图标</el-button>
            </el-upload>
            <img v-if="form.icon" :src="form.icon" class="thumb" :alt="form.name" />
            <span v-else class="icon-fallback">{{ (form.name || '类').slice(0, 1) }}</span>
            <el-input v-model="form.icon" placeholder="图标地址" style="flex: 1" />
          </div>
        </el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
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
import { categoryApi, productApi } from '../api'

const categories = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const form = reactive({ id: null, name: '', icon: '', sort: 0, status: 1, parentId: 0 })

const load = async () => {
  loading.value = true
  try {
    categories.value = await categoryApi.list()
  } finally {
    loading.value = false
  }
}

const openDialog = row => {
  Object.assign(form, row ? { ...row } : { id: null, name: '', icon: '', sort: 0, status: 1, parentId: 0 })
  dialogVisible.value = true
}

const uploadIcon = async options => {
  const formData = new FormData()
  formData.append('file', options.file)
  form.icon = await productApi.upload(formData, 'icon')
  ElMessage.success('图标上传成功')
}

const save = async () => {
  if (form.id) {
    await categoryApi.update(form.id, form)
  } else {
    await categoryApi.create(form)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}

const remove = async row => {
  await categoryApi.remove(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)
</script>

<style scoped>
.icon-fallback {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  border-radius: 6px;
  background: #fff2ec;
  color: #ff5a1f;
  font-weight: 700;
}

.icon-upload {
  display: flex;
  gap: 10px;
  align-items: center;
  width: 100%;
}
</style>
