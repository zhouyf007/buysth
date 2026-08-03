<template>
  <div>
    <h2 class="page-title">{{ id ? '编辑商品' : '新增商品' }}</h2>
    <el-card>
      <el-form :model="form" label-width="90px">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12">
            <el-form-item label="商品名称" required><el-input v-model="form.name" /></el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="分类" required>
              <el-select v-model="form.categoryId" style="width: 100%">
                <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="副标题"><el-input v-model="form.subtitle" /></el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="品牌" required><el-input v-model="form.brand" /></el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="地域" required><el-input v-model="form.region" placeholder="如 深圳" /></el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="上新日期">
              <el-date-picker v-model="form.publishDate" type="datetime" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio :label="1">上架</el-radio>
                <el-radio :label="0">下架</el-radio>
                <el-radio :label="2">草稿</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="主图" required>
          <div class="upload-row">
            <el-upload :show-file-list="false" :http-request="uploadImage">
              <el-button>上传图片</el-button>
            </el-upload>
            <el-input v-model="form.mainImage" placeholder="图片地址" style="flex: 1" />
            <img v-if="form.mainImage" :src="form.mainImage" class="thumb" />
          </div>
        </el-form-item>
        <el-form-item label="详情">
          <el-input v-model="form.detail" type="textarea" :rows="4" placeholder="支持 HTML" />
        </el-form-item>

        <el-divider>SKU 规格</el-divider>
        <el-table :data="form.skus">
          <el-table-column label="规格名" required>
            <template #default="{ row }"><el-input v-model="row.specName" /></template>
          </el-table-column>
          <el-table-column label="规格值" required>
            <template #default="{ row }"><el-input v-model="row.specValue" /></template>
          </el-table-column>
          <el-table-column label="价格" width="160" required>
            <template #default="{ row }"><el-input-number v-model="row.price" :min="0" :precision="2" /></template>
          </el-table-column>
          <el-table-column label="库存" width="160" required>
            <template #default="{ row }"><el-input-number v-model="row.stock" :min="0" /></template>
          </el-table-column>
          <el-table-column label="操作" width="90">
            <template #default="{ $index }">
              <el-button link type="danger" @click="form.skus.splice($index, 1)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-button class="add-sku" @click="form.skus.push({ specName: '版本', specValue: '', price: 0, stock: 0, status: 1 })">
          添加 SKU
        </el-button>

        <div class="form-actions">
          <el-button @click="$router.back()">返回</el-button>
          <el-button type="primary" :loading="saving" @click="save">保存</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { categoryApi, productApi } from '../api'

const route = useRoute()
const router = useRouter()
const id = route.params.id
const categories = ref([])
const saving = ref(false)
const form = reactive({
  categoryId: null, name: '', subtitle: '', brand: '', region: '',
  mainImage: '', detail: '', publishDate: null, status: 1,
  skus: []
})

const uploadImage = async options => {
  const formData = new FormData()
  formData.append('file', options.file)
  form.mainImage = await productApi.upload(formData)
  ElMessage.success('上传成功')
}

const save = async () => {
  if (!form.name || !form.categoryId) {
    ElMessage.warning('请填写商品名称和分类')
    return
  }
  if (!form.brand || !form.region || !form.mainImage) {
    ElMessage.warning('品牌、地域、主图为必填项')
    return
  }
  const skus = form.skus.filter(s => s.specValue)
  if (!skus.length) {
    ElMessage.warning('请至少填写一个SKU规格')
    return
  }
  for (const s of skus) {
    if (!s.specName || !s.specValue || s.price === null || s.stock === null) {
      ElMessage.warning('SKU的规格名、规格值、价格、库存均为必填')
      return
    }
  }
  saving.value = true
  try {
    const payload = { ...form }
    if (form.publishDate) {
      const d = new Date(form.publishDate)
      const pad = n => String(n).padStart(2, '0')
      payload.publishDate = `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}` +
        `T${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
    } else {
      payload.publishDate = undefined
    }
    payload.skus = skus
    if (id) {
      await productApi.update(id, payload)
    } else {
      await productApi.create(payload)
    }
    ElMessage.success('保存成功')
    router.push('/admin/products')
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  try {
    categories.value = await categoryApi.list()
  } catch (e) { /* ignore */ }
  if (id) {
    const p = await productApi.detail(id)
    Object.assign(form, {
      categoryId: p.categoryId, name: p.name, subtitle: p.subtitle, brand: p.brand,
      region: p.region, mainImage: p.mainImage, detail: p.detail,
      publishDate: p.publishDate, status: p.status,
      skus: (p.skus || []).map(s => ({
        specName: s.specName, specValue: s.specValue, price: Number(s.price),
        stock: s.stock, image: s.image, status: s.status, id: s.id
      }))
    })
  }
})
</script>

<style scoped>
.upload-row {
  display: flex;
  gap: 10px;
  align-items: center;
  width: 100%;
}

.add-sku {
  margin-top: 10px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}
</style>
