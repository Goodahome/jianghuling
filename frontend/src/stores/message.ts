import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUnreadCount } from '@/api/message'

export const useMessageStore = defineStore('message', () => {
  const unreadCount = ref(0)
  let timer: ReturnType<typeof setInterval> | null = null

  async function refreshUnread() {
    try {
      const data = await getUnreadCount()
      unreadCount.value = Number(data?.count ?? 0)
    } catch {
      // 未登录或网络失败时不打扰用户
    }
  }

  function startPolling(ms = 30000) {
    stopPolling()
    void refreshUnread()
    timer = setInterval(() => {
      void refreshUnread()
    }, ms)
  }

  function stopPolling() {
    if (timer) {
      clearInterval(timer)
      timer = null
    }
  }

  function clear() {
    stopPolling()
    unreadCount.value = 0
  }

  return { unreadCount, refreshUnread, startPolling, stopPolling, clear }
})
