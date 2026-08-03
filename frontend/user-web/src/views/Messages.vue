<template>
  <div class="container page">
    <h1 class="page-title">消息通知</h1>
    <div class="message-toolbar">
      <label class="check">
        <input type="checkbox" :checked="allSelected" @change="toggleAll" />
        <span>全选</span>
      </label>
      <button class="btn btn-danger" :disabled="!selectedIds.length" @click="batchRemove">
        批量删除（{{ selectedIds.length }}）
      </button>
    </div>
    <div v-if="messages.length" class="message-list">
      <div v-for="m in messages" :key="m.id" :class="['panel', 'message', { unread: !m.readStatus }]">
        <label class="check" @click.stop>
          <input type="checkbox" :checked="selectedIds.includes(m.id)" @change="toggle(m.id)" />
        </label>
        <div class="dot" v-if="!m.readStatus" @click="read(m)"></div>
        <div class="message-body" @click="read(m)">
          <strong>{{ m.title }}</strong>
          <p>{{ m.content }}</p>
          <span class="muted">{{ formatTime(m.createTime) }}</span>
        </div>
        <button class="btn btn-ghost small" @click="removeMessage(m)">删除</button>
      </div>
    </div>
    <div v-else class="panel empty">暂无消息</div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { notifyApi } from '../api'

const messages = ref([])
const selectedIds = ref([])

const allSelected = computed(() => messages.value.length > 0 && selectedIds.value.length === messages.value.length)

const load = async () => {
  const data = await notifyApi.messages({ current: 1, size: 50 })
  messages.value = data.records
  selectedIds.value = selectedIds.value.filter(id => messages.value.some(m => m.id === id))
}

const read = async m => {
  if (!m.readStatus) {
    await notifyApi.markRead(m.id)
    await load()
  }
}

const toggle = id => {
  selectedIds.value = selectedIds.value.includes(id)
    ? selectedIds.value.filter(x => x !== id)
    : [...selectedIds.value, id]
}

const toggleAll = () => {
  selectedIds.value = allSelected.value ? [] : messages.value.map(m => m.id)
}

const removeMessage = async m => {
  if (!window.confirm('确认删除该消息？')) return
  await notifyApi.deleteMessage(m.id)
  await load()
}

const batchRemove = async () => {
  if (!window.confirm(`确认删除选中的 ${selectedIds.value.length} 条消息？`)) return
  await notifyApi.batchDelete(selectedIds.value)
  selectedIds.value = []
  await load()
}

const formatTime = t => (t || '').replace('T', ' ').slice(0, 16)

onMounted(load)
</script>

<style scoped>
.page-title {
  margin: 0 0 18px;
  font-size: 22px;
}

.message-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.check {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--muted);
}

.btn.small {
  padding: 6px 12px;
  font-size: 12px;
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message {
  display: flex;
  gap: 12px;
  padding: 16px;
  cursor: pointer;
  align-items: flex-start;
}

.message.unread {
  border-left: 3px solid var(--primary);
}

.message-body {
  flex: 1;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--primary);
  margin-top: 22px;
  flex-shrink: 0;
}

.message strong {
  font-size: 15px;
}

.message p {
  margin: 6px 0;
  color: var(--muted);
  font-size: 13px;
  line-height: 1.6;
}
</style>
