<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  adminApproveLord,
  adminDismissLord,
  adminGetLord,
  adminListLordApplications,
  adminRejectLord,
} from '@/api/admin'

const tab = ref('current')
const lord = ref<Record<string, unknown> | null>(null)
const list = ref<Record<string, unknown>[]>([])

async function loadLord() {
  lord.value = await adminGetLord()
}

async function loadApps() {
  const data = await adminListLordApplications({ page: 1, pageSize: 50 })
  list.value = data.list || []
}

async function approve(id: number) {
  await adminApproveLord(id)
  ElMessage.success('已任命盟主')
  await Promise.all([loadApps(), loadLord()])
}

async function reject(id: number) {
  await adminRejectLord(id, '暂不符合')
  ElMessage.success('已驳回')
  await loadApps()
}

async function dismiss() {
  const { value } = await ElMessageBox.prompt('请输入罢免原因', '罢免现任盟主')
  await adminDismissLord(value)
  ElMessage.success('已罢免')
  await loadLord()
}

onMounted(async () => {
  await Promise.all([loadLord(), loadApps()])
})
</script>

<template>
  <div>
    <h2>盟主管理</h2>
    <el-tabs v-model="tab">
      <el-tab-pane label="现任盟主" name="current">
        <el-descriptions v-if="lord" :column="1" border>
          <el-descriptions-item label="用户ID">{{ lord.userId }}</el-descriptions-item>
          <el-descriptions-item label="昵称">{{ lord.nickname }}</el-descriptions-item>
          <el-descriptions-item label="等级">{{ lord.level }}</el-descriptions-item>
          <el-descriptions-item label="就任时间">{{ lord.startAt || lord.appointedAt }}</el-descriptions-item>
        </el-descriptions>
        <el-empty v-else description="暂无现任盟主" />
        <el-button v-if="lord" type="danger" style="margin-top: 16px" @click="dismiss">罢免</el-button>
      </el-tab-pane>
      <el-tab-pane label="申请审批" name="apps">
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
      </el-tab-pane>
    </el-tabs>
  </div>
</template>
