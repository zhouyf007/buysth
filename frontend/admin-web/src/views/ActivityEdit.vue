<template>
  <div>
    <h2 class="page-title">{{ id ? '编辑活动' : '新增活动' }}</h2>
    <el-card>
      <el-form :model="form" label-width="100px">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12">
            <el-form-item label="活动名称" required><el-input v-model="form.name" /></el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="活动类型">
              <el-radio-group v-model="form.type">
                <el-radio label="SECKILL">秒杀</el-radio>
                <el-radio label="PROMOTION">优惠</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="开始时间" required>
              <el-date-picker v-model="form.startTime" type="datetime" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="结束时间" required>
              <el-date-picker v-model="form.endTime" type="datetime" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="状态">
              <el-select v-model="form.status" style="width: 100%">
                <el-option label="草稿" value="DRAFT" />
                <el-option label="上线" value="ONLINE" />
                <el-option label="结束" value="ENDED" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="活动描述"><el-input v-model="form.description" /></el-form-item>
          </el-col>
          <template v-if="form.type === 'PROMOTION'">
            <el-col :xs="24" :sm="8">
              <el-form-item label="优惠码"><el-input v-model="form.promotionCode" placeholder="back-to-school" /></el-form-item>
            </el-col>
            <el-col :xs="24" :sm="8">
              <el-form-item label="折扣类型">
                <el-select v-model="form.discountType" style="width: 100%">
                  <el-option label="折扣(95折)" value="PERCENT" />
                  <el-option label="立减" value="FIXED" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="8">
              <el-form-item label="折扣值">
                <el-input-number v-model="form.discountValue" :min="0" :precision="2" style="width: 100%" />
              </el-form-item>
            </el-col>
          </template>
        </el-row>

        <template v-if="form.type === 'SECKILL'">
          <el-divider>秒杀商品</el-divider>
          <el-table :data="form.products">
            <el-table-column label="SKU ID">
              <template #default="{ row }"><el-input-number v-model="row.skuId" :min="1" /></template>
            </el-table-column>
            <el-table-column label="秒杀价" width="160">
              <template #default="{ row }"><el-input-number v-model="row.seckillPrice" :min="0" :precision="2" /></template>
            </el-table-column>
            <el-table-column label="秒杀库存" width="160">
              <template #default="{ row }"><el-input-number v-model="row.seckillStock" :min="0" /></template>
            </el-table-column>
            <el-table-column label="限购" width="140">
              <template #default="{ row }"><el-input-number v-model="row.limitPerUser" :min="1" /></template>
            </el-table-column>
            <el-table-column label="操作" width="90">
              <template #default="{ $index }">
                <el-button link type="danger" @click="form.products.splice($index, 1)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-button class="add-sku" @click="form.products.push({ skuId: 201, seckillPrice: 0, seckillStock: 0, limitPerUser: 1, status: 1 })">
            添加秒杀商品
          </el-button>
        </template>

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
import { activityApi } from '../api'

const route = useRoute()
const router = useRouter()
const id = route.params.id
const saving = ref(false)
const form = reactive({
  name: '', type: 'SECKILL', startTime: null, endTime: null, status: 'DRAFT',
  description: '', promotionCode: '', discountType: 'PERCENT', discountValue: 95,
  products: []
})

const save = async () => {
  if (!form.name || !form.startTime || !form.endTime) {
    ElMessage.warning('请填写活动名称和起止时间')
    return
  }
  saving.value = true
  try {
    const payload = {
      ...form,
      startTime: new Date(form.startTime).toLocaleString('sv-SE').replace('T', ' '),
      endTime: new Date(form.endTime).toLocaleString('sv-SE').replace('T', ' '),
      products: form.type === 'SECKILL' ? form.products : []
    }
    if (id) {
      await activityApi.update(id, payload)
    } else {
      await activityApi.create(payload)
    }
    ElMessage.success('保存成功')
    router.push('/admin/activities')
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  if (id) {
    const a = await activityApi.detail(id)
    Object.assign(form, {
      name: a.name, type: a.type, startTime: a.startTime, endTime: a.endTime, status: a.status,
      description: a.description, promotionCode: a.promotionCode || '',
      discountType: a.discountType || 'PERCENT', discountValue: Number(a.discountValue || 95),
      products: (a.products || []).map(p => ({
        skuId: p.skuId, seckillPrice: Number(p.seckillPrice),
        seckillStock: p.seckillStock, limitPerUser: p.limitPerUser, status: 1
      }))
    })
  }
})
</script>

<style scoped>
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

