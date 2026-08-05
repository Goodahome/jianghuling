<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  adminApproveLord,
  adminListLordApplications,
  adminRejectLord,
} from '@/api/admin'

const list = ref<Record<string, unknown>[]>([])

async function load() {
  const data = await adminListLordApplications({ page: 1, pageSize: 50 })
  list.value = data.list || []
}

async function approve(id: number) {
  await adminApproveLord(id)
  ElMessage.success('已任命盟主')
  await load()
}

async function reject(id: number) {
  await adminRejectLord(id, '暂不符合')
  ElMessage.success('已驳回')
  await load()
}

onMounted(load)
</script>

<template>
  <div>
    <h2>武林盟主申请</h2>
    <el-table :data="list">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="nickname" label="申请人" />
      <el-table-column prop="statement" label="陈述" />
      <el-table-column prop="status" label="状态" width="120" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" type="success" @click="approve(Number(row.id))">任命</el-button>
          <el-button size="small" type="danger" @click="reject(Number(row.id))">驳回</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
