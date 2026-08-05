<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminGetDispute, adminListDisputes, adminVerdictDispute } from '@/api/admin'

const list = ref<Record<string, unknown>[]>([])
const detail = ref<Record<string, unknown> | null>(null)

async function load() {
  const data = await adminListDisputes({ page: 1, pageSize: 50 })
  list.value = data.list || []
}

async function open(id: number) {
  detail.value = await adminGetDispute(id)
}

async function verdict(id: number) {
  const { value } = await ElMessageBox.prompt(
    '裁决：KEEP|REALLOCATE|REFUND|PUNISH + 备注',
    '终裁执行',
  )
  const [action, ...rest] = value.split(',')
  await adminVerdictDispute(id, { action: action.trim(), comment: rest.join(',').trim() })
  ElMessage.success('裁决已执行')
  await load()
}

onMounted(load)
</script>

<template>
  <div>
    <h2>纠纷仲裁</h2>
    <el-table :data="list">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="bountyId" label="悬赏" width="100" />
      <el-table-column prop="status" label="状态" width="120" />
      <el-table-column prop="reason" label="原因" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="open(Number(row.id))">详情</el-button>
          <el-button size="small" type="danger" @click="verdict(Number(row.id))">终裁</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-card v-if="detail" style="margin-top: 12px">
      <pre>{{ detail }}</pre>
    </el-card>
  </div>
</template>
