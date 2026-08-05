<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { listMessages, markAllRead, markRead } from '@/api/message'
import type { SiteMessage } from '@/types/models'
import EmptyState from '@/components/EmptyState.vue'

const list = ref<SiteMessage[]>([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const data = await listMessages({ page: 1, pageSize: 50 })
    list.value = data.list || []
  } finally {
    loading.value = false
  }
}

async function onRead(id: number) {
  await markRead(id)
  await load()
}

async function onReadAll() {
  await markAllRead()
  await load()
}

onMounted(load)
</script>

<template>
  <section class="jh-section">
    <div class="jh-container narrow">
      <div class="head">
        <h1 class="brand-title">站内消息</h1>
        <el-button @click="onReadAll">全部已读</el-button>
      </div>
      <div v-loading="loading" class="list">
        <EmptyState v-if="!loading && !list.length" title="暂无消息" />
        <div v-for="m in list" :key="m.id" class="jh-panel item" :class="{ unread: !m.read }">
          <div class="row">
            <strong>{{ m.title }}</strong>
            <el-button v-if="!m.read" link type="primary" @click="onRead(m.id)">标为已读</el-button>
          </div>
          <p>{{ m.content }}</p>
          <p class="jh-muted">{{ m.createdAt }}</p>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.narrow {
  max-width: 760px;
}
.head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
h1 {
  margin: 0;
  font-size: 32px;
}
.list {
  margin-top: 14px;
  display: grid;
  gap: 10px;
}
.item {
  padding: 14px;
}
.item.unread {
  border-color: rgba(178, 58, 45, 0.35);
}
.row {
  display: flex;
  justify-content: space-between;
}
</style>
