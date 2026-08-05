<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  adminApproveOffice,
  adminListOfficeApplications,
  adminRejectOffice,
} from '@/api/admin'

const list = ref<Record<string, unknown>[]>([])

async function load() {
  const data = await adminListOfficeApplications({ page: 1, pageSize: 50 })
  list.value = data.list || []
}

async function approve(id: number) {
  await adminApproveOffice(id)
  ElMessage.success('已授予')
  await load()
}

async function reject(id: number) {
  await adminRejectOffice(id, '不符合条件')
  ElMessage.success('已驳回')
  await load()
}

onMounted(load)
</script>

<template>
  <div>
    <h2>职司申请管理</h2>
    <el-table :data="list">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="nickname" label="申请人" />
      <el-table-column prop="officeCode" label="职司" />
      <el-table-column prop="statement" label="陈述" />
      <el-table-column prop="status" label="状态" width="120" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" type="success" @click="approve(Number(row.id))">授予</el-button>
          <el-button size="small" type="danger" @click="reject(Number(row.id))">驳回</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
