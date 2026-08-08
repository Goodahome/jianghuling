<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getHallSubmission, reviewSubmission } from '@/api/hall'
import { useHallAttentionStore } from '@/stores/hallAttention'
import type { ReviewResult } from '@/types/api'
import type { SubmissionDetail } from '@/types/models'
import { resolveSubmissionStatusLabel } from '@/utils/labels'
import HallBackBar from '@/components/HallBackBar.vue'
import SubmissionBody from '@/components/SubmissionBody.vue'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const current = ref<SubmissionDetail | null>(null)

const submissionId = computed(() => String(route.params.id))

const form = reactive({
  result: 'APPROVE' as ReviewResult,
  reason: '',
  itemComments: [] as { itemCode: string; comment: string }[],
})

const canJudge = computed(() => current.value?.status === 'PENDING')

async function load() {
  loading.value = true
  try {
    current.value = await getHallSubmission(submissionId.value)
    form.result = 'APPROVE'
    form.reason = ''
    form.itemComments = (current.value?.items || []).map((it) => ({
      itemCode: it.itemCode,
      comment: '',
    }))
  } finally {
    loading.value = false
  }
}

async function submitReview() {
  if (!current.value) return
  if (!canJudge.value) {
    return ElMessage.warning('仅待审成果可审核（职司不可改判）')
  }
  if (form.result === 'REJECT' && !form.reason.trim()) {
    return ElMessage.warning('驳回请填写原因')
  }
  submitting.value = true
  try {
    await reviewSubmission(current.value.submissionId, {
      result: form.result,
      reason: form.reason,
      itemComments: form.itemComments.filter((c) => c.comment.trim()),
    })
    ElMessage.success('已处理')
    void useHallAttentionStore().refresh()
    router.push('/hall/submission-reviews')
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>

<template>
  <section class="jh-section" v-loading="loading">
    <div class="jh-container narrow" v-if="current">
      <HallBackBar
        :items="[
          { label: '验功队列', to: '/hall/submission-reviews' },
          { label: '验功详情' },
        ]"
      >
        <el-button
          v-if="canJudge"
          class="jh-btn-seal"
          :loading="submitting"
          @click="submitReview"
        >
          提交审核
        </el-button>
      </HallBackBar>

      <div class="head jh-panel">
        <div class="tags">
          <div class="tags-left">
            <span class="type">验功</span>
            <el-tag size="small">{{ resolveSubmissionStatusLabel(current.status) }}</el-tag>
            <span class="pill">成果 #{{ current.submissionId }} · v{{ current.versionNo }}</span>
          </div>
        </div>
        <h1>{{ current.bountyTitle || `悬赏 #${current.bountyId}` }}</h1>
        <p class="jh-muted">
          提交人 {{ current.claimerNickname || `侠士#${current.claimerUserId}` }}
          · {{ current.createdAt }}
        </p>
        <p v-if="current.reviewedAt" class="jh-muted">审核 {{ current.reviewedAt }}</p>
        <p v-if="current.reviewReason" class="reason">审核说明：{{ current.reviewReason }}</p>
      </div>

      <div class="jh-panel block">
        <h2>成果摘要</h2>
        <p class="summary">{{ current.summary || '（无）' }}</p>
      </div>

      <div class="jh-panel block">
        <h2>清单举证</h2>
        <SubmissionBody :items="current.items || []" />
        <div v-if="canJudge" class="comments">
          <div v-for="c in form.itemComments" :key="c.itemCode" class="item-comment">
            <label class="jh-muted">{{ c.itemCode }} 单项意见</label>
            <el-input v-model="c.comment" placeholder="可选" />
          </div>
        </div>
      </div>

      <div v-if="canJudge" class="jh-panel block">
        <h2>落判</h2>
        <el-radio-group v-model="form.result" class="result-group">
          <el-radio-button value="APPROVE">通过</el-radio-button>
          <el-radio-button value="REJECT">驳回</el-radio-button>
        </el-radio-group>
        <el-input
          v-model="form.reason"
          type="textarea"
          :rows="3"
          :placeholder="form.result === 'REJECT' ? '驳回原因（必填）' : '审核意见（可选）'"
        />
      </div>
      <el-alert
        v-else
        type="info"
        :closable="false"
        title="已审成果仅可只读查看（职司不可改判）。"
        class="block-alert"
      />
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
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}
.tags-left {
  display: flex;
  gap: 8px;
  align-items: center;
  color: var(--jh-seal);
  flex-wrap: wrap;
}
.type {
  font-size: 13px;
}
.pill {
  font-size: 12px;
  color: var(--jh-muted);
  border: 1px solid var(--jh-line);
  padding: 2px 8px;
  border-radius: var(--jh-radius);
}
h1 {
  margin: 0 0 8px;
  font-size: clamp(24px, 4vw, 34px);
}
.reason {
  margin: 6px 0 0;
  color: var(--jh-seal);
}
.summary {
  margin: 0;
  white-space: pre-wrap;
  line-height: 1.7;
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
.item-comment {
  margin-top: 10px;
}
.item-comment label {
  display: block;
  font-size: 12px;
  margin-bottom: 4px;
}
.comments {
  margin-top: 12px;
  padding-top: 8px;
  border-top: 1px solid var(--jh-line);
}
.result-group {
  margin-bottom: 12px;
}
.block-alert {
  margin-bottom: 16px;
}
@media (max-width: 768px) {
  .head,
  .block {
    padding: 14px;
  }
}
</style>
