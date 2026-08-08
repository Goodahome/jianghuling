import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { listMyClaimed, listMyPublished } from '@/api/bounty'
import type { BountyListItem } from '@/types/models'
import {
  ensureMineSeenBaseline,
  hasMinePosterAttention,
  markMineBountySeen,
  markMineBountySeenByFields,
  clearMineSeenStorage,
} from '@/utils/mineSeen'

export const useMineAttentionStore = defineStore('mineAttention', () => {
  const published = ref<BountyListItem[]>([])
  const claimed = ref<BountyListItem[]>([])
  const ready = ref(false)
  let timer: ReturnType<typeof setInterval> | null = null

  const attentionIds = computed(() => {
    const ids = new Set<number>()
    for (const item of [...published.value, ...claimed.value]) {
      if (hasMinePosterAttention(item)) ids.add(Number(item.id))
    }
    return ids
  })

  const hasAttention = computed(() => attentionIds.value.size > 0)

  function isPosterHot(id: number | string) {
    return attentionIds.value.has(Number(id))
  }

  async function refresh() {
    try {
      const [pub, cla] = await Promise.all([
        listMyPublished({ page: 1, pageSize: 50 }),
        listMyClaimed({ page: 1, pageSize: 50 }),
      ])
      const pubList = pub?.list || []
      const claList = cla?.list || []
      ensureMineSeenBaseline([...pubList, ...claList])
      published.value = pubList
      claimed.value = claList
      ready.value = true
    } catch {
      /* 未登录或网络失败静默 */
    }
  }

  function startPolling(ms = 20000) {
    stopPolling()
    void refresh()
    timer = setInterval(() => {
      void refresh()
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
    published.value = []
    claimed.value = []
    ready.value = false
    clearMineSeenStorage()
  }

  function markSeenItem(item: BountyListItem) {
    markMineBountySeen(item)
    // 触发 computed 刷新：改引用
    published.value = [...published.value]
    claimed.value = [...claimed.value]
  }

  function markSeenDetail(id: number | string, status: string, submissionCount: number) {
    markMineBountySeenByFields(id, status, submissionCount)
    published.value = [...published.value]
    claimed.value = [...claimed.value]
  }

  return {
    published,
    claimed,
    ready,
    hasAttention,
    attentionIds,
    isPosterHot,
    refresh,
    startPolling,
    stopPolling,
    clear,
    markSeenItem,
    markSeenDetail,
  }
})
