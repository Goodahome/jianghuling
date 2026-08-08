<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { adminListFeedbacks } from '@/api/admin'
import type { AdminFeedbackListItem } from '@/types/models'
import type { FeedbackStatus, FeedbackType } from '@/types/api'
import {
  feedbackStatusLabel,
  feedbackTypeLabel,
  resolveFeedbackStatusLabel,
  resolveFeedbackTypeLabel,
} from '@/utils/labels'

const router = useRouter()
const list = ref<AdminFeedbackListItem[]>([])
const total = ref(0)
const loading = ref(false)
const query = reactive({
  page: 1,
  pageSize: 20,
  status: '' as FeedbackStatus | '',
  type: '' as FeedbackType | '',
  keyword: '',
})

const typeOptions = (Object.keys(feedbackTypeLabel) as FeedbackType[]).map((k) => ({
  value: k,
  label: feedbackTypeLabel[k],
}))
const statusOptions = (Object.keys(feedbackStatusLabel) as FeedbackStatus[]).map((k) => ({
  value: k,
  label: feedbackStatusLabel[k],
}))

async function load() {
  loading.value = true
  try {
    const data = await adminListFeedbacks({
      page: query.page,
      pageSize: query.pageSize,
      status: query.status || undefined,
      type: query.type || undefined,
      keyword: query.keyword.trim() || undefined,
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
    <h2>用户反馈</h2>
    <p class="hint">侠士投递的缺陷 / 建议 / 投诉；详情可改状态与处理备注。</p>
    <div class="filters">
      <el-input
        v-model="query.keyword"
        clearable
        placeholder="标题 / 提交人昵称 / ID"
        style="width: 220px"
        @keyup.enter="search"
      />
      <el-select v-model="query.type" clearable placeholder="类型" style="width: 140px">
        <el-option v-for="o in typeOptions" :key="o.value" :label="o.label" :value="o.value" />
      </el-select>
      <el-select v-model="query.status" clearable placeholder="状态" style="width: 140px">
        <el-option v-for="o in statusOptions" :key="o.value" :label="o.label" :value="o.value" />
      </el-select>
      <el-button type="primary" @click="search">查询</el-button>
    </div>
    <el-table v-loading="loading" :data="list">
      <el-table-column prop="createdAt" label="时间" width="180" />
      <el-table-column label="类型" width="110">
        <template #default="{ row }">{{ resolveFeedbackTypeLabel(row.type) }}</template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
      <el-table-column label="提交人" width="140">
        <template #default="{ row }">
          {{ row.submitterNickname || '—' }}
          <span class="sub">#{{ row.submitterId }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">{{ resolveFeedbackStatusLabel(row.status) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="router.push(`/admin/feedbacks/${row.id}`)">
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
