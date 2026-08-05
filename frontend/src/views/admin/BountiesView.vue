<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  adminForceCloseBounty,
  adminListBounties,
  adminReviewBounty,
  adminReviewSubmission,
} from '@/api/admin'
import { bountyStatusLabel } from '@/utils/labels'
import type { BountyStatus } from '@/types/api'

const list = ref<Record<string, unknown>[]>([])
const query = reactive({ page: 1, pageSize: 20, status: '' })
const loading = ref(false)

const statusOptions = computed(() =>
  (Object.keys(bountyStatusLabel) as BountyStatus[]).map((k) => ({
    value: k,
    label: bountyStatusLabel[k],
  })),
)

function statusText(status: unknown) {
  const key = String(status || '') as BountyStatus
  return bountyStatusLabel[key] || key || '—'
}

function canReview(status: unknown) {
  return status === 'PENDING_REVIEW'
}

function canForceClose(status: unknown) {
  return !['COMPLETED', 'CANCELLED', 'REJECTED'].includes(String(status || ''))
}

async function load() {
  loading.value = true
  try {
    const data = await adminListBounties({
      page: query.page,
      pageSize: query.pageSize,
      status: query.status || undefined,
    })
    list.value = data.list || []
  } finally {
    loading.value = false
  }
}

async function forceClose(id: number) {
  const { value } = await ElMessageBox.prompt('关闭原因', '强制下架')
  await adminForceCloseBounty(id, value)
  ElMessage.success('已关闭')
  await load()
}

async function reviewBounty(id: number, result: string) {
  let reason = ''
  if (result === 'REJECT') {
    const { value } = await ElMessageBox.prompt('驳回原因', '发令审核')
    reason = value
  }
  await adminReviewBounty(id, { result, reason })
  ElMessage.success(result === 'APPROVE' ? '已通过' : '已驳回')
  await load()
}

async function reviewSubmissionPrompt() {
  const { value } = await ElMessageBox.prompt('成果ID,APPROVE|REJECT,原因', '成果审核')
  const [id, result, reason] = value.split(',')
  await adminReviewSubmission(id.trim(), { result: result.trim(), reason: reason?.trim() })
  ElMessage.success('已处理')
}

onMounted(load)
</script>

<template>
  <div>
    <h2>悬赏与双审核</h2>
    <div style="margin-bottom: 12px; display: flex; gap: 8px; flex-wrap: wrap">
      <el-select v-model="query.status" clearable placeholder="全部状态" style="width: 160px">
        <el-option v-for="opt in statusOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
      </el-select>
      <el-button type="primary" :loading="loading" @click="load">查询</el-button>
      <el-button @click="reviewSubmissionPrompt">审核成果…</el-button>
    </div>
    <el-table v-loading="loading" :data="list">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" min-width="160" />
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          {{ statusText(row.status) }}
        </template>
      </el-table-column>
      <el-table-column prop="rewardAmount" label="赏银" width="100" />
      <el-table-column label="操作" width="300" fixed="right">
        <template #default="{ row }">
          <el-button
            size="small"
            type="success"
            :disabled="!canReview(row.status)"
            @click="reviewBounty(Number(row.id), 'APPROVE')"
          >
            通过
          </el-button>
          <el-button
            size="small"
            type="warning"
            :disabled="!canReview(row.status)"
            @click="reviewBounty(Number(row.id), 'REJECT')"
          >
            驳回
          </el-button>
          <el-button
            size="small"
            type="danger"
            :disabled="!canForceClose(row.status)"
            @click="forceClose(Number(row.id))"
          >
            强制关闭
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
