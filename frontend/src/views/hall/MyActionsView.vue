<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { listMyActions } from '@/api/hall'

const list = ref<Record<string, unknown>[]>([])

onMounted(async () => {
  const data = await listMyActions({ page: 1, pageSize: 50 })
  list.value = data.list || []
})
</script>

<template>
  <div>
    <h2>本人操作记录</h2>
    <el-table :data="list">
      <el-table-column prop="createdAt" label="时间" width="180" />
      <el-table-column prop="action" label="动作" />
      <el-table-column prop="targetType" label="对象类型" />
      <el-table-column prop="targetId" label="对象ID" width="100" />
      <el-table-column prop="result" label="结果" />
      <el-table-column prop="reason" label="原因" />
    </el-table>
  </div>
</template>
