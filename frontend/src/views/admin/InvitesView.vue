<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { adminCreateInvites, adminInvalidateInvite, adminListInvites } from '@/api/admin'

const list = ref<Record<string, unknown>[]>([])

async function load() {
  const data = await adminListInvites({ page: 1, pageSize: 50 })
  list.value = data.list || []
}

async function create() {
  await adminCreateInvites({ count: 5, quota: 1 })
  ElMessage.success('已批量生成')
  await load()
}

async function invalidate(id: number) {
  await adminInvalidateInvite(id)
  ElMessage.success('已失效')
  await load()
}

onMounted(load)
</script>

<template>
  <div>
    <h2>邀请管理</h2>
    <el-button type="primary" style="margin-bottom: 12px" @click="create">批量生成</el-button>
    <el-table :data="list">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="code" label="邀请码" />
      <el-table-column prop="ownerNickname" label="归属" />
      <el-table-column prop="usedCount" label="已用" width="80" />
      <el-table-column prop="quota" label="配额" width="80" />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button size="small" type="danger" @click="invalidate(Number(row.id))">失效</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
