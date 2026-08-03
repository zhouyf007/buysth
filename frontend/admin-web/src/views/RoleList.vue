<template>
  <div>
    <h2 class="page-title">角色管理</h2>
    <div class="table-toolbar">
      <el-button type="success" @click="openDialog()">新增角色</el-button>
    </div>
    <el-card>
      <el-table :data="roles" v-loading="loading">
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="code" label="角色编码" width="140" />
        <el-table-column prop="name" label="角色名称" width="150" />
        <el-table-column prop="description" label="描述" min-width="180" />
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-popconfirm v-if="row.code !== 'SUPER_ADMIN'" title="确认删除该角色？" @confirm="remove(row)">
              <template #reference>
                <el-button link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑角色' : '新增角色'" width="560px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="编码"><el-input v-model="form.code" :disabled="!!form.id" /></el-form-item>
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" /></el-form-item>
        <el-form-item label="菜单权限">
          <el-tree
            ref="treeRef"
            :data="menuTree"
            node-key="id"
            show-checkbox
            default-expand-all
            :props="{ label: 'name', children: 'children' }"
          />
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
import { nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { roleApi } from '../api'

const roles = ref([])
const menuTree = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const treeRef = ref(null)
const form = reactive({ id: null, code: '', name: '', description: '', menuIds: [] })

const load = async () => {
  loading.value = true
  try {
    roles.value = await roleApi.list()
    menuTree.value = await roleApi.menus()
  } finally {
    loading.value = false
  }
}

const openDialog = async row => {
  Object.assign(form, row
    ? { id: row.id, code: row.code, name: row.name, description: row.description, menuIds: [...(row.menuIds || [])] }
    : { id: null, code: '', name: '', description: '', menuIds: [] })
  dialogVisible.value = true
  await nextTick()
  treeRef.value?.setCheckedKeys(form.menuIds)
}

const save = async () => {
  form.menuIds = treeRef.value?.getCheckedKeys() || []
  if (form.id) {
    await roleApi.update(form.id, form)
  } else {
    await roleApi.create(form)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  load()
}

const remove = async row => {
  await roleApi.remove(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)
</script>

