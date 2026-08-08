<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { adminListSubmissionReviews } from '@/api/admin'
import type { ReviewSubmissionListItem } from '@/types/models'
import type { SubmissionReviewFilter } from '@/types/api'
import { resolveSubmissionStatusLabel } from '@/utils/labels'

const router = useRouter()
const list = ref<ReviewSubmissionListItem[]>([])
const total = ref(0)
const loading = ref(false)
const query = reactive({
  page: 1,
  pageSize: 20,
  status: 'PENDING' as SubmissionReviewFilter | '',
  keyword: '',
  bountyId: '' as string,
})

const statusOptions: { value: SubmissionReviewFilter; label: string }[] = [
  { value: 'PENDING', label: '待审' },
  { value: 'REVIEWED', label: '已审' },
  { value: 'APPROVED', label: '已通过' },
  { value: 'REJECTED', label: '已驳回' },
]

async function load() {
  loading.value = true
  try {
    const data = await adminListSubmissionReviews({
      page: query.page,
      pageSize: query.pageSize,
      status: query.status || undefined,
      keyword: query.keyword.trim() || undefined,
      bountyId: query.bountyId.trim() || undefined,
    })
    list.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function search() {
  query.page = 1
  load()
}

onMounted(load)
</script>

<template>
  <div>
    <h2>成果审核</h2>
    <p class="hint">独立入口审核侠士提交成果；详情可看完整正文后通过/驳回（可改判）。</p>
    <div class="filters">
      <el-input
        v-model="query.keyword"
        clearable
        placeholder="悬赏标题 / 提交人昵称 / ID"
        style="width: 240px"
        @keyup.enter="search"
      />
      <el-input
        v-model="query.bountyId"
        clearable
        placeholder="悬赏 ID"
        style="width: 120px"
        @keyup.enter="search"
      />
      <el-select v-model="query.status" clearable placeholder="状态" style="width: 140px">
        <el-option v-for="o in statusOptions" :key="o.value" :label="o.label" :value="o.value" />
      </el-select>
      <el-button type="primary" @click="search">查询</el-button>
    </div>
    <el-table v-loading="loading" :data="list">
      <el-table-column prop="createdAt" label="提交时间" width="180" />
      <el-table-column label="悬赏" min-width="200" show-overflow-tooltip>
        <template #default="{ row }">
          #{{ row.bountyId }} · {{ row.bountyTitle || '—' }}
        </template>
      </el-table-column>
      <el-table-column label="提交人" width="150">
        <template #default="{ row }">
          {{ row.claimerNickname || '—' }}
          <span class="sub">#{{ row.claimerUserId }}</span>
        </template>
      </el-table-column>
      <el-table-column label="版本" width="70" prop="versionNo" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">{{ resolveSubmissionStatusLabel(row.status) }}</template>
      </el-table-column>
      <el-table-column prop="summary" label="摘要" min-width="180" show-overflow-tooltip />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button
            size="small"
            type="primary"
            @click="router.push(`/admin/submission-reviews/${row.submissionId}`)"
          >
            详情
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-model:current-page="query.page"
      :page-size="query.pageSize"
      :total="total"
      layout="total, prev, pager, next"
      style="margin-top: 12px"
      @current-change="load"
    />
  </div>
</template>

<style scoped>
h2 {
  margin: 0 0 4px;
}
.hint {
  margin: 0 0 12px;
  color: #909399;
  font-size: 13px;
}
.filters {
  margin-bottom: 12px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.sub {
  color: #909399;
  font-size: 12px;
  margin-left: 4px;
}
</style>
