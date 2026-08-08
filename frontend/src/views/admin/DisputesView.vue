<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { adminListDisputes } from '@/api/admin'

const router = useRouter()
const list = ref<Record<string, unknown>[]>([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const data = await adminListDisputes({ page: 1, pageSize: 50 })
    list.value = data.list || []
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div>
    <h2>纠纷仲裁</h2>
    <p class="hint">点击详情进入结构化举证与终裁页（禁止 JSON dump）。</p>
    <el-table v-loading="loading" :data="list">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="bountyId" label="悬赏" width="100">
        <template #default="{ row }">
          <el-button link type="primary" @click="router.push(`/admin/bounties/${row.bountyId}`)">
            #{{ row.bountyId }}
          </el-button>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="120" />
      <el-table-column prop="reason" label="原因" min-width="200" show-overflow-tooltip />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="router.push(`/admin/disputes/${row.id}`)">
            详情
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<style scoped>
.hint {
  margin: -4px 0 12px;
  color: #909399;
  font-size: 13px;
}
</style>
