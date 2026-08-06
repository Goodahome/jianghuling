<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getSubmission } from '@/api/bounty'
import { reviewSubmission } from '@/api/hall'
import type { ReviewResult } from '@/types/api'
import type { Submission } from '@/types/models'
import HallBackBar from '@/components/HallBackBar.vue'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const current = ref<(Submission & { bountyTitle?: string; bountyId?: number }) | null>(null)

const submissionId = computed(() => String(route.params.id))

const form = reactive({
  result: 'APPROVE' as ReviewResult,
  reason: '',
  itemComments: [] as { itemCode: string; comment: string }[],
})

async function load() {
  loading.value = true
  try {
    current.value = (await getSubmission(submissionId.value)) as typeof current.value
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
  if (form.result === 'REJECT' && !form.reason.trim()) {
    return ElMessage.warning('驳回请填写原因')
  }
  submitting.value = true
  try {
    await reviewSubmission(current.value.id, {
      result: form.result,
      reason: form.reason,
      itemComments: form.itemComments.filter((c) => c.comment.trim()),
    })
    ElMessage.success('已处理')
    router.push('/hall/submission-reviews')
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>

<template>
  <section class="jh-section" v-loading="loading">
    <div class="jh-container" v-if="current">
      <HallBackBar
        :items="[
          { label: '验功队列', to: '/hall/submission-reviews' },
          { label: '验功详情' },
        ]"
      >
        <el-button class="jh-btn-seal" :loading="submitting" @click="submitReview">提交审核</el-button>
      </HallBackBar>

      <div class="head jh-panel">
        <div class="tags">
          <div class="tags-left">
            <span class="type">验功</span>
            <span class="pill">成果 #{{ current.id }}</span>
          </div>
        </div>
        <h1>{{ current.bountyTitle || `悬赏 #${current.bountyId || '—'}` }}</h1>
        <p class="jh-muted">摘要：{{ current.contentSummary || '（无）' }}</p>
        <p v-if="current.bountyId" class="jh-muted">
          关联悬赏 #{{ current.bountyId }}
        </p>
      </div>

      <div class="jh-panel block">
        <h2>清单举证</h2>
        <div v-for="it in current.items || []" :key="it.itemCode" class="item">
          <div class="item-head">
            <strong>{{ it.itemName || it.itemCode }}</strong>
            <el-tag size="small" :type="it.done ? 'success' : 'info'">
              {{ it.done ? '已完成' : '未完成' }}
            </el-tag>
          </div>
          <p>{{ it.text || '—' }}</p>
          <div v-if="it.mediaUrls?.length" class="imgs">
            <a v-for="u in it.mediaUrls" :key="u" :href="u" target="_blank" rel="noreferrer">
              <img :src="u" alt="" />
            </a>
          </div>
          <el-input
            v-for="c in form.itemComments"
            v-show="c.itemCode === it.itemCode"
            :key="c.itemCode"
            v-model="c.comment"
            placeholder="单项意见（可选）"
            style="margin-top: 6px"
          />
        </div>
      </div>

      <div class="jh-panel block">
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
.block {
  padding: 18px;
  margin-bottom: 16px;
}
h2 {
  margin: 0 0 12px;
  font-size: 18px;
  font-family: var(--jh-font-display);
}
.item {
  padding: 12px 0;
  border-bottom: 1px solid var(--jh-line);
}
.item-head {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 6px;
}
.imgs {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  margin-top: 6px;
}
.imgs img {
  width: 72px;
  height: 54px;
  object-fit: cover;
  border-radius: var(--jh-radius);
}
.result-group {
  margin-bottom: 12px;
}
@media (max-width: 768px) {
  .head,
  .block {
    padding: 14px;
  }
}
</style>
