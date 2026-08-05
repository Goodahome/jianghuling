<script setup lang="ts">
import { nextTick, onMounted, onUnmounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { listMessages, sendMessage } from '@/api/bounty'
import type { BountyMessage } from '@/types/models'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const auth = useAuthStore()
const list = ref<BountyMessage[]>([])
const content = ref('')
const box = ref<HTMLElement | null>(null)
let timer: number | undefined

async function load() {
  const data = await listMessages(route.params.id as string, { page: 1, pageSize: 50 })
  list.value = (data.list || []).slice().reverse()
  await nextTick()
  if (box.value) box.value.scrollTop = box.value.scrollHeight
}

async function onSend() {
  if (!content.value.trim()) return
  await sendMessage(route.params.id as string, content.value.trim())
  content.value = ''
  await load()
}

onMounted(async () => {
  await load()
  timer = window.setInterval(load, 8000)
})
onUnmounted(() => clearInterval(timer))
</script>

<template>
  <section class="jh-section">
    <div class="jh-container narrow jh-panel chat">
      <h1>协作会话</h1>
      <div ref="box" class="messages">
        <div
          v-for="m in list"
          :key="m.id"
          class="msg"
          :class="{ mine: m.senderId === auth.user?.id }"
        >
          <div class="meta">{{ m.senderNickname }} · {{ m.createdAt }}</div>
          <div class="bubble">{{ m.content }}</div>
        </div>
      </div>
      <div class="composer">
        <el-input v-model="content" type="textarea" :rows="2" placeholder="回复同道…" @keyup.ctrl.enter="onSend" />
        <el-button type="primary" class="jh-btn-seal" @click="onSend">发送</el-button>
      </div>
    </div>
  </section>
</template>

<style scoped>
.narrow {
  max-width: 720px;
  padding: 16px;
}
h1 {
  margin: 0 0 12px;
  font-size: 24px;
  font-family: var(--jh-font-display);
}
.messages {
  height: min(420px, 50dvh);
  overflow: auto;
  border: 1px solid var(--jh-line);
  border-radius: 8px;
  padding: 12px;
  background: #fff;
  -webkit-overflow-scrolling: touch;
}
.msg {
  margin-bottom: 12px;
  max-width: 80%;
}
.msg.mine {
  margin-left: auto;
  text-align: right;
}
.meta {
  font-size: 12px;
  color: var(--jh-muted);
  margin-bottom: 4px;
}
.bubble {
  display: inline-block;
  padding: 8px 12px;
  border-radius: 10px;
  background: #eef2f5;
  word-break: break-word;
  text-align: left;
}
.msg.mine .bubble {
  background: rgba(178, 58, 45, 0.12);
}
.composer {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 8px;
  margin-top: 12px;
  align-items: end;
}
@media (max-width: 768px) {
  .narrow {
    padding: 12px;
  }
  .messages {
    height: min(360px, 46dvh);
  }
  .composer {
    grid-template-columns: 1fr;
  }
  .composer .el-button {
    width: 100%;
  }
}
</style>
