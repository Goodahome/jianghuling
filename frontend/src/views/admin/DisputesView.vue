<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminGetDispute, adminListDisputes, adminVerdictDispute } from '@/api/admin'

const list = ref<Record<string, unknown>[]>([])
const detail = ref<Record<string, unknown> | null>(null)
const detailVisible = ref(false)

async function load() {
  const data = await adminListDisputes({ page: 1, pageSize: 50 })
  list.value = data.list || []
}

async function open(id: number) {
  detail.value = await adminGetDispute(id)
  detailVisible.value = true
}

function closeDetail() {
  detailVisible.value = false
  detail.value = null
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
  if (detail.value && Number(detail.value.id) === id) {
    detail.value = await adminGetDispute(id)
  }
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

    <el-dialog
      v-model="detailVisible"
      title="纠纷详情"
      width="640px"
      destroy-on-close
      @closed="detail = null"
    >
      <pre class="detail-pre">{{ detail }}</pre>
      <template #footer>
        <el-button @click="closeDetail">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.detail-pre {
  margin: 0;
  max-height: 60vh;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 13px;
  line-height: 1.5;
}
</style>
