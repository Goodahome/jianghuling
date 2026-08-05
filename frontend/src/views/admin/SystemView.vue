<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { adminGetSystemConfig, adminListAuditLogs, adminPutSystemConfig } from '@/api/admin'

const configText = ref('{}')
const logs = ref<Record<string, unknown>[]>([])

onMounted(async () => {
  const cfg = await adminGetSystemConfig()
  configText.value = JSON.stringify(cfg || {}, null, 2)
  const data = await adminListAuditLogs({ page: 1, pageSize: 30 })
  logs.value = data.list || []
})

async function save() {
  await adminPutSystemConfig(JSON.parse(configText.value))
  ElMessage.success('系统参数已保存')
}
</script>

<template>
  <div>
    <h2>系统配置与审计</h2>
    <el-input v-model="configText" type="textarea" :rows="12" style="margin-bottom: 12px" />
    <el-button type="primary" @click="save">保存系统参数</el-button>
    <h3 style="margin-top: 24px">最近审计日志</h3>
    <el-table :data="logs">
      <el-table-column prop="createdAt" label="时间" width="180" />
      <el-table-column prop="operator" label="操作者" width="120" />
      <el-table-column prop="action" label="动作" />
      <el-table-column prop="detail" label="详情" />
    </el-table>
  </div>
</template>
