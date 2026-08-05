<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminCreateInvites, adminInvalidateInvite, adminListInvites } from '@/api/admin'

const list = ref<Record<string, unknown>[]>([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const data = await adminListInvites({ page: 1, pageSize: 50 })
    list.value = data.list || []
  } finally {
    loading.value = false
  }
}

async function create() {
  const res = await adminCreateInvites({ count: 5, quota: 1 })
  const codes = res?.codes || []
  await load()
  if (codes.length) {
    await ElMessageBox.alert(codes.join('\n'), `已生成 ${codes.length} 个邀请码`, {
      confirmButtonText: '知道了',
      customClass: 'invite-codes-alert',
    })
  } else {
    ElMessage.success('已批量生成，请查看列表')
  }
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
    <el-button type="primary" style="margin-bottom: 12px" :loading="loading" @click="create">
      批量生成
    </el-button>
    <el-table v-loading="loading" :data="list">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="code" label="邀请码" min-width="120" />
      <el-table-column prop="ownerNickname" label="归属" width="120" />
      <el-table-column prop="usedCount" label="已用" width="80" />
      <el-table-column prop="quota" label="配额" width="80" />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column prop="expireAt" label="过期" min-width="160" />
      <el-table-column prop="createdAt" label="创建时间" min-width="160" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button
            size="small"
            type="danger"
            :disabled="row.status === 'INVALID'"
            @click="invalidate(Number(row.id))"
          >
            失效
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
