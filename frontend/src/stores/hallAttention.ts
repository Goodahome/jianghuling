import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { listBountyReviews, listSubmissionReviews } from '@/api/hall'
import { useAuthStore } from '@/stores/auth'

/**
 * 执事堂待办红点：令审待审悬赏；若兼验功使则成果待审也计入。
 */
export const useHallAttentionStore = defineStore('hallAttention', () => {
  const pendingBounty = ref(0)
  const pendingSubmission = ref(0)
  let timer: ReturnType<typeof setInterval> | null = null

  const hasAttention = computed(() => pendingBounty.value > 0 || pendingSubmission.value > 0)

  async function refresh() {
    const auth = useAuthStore()
    if (!auth.hasOffice) {
      pendingBounty.value = 0
      pendingSubmission.value = 0
      return
    }
    const tasks: Promise<void>[] = []
    if (auth.hasOfficeCode('DECREE_REVIEWER')) {
      tasks.push(
        listBountyReviews({ status: 'PENDING', page: 1, pageSize: 1 })
          .then((b) => {
            pendingBounty.value = Number(b?.total ?? 0)
          })
          .catch(() => {
            pendingBounty.value = 0
          }),
      )
    } else {
      pendingBounty.value = 0
    }
    if (auth.hasOfficeCode('FEAT_REVIEWER')) {
      tasks.push(
        listSubmissionReviews({ status: 'PENDING', page: 1, pageSize: 1 })
          .then((s) => {
            pendingSubmission.value = Number(s?.total ?? 0)
          })
          .catch(() => {
            pendingSubmission.value = 0
          }),
      )
    } else {
      pendingSubmission.value = 0
    }
    await Promise.all(tasks)
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
    pendingBounty.value = 0
    pendingSubmission.value = 0
  }

  return {
    pendingBounty,
    pendingSubmission,
    hasAttention,
    refresh,
    startPolling,
    stopPolling,
    clear,
  }
})
