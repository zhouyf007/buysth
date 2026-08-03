<template>
  <div class="container page">
    <h1 class="page-title">消息通知</h1>
    <div v-if="messages.length" class="message-list">
      <div v-for="m in messages" :key="m.id" :class="['panel', 'message', { unread: !m.readStatus }]" @click="read(m)">
        <div class="dot" v-if="!m.readStatus"></div>
        <div>
          <strong>{{ m.title }}</strong>
          <p>{{ m.content }}</p>
          <span class="muted">{{ formatTime(m.createTime) }}</span>
        </div>
      </div>
    </div>
    <div v-else class="panel empty">暂无消息</div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { notifyApi } from '../api'

const messages = ref([])

const load = async () => {
  const data = await notifyApi.messages({ current: 1, size: 20 })
  messages.value = data.records
}

const read = async m => {
  if (!m.readStatus) {
    await notifyApi.markRead(m.id)
    await load()
  }
}

const formatTime = t => (t || '').replace('T', ' ').slice(0, 16)

onMounted(load)
</script>

<style scoped>
.page-title {
  margin: 0 0 18px;
  font-size: 22px;
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

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--primary);
  margin-top: 6px;
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

