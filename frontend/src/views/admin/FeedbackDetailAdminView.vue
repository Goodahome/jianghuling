<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminGetFeedback, adminUpdateFeedbackStatus } from '@/api/admin'
import { useAdminAuthStore } from '@/stores/adminAuth'
import type { AdminFeedbackDetail } from '@/types/models'
import type { FeedbackStatus } from '@/types/api'
import { resolveFeedbackStatusLabel, resolveFeedbackTypeLabel } from '@/utils/labels'

const route = useRoute()
const router = useRouter()
const adminAuth = useAdminAuthStore()

const loading = ref(false)
const submitting = ref(false)
const detail = ref<AdminFeedbackDetail | null>(null)

const form = reactive({
  status: 'PROCESSING' as FeedbackStatus,
  handleRemark: '',
})

const feedbackId = computed(() => String(route.params.id))
const canWrite = computed(() => adminAuth.hasPermission('feedback:write'))
const attachments = computed(() =>
  Array.isArray(detail.value?.attachmentUrls) ? detail.value!.attachmentUrls! : [],
)
const isTerminal = computed(() => {
  const s = String(detail.value?.status || '')
  return s === 'RESOLVED' || s === 'CLOSED'
})

/** §16.11.3 MVP 合法流转 */
const nextStatusOptions = computed(() => {
  const cur = String(detail.value?.status || '') as FeedbackStatus
  if (cur === 'NEW') {
    return [
      { value: 'PROCESSING' as FeedbackStatus, label: '处理中' },
      { value: 'RESOLVED' as FeedbackStatus, label: '已完结' },
      { value: 'CLOSED' as FeedbackStatus, label: '已关闭' },
    ]
  }
  if (cur === 'PROCESSING') {
    return [
      { value: 'RESOLVED' as FeedbackStatus, label: '已完结' },
      { value: 'CLOSED' as FeedbackStatus, label: '已关闭' },
    ]
  }
  return []
})

async function load() {
  loading.value = true
  try {
    detail.value = await adminGetFeedback(feedbackId.value)
    form.handleRemark = String(detail.value.handleRemark || '')
    const opts = nextStatusOptions.value
    form.status = opts[0]?.value || (detail.value.status as FeedbackStatus)
  } finally {
    loading.value = false
  }
}

async function submitStatus() {
  if (!canWrite.value) {
    ElMessage.warning('无反馈改状态权限（feedback:write）')
    return
  }
  if (!form.status) {
    ElMessage.warning('请选择目标状态')
    return
  }
  await ElMessageBox.confirm(
    `确认将状态改为「${resolveFeedbackStatusLabel(form.status)}」？`,
    '改状态',
  )
  submitting.value = true
  try {
    detail.value = await adminUpdateFeedbackStatus(feedbackId.value, {
      status: form.status,
      handleRemark: form.handleRemark.trim() || undefined,
    })
    ElMessage.success('状态已更新')
    form.handleRemark = String(detail.value.handleRemark || '')
    const opts = nextStatusOptions.value
    form.status = opts[0]?.value || (detail.value.status as FeedbackStatus)
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>

<template>
  <div v-loading="loading" class="page">
    <div class="toolbar">
      <el-button @click="router.push('/admin/feedbacks')">返回列表</el-button>
    </div>

    <template v-if="detail">
      <el-card shadow="never" class="block">
        <h2>{{ detail.title }}</h2>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="ID">{{ detail.id }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            {{ resolveFeedbackStatusLabel(detail.status) }}
          </el-descriptions-item>
          <el-descriptions-item label="类型">
            {{ resolveFeedbackTypeLabel(detail.type) }}
          </el-descriptions-item>
          <el-descriptions-item label="提交人">
            {{ detail.submitterNickname || '—' }} #{{ detail.submitterId }}
          </el-descriptions-item>
          <el-descriptions-item label="联系方式">{{ detail.contact || '—' }}</el-descriptions-item>
          <el-descriptions-item label="相关页面 / 悬赏">
            {{ detail.relatedRef || '—' }}
          </el-descriptions-item>
          <el-descriptions-item label="创建">{{ detail.createdAt || '—' }}</el-descriptions-item>
          <el-descriptions-item label="更新">{{ detail.updatedAt || '—' }}</el-descriptions-item>
          <el-descriptions-item label="最近改状态">
            {{ detail.statusChangedAt || '—' }}
            <span v-if="detail.statusChangedByAdminName">
              · {{ detail.statusChangedByAdminName }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="处理备注" :span="2">
            {{ detail.handleRemark || '—' }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card shadow="never" class="block">
        <h3>正文</h3>
        <p class="content">{{ detail.content }}</p>
        <div v-if="attachments.length" class="atts">
          <p class="label">附件</p>
          <div class="thumbs">
            <a v-for="(u, i) in attachments" :key="i" :href="u" target="_blank" rel="noopener">
              <img :src="u" alt="" />
            </a>
          </div>
        </div>
        <el-empty v-else description="无附件" :image-size="60" />
      </el-card>

      <el-card v-if="detail.statusHistory?.length" shadow="never" class="block">
        <h3>状态轨迹</h3>
        <el-timeline>
          <el-timeline-item
            v-for="(h, i) in detail.statusHistory"
            :key="i"
            :timestamp="h.at"
            placement="top"
          >
            {{ h.fromStatus ? resolveFeedbackStatusLabel(h.fromStatus) : '（创建）' }}
            → {{ resolveFeedbackStatusLabel(h.toStatus) }}
            <span v-if="h.adminName"> · {{ h.adminName }}</span>
            <div v-if="h.remark" class="hist-remark">{{ h.remark }}</div>
          </el-timeline-item>
        </el-timeline>
      </el-card>

      <el-card v-if="canWrite && !isTerminal" shadow="never" class="block">
        <h3>改状态</h3>
        <el-form label-width="100px" style="max-width: 560px">
          <el-form-item label="目标状态" required>
            <el-select v-model="form.status" style="width: 220px">
              <el-option
                v-for="o in nextStatusOptions"
                :key="o.value"
                :label="o.label"
                :value="o.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="处理备注">
            <el-input
              v-model="form.handleRemark"
              type="textarea"
              :rows="3"
              placeholder="传入则覆盖更新；不传保留原备注"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="submitting" @click="submitStatus">提交</el-button>
          </el-form-item>
        </el-form>
      </el-card>
      <el-alert
        v-else-if="canWrite && isTerminal"
        type="info"
        :closable="false"
        title="已终态（已完结 / 已关闭），本期不可回退。"
        class="block"
      />
      <el-alert
        v-else-if="!canWrite"
        type="warning"
        :closable="false"
        title="只读：当前账号无 feedback:write 权限。"
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
  margin: 0 0 12px;
  white-space: pre-wrap;
  line-height: 1.7;
}
.atts .label {
  margin: 0 0 8px;
  font-weight: 600;
}
.thumbs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.thumbs a {
  display: block;
  width: 120px;
  height: 90px;
  border: 1px solid #e5e7eb;
  border-radius: 4px;
  overflow: hidden;
  background: #fff;
}
.thumbs img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.hist-remark {
  margin-top: 4px;
  color: #606266;
  font-size: 13px;
}
</style>
