<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminListNotices, adminSaveNotice } from '@/api/admin'
import { noticeCategoryLabel } from '@/utils/labels'
import type { NoticeCategory } from '@/types/api'

const router = useRouter()
const list = ref<Record<string, unknown>[]>([])
const loading = ref(false)

function categoryText(c: unknown) {
  const key = String(c || '') as NoticeCategory
  return noticeCategoryLabel[key] || key || '—'
}

function statusText(s: unknown) {
  const v = String(s || 'PUBLISHED')
  if (v === 'OFFLINE') return '已下架'
  if (v === 'PUBLISHED') return '已发布'
  return v
}

async function load() {
  loading.value = true
  try {
    const data = await adminListNotices({ page: 1, pageSize: 50 })
    list.value = data.list || []
  } finally {
    loading.value = false
  }
}

async function togglePin(row: Record<string, unknown>) {
  await adminSaveNotice(
    {
      title: row.title,
      category: row.category,
      content: row.content,
      pinned: !row.pinned,
      status: row.status || 'PUBLISHED',
    },
    Number(row.id),
  )
  ElMessage.success(row.pinned ? '已取消置顶' : '已置顶')
  await load()
}

async function setStatus(row: Record<string, unknown>, status: 'PUBLISHED' | 'OFFLINE') {
  const tip = status === 'OFFLINE' ? '确认下架该告示？' : '确认重新发布？'
  await ElMessageBox.confirm(tip, '告示状态')
  await adminSaveNotice(
    {
      title: row.title,
      category: row.category,
      content: row.content,
      pinned: !!row.pinned,
      status,
    },
    Number(row.id),
  )
  ElMessage.success(status === 'OFFLINE' ? '已下架' : '已发布')
  await load()
}

onMounted(load)
</script>

<template>
  <div>
    <div class="toolbar">
      <h2>告示管理</h2>
      <el-button type="primary" @click="router.push('/admin/notices/new')">发布告示</el-button>
    </div>
    <el-table v-loading="loading" :data="list">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" min-width="180" />
      <el-table-column label="分类" width="120">
        <template #default="{ row }">{{ categoryText(row.category) }}</template>
      </el-table-column>
      <el-table-column label="置顶" width="80">
        <template #default="{ row }">{{ row.pinned ? '是' : '否' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">{{ statusText(row.status) }}</template>
      </el-table-column>
      <el-table-column prop="createdAt" label="时间" width="180" />
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="router.push(`/admin/notices/${row.id}/edit`)">编辑</el-button>
          <el-button size="small" @click="togglePin(row)">{{ row.pinned ? '取消置顶' : '置顶' }}</el-button>
          <el-button
            v-if="String(row.status || 'PUBLISHED') !== 'OFFLINE'"
            size="small"
            type="warning"
            @click="setStatus(row, 'OFFLINE')"
          >
            下架
          </el-button>
          <el-button v-else size="small" type="success" @click="setStatus(row, 'PUBLISHED')">发布</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}
h2 {
  margin: 0;
}
</style>
