<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getBounty, listMessages, sendMessage } from '@/api/bounty'
import type { BountyMessage } from '@/types/models'
import { useAuthStore } from '@/stores/auth'
import { useMineAttentionStore } from '@/stores/mineAttention'
import PageBreadcrumb from '@/components/PageBreadcrumb.vue'

const route = useRoute()
const auth = useAuthStore()
const mineAttention = useMineAttentionStore()
const bountyId = String(route.params.id || '')
const crumbs = computed(() => {
  const from = String(route.query.from || '')
  if (from === 'mine') {
    return [
      { label: '我的悬赏', to: '/mine' },
      { label: '悬赏详情', to: { path: `/bounties/${bountyId}`, query: { from: 'mine' } } },
      { label: '协作会话' },
    ]
  }
  return [
    { label: '悬赏榜', to: '/plaza' },
    { label: '悬赏详情', to: { path: `/bounties/${bountyId}`, query: { from: 'plaza' } } },
    { label: '协作会话' },
  ]
})
const list = ref<BountyMessage[]>([])
const content = ref('')
const sending = ref(false)
const canSendMessage = ref(false)
const capsReady = ref(false)
/** 详情无 capabilities 时与详情页一致提示，避免误以为「协作中禁发」 */
const capsMissing = ref(false)
const box = ref<HTMLElement | null>(null)
let timer: number | undefined

/** 与消息 senderId 统一为 number，避免字符串/数字不相等导致双方气泡错位 */
const myUserId = computed(() => {
  const id = auth.me?.id ?? auth.user?.id
  return id == null ? null : Number(id)
})

function isMine(m: BountyMessage) {
  if (myUserId.value == null || m.senderId == null) return false
  return Number(m.senderId) === myUserId.value
}

function senderLabel(m: BountyMessage) {
  const name = (m.senderNickname || '').trim()
  if (name) return name
  return isMine(m) ? '我' : `侠士#${m.senderId}`
}

function nearBottom(el: HTMLElement) {
  return el.scrollHeight - el.scrollTop - el.clientHeight < 80
}

async function scrollToBottom(force = false) {
  await nextTick()
  const el = box.value
  if (!el) return
  if (force || nearBottom(el)) {
    el.scrollTop = el.scrollHeight
  }
}

function mergeMessages(incoming: BountyMessage[]) {
  const byId = new Map<number, BountyMessage>()
  for (const m of list.value) byId.set(Number(m.id), m)
  for (const m of incoming) byId.set(Number(m.id), m)
  return Array.from(byId.values()).sort((a, b) => Number(a.id) - Number(b.id))
}

async function loadCapabilities() {
  try {
    const bounty = await getBounty(bountyId)
    capsMissing.value = bounty.capabilities == null
    canSendMessage.value = !!bounty.capabilities?.canSendMessage
  } catch {
    capsMissing.value = true
    canSendMessage.value = false
  } finally {
    capsReady.value = true
  }
}

async function load(opts?: { forceScroll?: boolean }) {
  try {
    const data = await listMessages(bountyId, { page: 1, pageSize: 50 })
    // 接口升序；共享流含双方消息，禁止按发送方过滤
    const incoming = (data.list || []).slice()
    const stayBottom = opts?.forceScroll || (box.value ? nearBottom(box.value) : true)
    list.value = mergeMessages(incoming)
    await scrollToBottom(stayBottom)
  } catch {
    /* 轮询失败不打断会话页 */
  }
}

async function onSend() {
  const text = content.value.trim()
  if (!text || sending.value || !canSendMessage.value) return
  sending.value = true
  try {
    const created = await sendMessage(bountyId, text)
    content.value = ''
    if (created?.id != null) {
      list.value = mergeMessages([created])
      await scrollToBottom(true)
    }
    // 立即重拉，保证与对方侧列表一致（api.md §7.7）
    await load({ forceScroll: true })
  } catch (err: unknown) {
    const code = (err as { code?: number })?.code
    if (code === 43008) {
      canSendMessage.value = false
    }
  } finally {
    sending.value = false
  }
}

function onVisibility() {
  if (document.visibilityState === 'visible') {
    void load()
  }
}

onMounted(async () => {
  if (!auth.user && auth.token) {
    try {
      await auth.fetchMe()
    } catch {
      /* ignore */
    }
  }
  await loadCapabilities()
  await load({ forceScroll: true })
  timer = window.setInterval(() => {
    if (document.visibilityState === 'visible') void load()
  }, 8000)
  document.addEventListener('visibilitychange', onVisibility)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  document.removeEventListener('visibilitychange', onVisibility)
  void mineAttention.refresh()
})
</script>

<template>
  <section class="jh-section">
    <div class="jh-container narrow">
      <PageBreadcrumb :items="crumbs" />
      <div class="jh-panel chat">
        <h1>协作会话</h1>
        <p class="hint">令主与揭榜同道共享此会话 · 约 8 秒自动刷新</p>
        <div ref="box" class="messages">
          <div
            v-for="m in list"
            :key="m.id"
            class="msg"
            :class="{ mine: isMine(m) }"
          >
            <div class="meta">{{ senderLabel(m) }} · {{ m.createdAt }}</div>
            <div class="bubble">{{ m.content }}</div>
          </div>
          <p v-if="!list.length" class="empty">尚无往来，先捎句话吧</p>
        </div>
        <div v-if="capsReady && canSendMessage" class="composer">
          <el-input
            v-model="content"
            type="textarea"
            :rows="2"
            placeholder="回复同道…"
            @keyup.ctrl.enter="onSend"
          />
          <el-button type="primary" class="jh-btn-seal" :loading="sending" @click="onSend">
            发送
          </el-button>
        </div>
        <div v-else-if="capsReady && capsMissing" class="caps-fail">
          <p class="readonly-tip">能力状态加载失败</p>
          <el-button size="small" plain @click="loadCapabilities">重试</el-button>
        </div>
        <p v-else-if="capsReady" class="readonly-tip">当前不可发送</p>
      </div>
    </div>
  </section>
</template>

<style scoped>
.chat {
  padding: 16px;
}
h1 {
  margin: 0 0 6px;
  font-size: 24px;
  font-family: var(--jh-font-display);
}
.hint {
  margin: 0 0 12px;
  font-size: 12px;
  color: var(--jh-muted);
}
.caps-fail {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 10px;
}
.caps-fail .readonly-tip {
  margin: 0;
}
.messages {
  height: min(420px, 50dvh);
  overflow: auto;
  border: 1px solid var(--jh-line);
  border-radius: 8px;
  padding: 12px;
  background: transparent;
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
  background: rgba(247, 240, 221, 0.1);
  border: 1px solid rgba(196, 163, 90, 0.28);
  word-break: break-word;
  text-align: left;
}
.msg.mine .bubble {
  background: rgba(196, 163, 90, 0.22);
  border-color: rgba(196, 163, 90, 0.45);
}
.empty {
  margin: 24px 0;
  text-align: center;
  color: var(--jh-muted);
  font-size: 13px;
}
.composer {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 8px;
  margin-top: 12px;
  align-items: end;
}
.readonly-tip {
  margin: 12px 0 0;
  padding: 10px 12px;
  text-align: center;
  font-size: 13px;
  color: var(--jh-muted);
  background: transparent;
  border: 1px dashed var(--jh-line);
  border-radius: 8px;
}
@media (max-width: 768px) {
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
