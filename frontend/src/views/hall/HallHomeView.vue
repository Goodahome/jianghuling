<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { listBountyReviews, listSubmissionReviews } from '@/api/hall'
import JhPageHeader from '@/components/JhPageHeader.vue'

const auth = useAuthStore()
const pendingBounty = ref(0)
const pendingSubmission = ref(0)

onMounted(async () => {
  const tasks: Promise<void>[] = []
  if (auth.hasDecreeOffice) {
    tasks.push(
      listBountyReviews({ status: 'PENDING', page: 1, pageSize: 1 })
        .then((b) => {
          pendingBounty.value = b?.total || 0
        })
        .catch(() => {
          pendingBounty.value = 0
        }),
    )
  }
  if (auth.hasFeatOffice) {
    tasks.push(
      listSubmissionReviews({ status: 'PENDING', page: 1, pageSize: 1 })
        .then((s) => {
          pendingSubmission.value = s?.total || 0
        })
        .catch(() => {
          pendingSubmission.value = 0
        }),
    )
  }
  await Promise.all(tasks)
})
</script>

<template>
  <section class="jh-section">
    <div class="jh-container">
      <JhPageHeader
        title="执事堂"
        :subtitle="`欢迎，${auth.user?.nickname || '侠士'}。于此履职，回避由堂规强制。`"
      />

      <div class="cards">
        <RouterLink
          v-if="auth.hasDecreeOffice"
          to="/hall/bounty-reviews"
          class="jh-panel card"
        >
          <span class="label">待审发令</span>
          <strong class="value">{{ pendingBounty }}</strong>
          <span class="go">去令审 →</span>
        </RouterLink>
        <RouterLink
          v-if="auth.hasFeatOffice"
          to="/hall/submission-reviews"
          class="jh-panel card"
        >
          <span class="label">待审成果</span>
          <strong class="value">{{ pendingSubmission }}</strong>
          <span class="go">去验功 →</span>
        </RouterLink>
      </div>
      <p
        v-if="!auth.hasDecreeOffice && !auth.hasFeatOffice"
        class="jh-muted empty-tip"
      >
        当前账号暂无生效职司权限。
      </p>
    </div>
  </section>
</template>

<style scoped>
.cards {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-top: 16px;
}
.card {
  padding: 18px 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  color: inherit;
}
.card:hover {
  border-color: var(--jh-seal);
}
.label {
  color: var(--jh-muted);
  font-size: 14px;
}
.value {
  font-size: 32px;
  font-family: var(--jh-font-display);
  color: var(--jh-seal);
}
.go {
  color: var(--jh-seal);
  font-size: 14px;
}
.empty-tip {
  margin-top: 16px;
}
@media (max-width: 640px) {
  .cards {
    grid-template-columns: 1fr;
  }
}
</style>
