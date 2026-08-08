<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getSubmission } from '@/api/bounty'
import type { SubmissionDetail } from '@/types/models'
import { resolveSubmissionStatusLabel } from '@/utils/labels'
import { submissionClaimerLabel } from '@/utils/submission'
import PageBreadcrumb from '@/components/PageBreadcrumb.vue'
import SubmissionBody from '@/components/SubmissionBody.vue'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref<SubmissionDetail | null>(null)

const bountyId = computed(() => String(route.params.id || ''))
const submissionId = computed(() => String(route.params.submissionId || ''))

const crumbs = computed(() => [
  { label: '悬赏榜', to: '/plaza' },
  { label: '悬赏详情', to: `/bounties/${bountyId.value}` },
  { label: '成果详情' },
])

async function load() {
  loading.value = true
  try {
    detail.value = await getSubmission(submissionId.value)
  } catch {
    detail.value = null
  } finally {
    loading.value = false
  }
}

watch(submissionId, () => {
  load()
})

onMounted(load)
</script>

<template>
  <section class="jh-section" v-loading="loading">
    <div class="jh-container narrow" v-if="detail">
      <PageBreadcrumb :items="crumbs" />
      <div class="head jh-panel">
        <div class="tags">
          <el-tag size="small">{{ resolveSubmissionStatusLabel(detail.status) }}</el-tag>
          <span class="jh-muted">v{{ detail.versionNo }} · #{{ detail.submissionId || '—' }}</span>
        </div>
        <h1>{{ detail.bountyTitle || `悬赏 #${detail.bountyId}` }}</h1>
        <p class="jh-muted">
          提交人 {{ submissionClaimerLabel(detail) }}
          · {{ detail.createdAt }}
        </p>
        <p v-if="detail.reviewedAt" class="jh-muted">审核时间 {{ detail.reviewedAt }}</p>
        <el-button class="back" @click="router.push(`/bounties/${detail.bountyId}`)">
          返回悬赏
        </el-button>
      </div>

      <div class="jh-panel block">
        <h2>成果摘要</h2>
        <p class="summary">{{ detail.summary || '（无摘要）' }}</p>
        <p v-if="detail.reviewReason" class="reason">审核说明：{{ detail.reviewReason }}</p>
      </div>

      <div class="jh-panel block">
        <h2>清单正文</h2>
        <SubmissionBody :items="detail.items || []" />
      </div>
    </div>
    <div class="jh-container narrow" v-else-if="!loading">
      <PageBreadcrumb :items="crumbs" />
      <el-empty description="成果不存在或无权查看">
        <el-button @click="router.push(`/bounties/${bountyId}`)">返回悬赏</el-button>
      </el-empty>
    </div>
  </section>
</template>

<style scoped>
.head {
  padding: 22px;
  margin-bottom: 16px;
}
.tags {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
h1 {
  margin: 0 0 8px;
  font-size: clamp(22px, 4vw, 28px);
  font-family: var(--jh-font-doc);
  color: var(--jh-ink);
  font-weight: 600;
}
.back {
  margin-top: 12px;
}
.block {
  padding: 18px;
  margin-bottom: 16px;
}
h2 {
  margin: 0 0 12px;
  font-size: 18px;
  font-family: var(--jh-font-display);
}
.summary {
  margin: 0;
  white-space: pre-wrap;
  line-height: 1.7;
}
.reason {
  margin: 10px 0 0;
  color: var(--jh-seal);
  font-size: 14px;
}
@media (max-width: 768px) {
  .head,
  .block {
    padding: 14px;
  }
}
</style>
