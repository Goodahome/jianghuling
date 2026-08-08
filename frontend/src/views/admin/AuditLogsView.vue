<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { adminListAuditLogs } from '@/api/admin'

const loading = ref(false)
const list = ref<Record<string, unknown>[]>([])
const total = ref(0)
const query = reactive({
  page: 1,
  pageSize: 20,
  operator: '',
  action: '',
  keyword: '',
})

async function load() {
  loading.value = true
  try {
    const data = await adminListAuditLogs({
      page: query.page,
      pageSize: query.pageSize,
      operator: query.operator.trim() || undefined,
      action: query.action.trim() || undefined,
      keyword: query.keyword.trim() || undefined,
    })
    list.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function onSearch() {
  query.page = 1
  void load()
}

function onReset() {
  query.operator = ''
  query.action = ''
  query.keyword = ''
  query.page = 1
  void load()
}

onMounted(load)
</script>

<template>
  <div class="audit-page">
    <h2>审计日志</h2>
    <p class="tip">记录后台关键操作（调参、审核、调账等），只读。</p>

    <el-form class="filters" inline @submit.prevent="onSearch">
      <el-form-item label="操作者">
        <el-input v-model="query.operator" clearable placeholder="如 admin:1" style="width: 160px" />
      </el-form-item>
      <el-form-item label="动作">
        <el-input v-model="query.action" clearable placeholder="如 CONFIG_GROWTH" style="width: 180px" />
      </el-form-item>
      <el-form-item label="关键词">
        <el-input
          v-model="query.keyword"
          clearable
          placeholder="匹配动作或详情"
          style="width: 200px"
          @keyup.enter="onSearch"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSearch">查询</el-button>
        <el-button @click="onReset">重置</el-button>
        <el-button @click="load">刷新</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="list" stripe border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="createdAt" label="时间" width="180" />
      <el-table-column prop="operator" label="操作者" width="140" />
      <el-table-column prop="action" label="动作" min-width="160" show-overflow-tooltip />
      <el-table-column prop="detail" label="详情" min-width="280" show-overflow-tooltip />
    </el-table>

    <div class="pager">
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.pageSize"
        :total="total"
        :page-sizes="[20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        background
        @current-change="load"
        @size-change="
          () => {
            query.page = 1
            load()
          }
        "
      />
    </div>
  </div>
</template>

<style scoped>
.audit-page h2 {
  margin: 0 0 6px;
}
.tip {
  margin: 0 0 14px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
.filters {
  margin-bottom: 8px;
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
