<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminGetDispute, adminVerdictDispute } from '@/api/admin'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const detail = ref<Record<string, unknown> | null>(null)

const disputeId = computed(() => String(route.params.id))

const form = reactive({
  action: 'KEEP' as 'KEEP' | 'REALLOCATE' | 'REFUND' | 'PUNISH',
  comment: '',
  reallocUserId: '',
  reallocAmount: '',
  punishUserId: '',
  punishType: 'REPUTATION_DEDUCT' as 'REPUTATION_DEDUCT' | 'BAN',
  punishValue: '0',
})

function parseJsonField(raw: unknown): Record<string, unknown> | null {
  if (raw == null || raw === '') return null
  if (typeof raw === 'object') return raw as Record<string, unknown>
  try {
    return JSON.parse(String(raw)) as Record<string, unknown>
  } catch {
    return null
  }
}

const evidence = computed(() => parseJsonField(detail.value?.evidenceJson))
const evidenceText = computed(() => String(evidence.value?.text || ''))
const evidenceUrls = computed(() => {
  const u = evidence.value?.urls
  return Array.isArray(u) ? (u as string[]) : []
})
const verdict = computed(() => parseJsonField(detail.value?.verdictJson))
const isOpen = computed(() => detail.value?.status === 'OPEN')

async function load() {
  loading.value = true
  try {
    detail.value = await adminGetDispute(disputeId.value)
  } finally {
    loading.value = false
  }
}

watch(
  () => form.action,
  () => {
    /* keep drafts */
  },
)

async function submitVerdict() {
  if (!form.comment.trim()) {
    ElMessage.warning('请填写裁决说明')
    return
  }
  await ElMessageBox.confirm(`确认以 ${form.action} 结案？`, '终裁执行')

  const body: Record<string, unknown> = {
    action: form.action,
    comment: form.comment.trim(),
    reallocations: [] as Array<{ userId: number; amount: number }>,
    punishments: [] as Array<{ userId: number; type: string; value: number }>,
  }

  if (form.action === 'REALLOCATE') {
    const userId = Number(form.reallocUserId)
    const amount = Number(form.reallocAmount)
    if (!Number.isFinite(userId) || !Number.isFinite(amount)) {
      ElMessage.warning('请填写再分配 userId 与 amount')
      return
    }
    body.reallocations = [{ userId, amount }]
  }
  if (form.action === 'PUNISH') {
    const userId = Number(form.punishUserId)
    const value = Number(form.punishValue || 0)
    if (!Number.isFinite(userId)) {
      ElMessage.warning('请填写处罚对象 userId')
      return
    }
    body.punishments = [{ userId, type: form.punishType, value }]
  }

  submitting.value = true
  try {
    await adminVerdictDispute(disputeId.value, body)
    ElMessage.success('裁决已执行')
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
      <el-button @click="router.push('/admin/disputes')">返回列表</el-button>
      <el-button
        v-if="detail?.bountyId"
        type="primary"
        plain
        @click="router.push(`/admin/bounties/${detail.bountyId}`)"
      >
        查看关联悬赏 #{{ detail.bountyId }}
      </el-button>
    </div>

    <template v-if="detail">
      <el-card shadow="never" class="block">
        <h2>纠纷 #{{ detail.id }}</h2>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="状态">{{ detail.status }}</el-descriptions-item>
          <el-descriptions-item label="悬赏 ID">{{ detail.bountyId }}</el-descriptions-item>
          <el-descriptions-item label="发起人">#{{ detail.initiatorId }}</el-descriptions-item>
          <el-descriptions-item label="结算单">{{ detail.settlementId ?? '—' }}</el-descriptions-item>
          <el-descriptions-item label="截止">{{ detail.deadlineAt || '—' }}</el-descriptions-item>
          <el-descriptions-item label="创建">{{ detail.createdAt || '—' }}</el-descriptions-item>
          <el-descriptions-item label="原因" :span="2">{{ detail.reason || '—' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card shadow="never" class="block">
        <h3>举证材料</h3>
        <p class="text">{{ evidenceText || '（无文字举证）' }}</p>
        <div v-if="evidenceUrls.length" class="urls">
          <a v-for="(u, i) in evidenceUrls" :key="i" :href="u" target="_blank" rel="noopener">附件 {{ i + 1 }}</a>
        </div>
        <el-empty v-else description="无附件链接" :image-size="60" />
      </el-card>

      <el-card v-if="verdict" shadow="never" class="block">
        <h3>已有裁决</h3>
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="动作">{{ verdict.action }}</el-descriptions-item>
          <el-descriptions-item label="说明">{{ verdict.comment || '—' }}</el-descriptions-item>
          <el-descriptions-item label="时间">{{ verdict.at || '—' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card v-if="isOpen" shadow="never" class="block">
        <h3>终裁表单</h3>
        <el-form label-width="100px" style="max-width: 560px">
          <el-form-item label="裁决动作" required>
            <el-select v-model="form.action" style="width: 220px">
              <el-option label="KEEP 维持" value="KEEP" />
              <el-option label="REALLOCATE 再分配" value="REALLOCATE" />
              <el-option label="REFUND 退回" value="REFUND" />
              <el-option label="PUNISH 处罚" value="PUNISH" />
            </el-select>
          </el-form-item>
          <el-form-item label="裁决说明" required>
            <el-input v-model="form.comment" type="textarea" :rows="3" />
          </el-form-item>
          <template v-if="form.action === 'REALLOCATE'">
            <el-form-item label="目标用户">
              <el-input v-model="form.reallocUserId" placeholder="userId" />
            </el-form-item>
            <el-form-item label="金额(两)">
              <el-input v-model="form.reallocAmount" placeholder="amount" />
            </el-form-item>
            <p class="hint">MVP：服务端可能仅结案，仍须按契约提交 reallocations。</p>
          </template>
          <template v-if="form.action === 'PUNISH'">
            <el-form-item label="处罚对象">
              <el-input v-model="form.punishUserId" placeholder="userId" />
            </el-form-item>
            <el-form-item label="类型">
              <el-select v-model="form.punishType" style="width: 220px">
                <el-option label="扣声望" value="REPUTATION_DEDUCT" />
                <el-option label="封禁" value="BAN" />
              </el-select>
            </el-form-item>
            <el-form-item label="参数值">
              <el-input v-model="form.punishValue" placeholder="value" />
            </el-form-item>
            <p class="hint">MVP：服务端可能仅结案，仍须按契约提交 punishments。</p>
          </template>
          <el-form-item>
            <el-button type="danger" :loading="submitting" @click="submitVerdict">执行终裁</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </template>
  </div>
</template>

<style scoped>
.page {
  max-width: 900px;
}
.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}
.block {
  margin-bottom: 12px;
}
h2 {
  margin: 0 0 12px;
  font-size: 20px;
}
h3 {
  margin: 0 0 10px;
  font-size: 15px;
}
.text {
  margin: 0 0 8px;
  white-space: pre-wrap;
}
.urls {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.hint {
  margin: 0 0 8px 100px;
  color: #909399;
  font-size: 12px;
}
</style>
