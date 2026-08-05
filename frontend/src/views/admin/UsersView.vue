<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminAdjustAssets, adminListUsers, adminUserAction } from '@/api/admin'

const list = ref<Record<string, unknown>[]>([])
const total = ref(0)
const query = reactive({ page: 1, pageSize: 20, keyword: '' })

async function load() {
  const data = await adminListUsers(query)
  list.value = data.list || []
  total.value = data.total || 0
}

async function act(id: number, action: 'disable' | 'enable' | 'ban' | 'unban') {
  await adminUserAction(id, action)
  ElMessage.success('已处理')
  await load()
}

async function adjust(id: number) {
  const { value } = await ElMessageBox.prompt(
    '格式：BALANCE|CHIVALRY|STAMINA,delta,原因',
    '资产调整',
  )
  const [assetType, delta, reason] = value.split(',')
  await adminAdjustAssets(id, {
    assetType: assetType.trim(),
    delta: Number(delta),
    reason: reason?.trim() || '调账',
  })
  ElMessage.success('已调账（记审计）')
}

onMounted(load)
</script>

<template>
  <div>
    <h2>侠士管理</h2>
    <div style="margin-bottom: 12px; display: flex; gap: 8px">
      <el-input v-model="query.keyword" placeholder="用户名/手机号/昵称" style="width: 220px" />
      <el-button type="primary" @click="query.page = 1; load()">查询</el-button>
    </div>
    <el-table :data="list">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column prop="phone" label="手机" />
      <el-table-column prop="levelTitle" label="等级" />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column label="操作" width="320">
        <template #default="{ row }">
          <el-button size="small" @click="act(Number(row.id), 'disable')">禁用</el-button>
          <el-button size="small" @click="act(Number(row.id), 'enable')">启用</el-button>
          <el-button size="small" type="danger" @click="act(Number(row.id), 'ban')">封禁</el-button>
          <el-button size="small" @click="adjust(Number(row.id))">调账</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-model:current-page="query.page"
      :page-size="query.pageSize"
      :total="total"
      layout="total, prev, pager, next"
      style="margin-top: 12px"
      @current-change="load"
    />
  </div>
</template>
