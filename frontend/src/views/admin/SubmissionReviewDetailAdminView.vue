<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminGetSubmission, adminReviewSubmission } from '@/api/admin'
import { useAdminAuthStore } from '@/stores/adminAuth'
import type { SubmissionDetail } from '@/types/models'
import type { ReviewResult } from '@/types/api'
import { resolveSubmissionStatusLabel } from '@/utils/labels'
import SubmissionBody from '@/components/SubmissionBody.vue'

const route = useRoute()
const router = useRouter()
const adminAuth = useAdminAuthStore()

const loading = ref(false)
const submitting = ref(false)
const detail = ref<SubmissionDetail | null>(null)

const form = reactive({
  result: 'APPROVE' as ReviewResult,
  reason: '',
})

const submissionId = computed(() => String(route.params.id || route.params.submissionId || ''))
const canRead = computed(() => adminAuth.hasPermission('submission:read'))
const canReview = computed(() => adminAuth.hasPermission('submission:review'))

async function load() {
  loading.value = true
  try {
    detail.value = await adminGetSubmission(submissionId.value)
    form.result = 'APPROVE'
    form.reason = ''
  } finally {
    loading.value = false
  }
}

async function submitReview() {
  if (!canReview.value) {
    ElMessage.warning('无成果审核权限（submission:review）')
    return
  }
  if (form.result === 'REJECT' && !form.reason.trim()) {
    ElMessage.warning('驳回请填写原因')
    return
  }
  await ElMessageBox.confirm(
    form.result === 'APPROVE' ? '确认通过该成果？（已审可改判）' : '确认驳回该成果？',
    '成果审核',
  )
  submitting.value = true
  try {
    await adminReviewSubmission(submissionId.value, {
      result: form.result,
      reason: form.reason.trim() || null,
      itemComments: [],
    })
    ElMessage.success(form.result === 'APPROVE' ? '已通过' : '已驳回')
    await load()
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>

<template>
  <div v-loading="loading" class="page">
    <div class="toolbar">
      <el-button @click="router.push('/admin/submission-reviews')">返回列表</el-button>
      <el-button
        v-if="detail?.bountyId"
        @click="router.push(`/admin/bounties/${detail.bountyId}`)"
      >
        查看悬赏
      </el-button>
    </div>

    <template v-if="detail">
      <el-card shadow="never" class="block">
        <h2>{{ detail.bountyTitle || `悬赏 #${detail.bountyId}` }}</h2>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="成果 ID">{{ detail.submissionId }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            {{ resolveSubmissionStatusLabel(detail.status) }}
          </el-descriptions-item>
          <el-descriptions-item label="悬赏 ID">{{ detail.bountyId }}</el-descriptions-item>
          <el-descriptions-item label="版本">v{{ detail.versionNo }}</el-descriptions-item>
          <el-descriptions-item label="提交人">
            {{ detail.claimerNickname || '—' }} #{{ detail.claimerUserId }}
          </el-descriptions-item>
          <el-descriptions-item label="Claim ID">{{ detail.claimId }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ detail.createdAt || '—' }}</el-descriptions-item>
          <el-descriptions-item label="审核时间">{{ detail.reviewedAt || '—' }}</el-descriptions-item>
          <el-descriptions-item label="审核说明" :span="2">
            {{ detail.reviewReason || '—' }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card shadow="never" class="block">
        <h3>成果摘要</h3>
        <p class="content">{{ detail.summary || '（无摘要）' }}</p>
      </el-card>

      <el-card shadow="never" class="block">
        <h3>清单正文</h3>
        <SubmissionBody :items="detail.items || []" />
      </el-card>

      <el-card v-if="canReview" shadow="never" class="block">
        <h3>审核操作</h3>
        <p class="hint">管理员可对已审成果改判；驳回须填写原因。</p>
        <el-form label-width="100px" style="max-width: 560px">
          <el-form-item label="结果" required>
            <el-radio-group v-model="form.result">
              <el-radio-button value="APPROVE">通过</el-radio-button>
              <el-radio-button value="REJECT">驳回</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item :label="form.result === 'REJECT' ? '驳回原因' : '说明'" :required="form.result === 'REJECT'">
            <el-input
              v-model="form.reason"
              type="textarea"
              :rows="3"
              :placeholder="form.result === 'REJECT' ? '驳回原因（必填）' : '审核意见（可选）'"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="submitting" @click="submitReview">提交</el-button>
          </el-form-item>
        </el-form>
      </el-card>
      <el-alert
        v-else-if="canRead"
        type="warning"
        :closable="false"
        title="只读：当前账号无 submission:review 权限。"
        class="block"
      />
    </template>
  </div>
</template>

<style scoped>
.page {
  max-width: 960px;
}
.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}
.block {
  margin-bottom: 14px;
}
h2 {
  margin: 0 0 12px;
}
h3 {
  margin: 0 0 10px;
}
.content {
  margin: 0;
  white-space: pre-wrap;
  line-height: 1.7;
}
.hint {
  margin: 0 0 12px;
  color: #909399;
  font-size: 13px;
}
</style>
