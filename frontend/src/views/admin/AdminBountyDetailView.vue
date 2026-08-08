<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  adminForceCloseBounty,
  adminGetBounty,
  adminListBountyMessages,
  adminReviewBounty,
} from '@/api/admin'
import { getWarrantTemplates } from '@/api/meta'
import type { BountyDetail, WarrantFieldDef, WarrantTemplate } from '@/types/models'
import {
  bountyStatusLabel,
  difficultyLabel,
  formatAmount,
  formatWarrantValue,
  isWarrantValueEmpty,
  resolveBountyTypeLabel,
  resolveSubmissionStatusLabel,
  warrantFieldFallbackLabel,
} from '@/utils/labels'
import type { BountyStatus } from '@/types/api'

type AdminBountyDetail = BountyDetail & {
  claims?: Array<Record<string, unknown>>
  submissions?: Array<Record<string, unknown>>
}

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const detail = ref<AdminBountyDetail | null>(null)
const warrantTemplates = ref<WarrantTemplate[]>([])
const messages = ref<Record<string, unknown>[]>([])
const messagesLoading = ref(false)
const showMessages = ref(false)

const bountyId = computed(() => String(route.params.id))

const statusText = computed(() => {
  const s = String(detail.value?.status || '') as BountyStatus
  return bountyStatusLabel[s] || s || '—'
})

const canReview = computed(() => detail.value?.status === 'PENDING_REVIEW')
const canForceClose = computed(
  () => !!detail.value && !['COMPLETED', 'CANCELLED', 'REJECTED'].includes(detail.value.status),
)

const warrantFieldDefs = computed((): WarrantFieldDef[] => {
  const type = detail.value?.type
  if (!type) return []
  const tpl = warrantTemplates.value.find((t) => t.type === type || (t as { code?: string }).code === type)
  return tpl?.fields || []
})

const warrantRows = computed(() => {
  const fields = detail.value?.warrantFields || {}
  const rows: { key: string; label: string; text: string }[] = []
  const seen = new Set<string>()
  for (const def of warrantFieldDefs.value) {
    seen.add(def.key)
    const val = fields[def.key]
    if (isWarrantValueEmpty(val) && !def.required) continue
    rows.push({
      key: def.key,
      label: def.label || warrantFieldFallbackLabel[def.key] || def.key,
      text: formatWarrantValue(val) || '—',
    })
  }
  for (const [key, val] of Object.entries(fields)) {
    if (seen.has(key) || isWarrantValueEmpty(val)) continue
    rows.push({
      key,
      label: warrantFieldFallbackLabel[key] || key,
      text: formatWarrantValue(val),
    })
  }
  return rows
})

const claims = computed(() => detail.value?.claims || [])
const submissions = computed(() => detail.value?.submissions || [])

async function load() {
  loading.value = true
  try {
    const [raw, templates] = await Promise.all([
      adminGetBounty(bountyId.value),
      getWarrantTemplates().catch(() => [] as WarrantTemplate[]),
    ])
    detail.value = raw
    warrantTemplates.value = templates
  } finally {
    loading.value = false
  }
}

async function loadMessages() {
  showMessages.value = true
  messagesLoading.value = true
  try {
    const data = await adminListBountyMessages(bountyId.value, { page: 1, pageSize: 100 })
    messages.value = (data.list || []) as Record<string, unknown>[]
  } finally {
    messagesLoading.value = false
  }
}

async function reviewBounty(result: 'APPROVE' | 'REJECT') {
  let reason = ''
  if (result === 'REJECT') {
    const { value } = await ElMessageBox.prompt('驳回原因', '发令审核')
    reason = value
  } else {
    await ElMessageBox.confirm('确认通过该发令？', '发令审核')
  }
  submitting.value = true
  try {
    await adminReviewBounty(bountyId.value, { result, reason })
    ElMessage.success(result === 'APPROVE' ? '已通过' : '已驳回')
    await load()
  } finally {
    submitting.value = false
  }
}

async function forceClose() {
  const { value } = await ElMessageBox.prompt('关闭原因', '强制下架')
  submitting.value = true
  try {
    await adminForceCloseBounty(bountyId.value, value)
    ElMessage.success('已关闭')
    await load()
  } finally {
    submitting.value = false
  }
}

function submissionRowId(row: Record<string, unknown>) {
  return Number(row.submissionId ?? row.id)
}

function goSubmissionDetail(row: Record<string, unknown>) {
  const id = submissionRowId(row)
  if (!id) return
  router.push(`/admin/submission-reviews/${id}`)
}

onMounted(load)
</script>

<template>
  <div v-loading="loading" class="page">
    <div class="toolbar">
      <el-button @click="router.push('/admin/bounties')">返回列表</el-button>
      <div class="ops">
        <el-button type="success" :disabled="!canReview" :loading="submitting" @click="reviewBounty('APPROVE')">
          通过发令
        </el-button>
        <el-button type="warning" :disabled="!canReview" :loading="submitting" @click="reviewBounty('REJECT')">
          驳回发令
        </el-button>
        <el-button type="danger" :disabled="!canForceClose" :loading="submitting" @click="forceClose">
          强制关闭
        </el-button>
        <el-button @click="loadMessages">会话抽检</el-button>
      </div>
    </div>

    <template v-if="detail">
      <el-card shadow="never" class="block">
        <div class="meta">
          <el-tag size="small">{{ resolveBountyTypeLabel(detail.type, detail.typeDisplayName) }}</el-tag>
          <el-tag size="small" type="info">{{ statusText }}</el-tag>
          <el-tag size="small" type="warning">{{ difficultyLabel[detail.difficulty] || detail.difficulty }}</el-tag>
        </div>
        <h2>#{{ detail.id }} · {{ detail.title }}</h2>
        <p>
          赏银 {{ formatAmount(detail.rewardAmount) }} 两 · 揭榜 {{ detail.claimCount ?? claims.length }} 人 · 令主 #{{
            detail.publisherId
          }}
        </p>
        <p class="muted">{{ detail.district || detail.city || '—' }} · 截止 {{ detail.deadlineAt }}</p>
      </el-card>

      <el-row :gutter="16">
        <el-col :xs="24" :md="14">
          <el-card shadow="never" class="block">
            <h3>令状字段</h3>
            <el-empty v-if="!warrantRows.length" description="暂无令状" />
            <el-descriptions v-else :column="1" border size="small">
              <el-descriptions-item v-for="row in warrantRows" :key="row.key" :label="row.label">
                {{ row.text }}
              </el-descriptions-item>
            </el-descriptions>
            <div v-if="detail.taskTags?.length" class="tags">
              <el-tag v-for="t in detail.taskTags" :key="t" size="small">{{ t }}</el-tag>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="24" :md="10">
          <el-card shadow="never" class="block">
            <h3>探子清单</h3>
            <el-empty v-if="!detail.checklist?.length" description="暂无清单" />
            <ul v-else class="checklist">
              <li v-for="item in detail.checklist" :key="item.itemCode">
                <span>{{ item.itemName }}</span>
                <el-tag v-if="item.required" size="small" type="danger">必验</el-tag>
              </li>
            </ul>
          </el-card>
        </el-col>
      </el-row>

      <el-card shadow="never" class="block">
        <h3>揭榜记录</h3>
        <el-table :data="claims" size="small" empty-text="暂无揭榜">
          <el-table-column prop="id" label="Claim ID" width="100" />
          <el-table-column prop="userId" label="侠士 ID" width="100" />
          <el-table-column prop="status" label="状态" width="120" />
          <el-table-column prop="staminaCost" label="体力" width="80" />
          <el-table-column prop="createdAt" label="时间" min-width="160" />
        </el-table>
      </el-card>

      <el-card shadow="never" class="block">
        <h3>成果提交</h3>
        <p class="muted tip">下钻同一详情页审核正文（§16.12.2 = §8.0）</p>
        <el-table :data="submissions" size="small" empty-text="暂无成果">
          <el-table-column label="成果 ID" width="100">
            <template #default="{ row }">{{ row.submissionId ?? row.id }}</template>
          </el-table-column>
          <el-table-column label="提交人" width="140">
            <template #default="{ row }">
              {{ row.claimerNickname || row.userId || '—' }}
              <span v-if="row.claimerUserId" class="muted"> #{{ row.claimerUserId }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="versionNo" label="版本" width="70" />
          <el-table-column label="状态" width="110">
            <template #default="{ row }">
              {{ resolveSubmissionStatusLabel(String(row.status || '')) }}
            </template>
          </el-table-column>
          <el-table-column label="摘要" min-width="160" show-overflow-tooltip>
            <template #default="{ row }">{{ row.summary || row.contentSummary || '—' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button size="small" type="primary" @click="goSubmissionDetail(row)">
                详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-card v-if="showMessages" shadow="never" class="block" v-loading="messagesLoading">
        <h3>会话抽检</h3>
        <el-empty v-if="!messages.length" description="暂无消息" />
        <ul v-else class="msgs">
          <li v-for="m in messages" :key="String(m.id)">
            <strong>#{{ m.senderId }}</strong>
            <span class="muted"> · {{ m.createdAt }}</span>
            <p>{{ m.content }}</p>
          </li>
        </ul>
      </el-card>
    </template>
  </div>
</template>

<style scoped>
.page {
  max-width: 1100px;
}
.toolbar {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 12px;
}
.ops {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.block {
  margin-bottom: 12px;
}
.meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 8px;
}
h2 {
  margin: 0 0 8px;
  font-size: 22px;
}
h3 {
  margin: 0 0 12px;
  font-size: 16px;
}
.tip {
  margin: -4px 0 10px;
  font-size: 12px;
}
.muted {
  color: #909399;
}
.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 12px;
}
.checklist {
  list-style: none;
  margin: 0;
  padding: 0;
}
.checklist li {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  padding: 8px 0;
  border-bottom: 1px solid #ebeef5;
}
.msgs {
  list-style: none;
  margin: 0;
  padding: 0;
}
.msgs li {
  padding: 8px 0;
  border-bottom: 1px solid #ebeef5;
}
.msgs p {
  margin: 4px 0 0;
}
</style>
