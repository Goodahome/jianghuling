<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminListUsers, adminUserAction } from '@/api/admin'
import AdminUserAdjustDialog from '@/components/admin/AdminUserAdjustDialog.vue'

const router = useRouter()
const list = ref<Record<string, unknown>[]>([])
const total = ref(0)
const query = reactive({ page: 1, pageSize: 20, keyword: '', status: '' })
const adjustVisible = ref(false)
const adjustUserId = ref<number | null>(null)

async function load() {
  const data = await adminListUsers({
    page: query.page,
    pageSize: query.pageSize,
    keyword: query.keyword || undefined,
    status: query.status || undefined,
  })
  list.value = data.list || []
  total.value = data.total || 0
}

async function act(id: number, action: 'disable' | 'enable' | 'ban' | 'unban') {
  const labels = { disable: '禁用', enable: '启用', ban: '封禁', unban: '解封' }
  await ElMessageBox.confirm(`确认${labels[action]}该侠士？`, '状态变更')
  await adminUserAction(id, action)
  ElMessage.success('已处理')
  await load()
}

function openAdjust(id: number) {
  adjustUserId.value = id
  adjustVisible.value = true
}

/** 产品无硬删接口：删除 = 禁用账号（可再启用） */
async function removeUser(id: number) {
  await ElMessageBox.confirm(
    '将禁用该侠士账号（软删除）。禁用后不可登录，可随时再「启用」恢复。确认删除？',
    '删除侠士',
    { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' },
  )
  await adminUserAction(id, 'disable')
  ElMessage.success('已删除（账号已禁用）')
  await load()
}

onMounted(load)
</script>

<template>
  <div>
    <h2>侠士管理</h2>
    <div class="filters">
      <el-input v-model="query.keyword" placeholder="用户名/手机号/昵称" style="width: 220px" />
      <el-select v-model="query.status" clearable placeholder="状态" style="width: 140px">
        <el-option label="ACTIVE" value="ACTIVE" />
        <el-option label="DISABLED" value="DISABLED" />
        <el-option label="BANNED" value="BANNED" />
      </el-select>
      <el-button type="primary" @click="query.page = 1; load()">查询</el-button>
    </div>
    <el-table :data="list">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="nickname" label="昵称" min-width="120">
        <template #default="{ row }">
          <el-button link type="primary" @click="router.push(`/admin/users/${row.id}`)">
            {{ row.nickname || row.username || row.id }}
          </el-button>
        </template>
      </el-table-column>
      <el-table-column prop="phone" label="手机" width="130" />
      <el-table-column prop="levelTitle" label="等级" width="120" />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column label="操作" width="460" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="router.push(`/admin/users/${row.id}`)">详情</el-button>
          <el-button size="small" @click="act(Number(row.id), 'disable')">禁用</el-button>
          <el-button size="small" @click="act(Number(row.id), 'enable')">启用</el-button>
          <el-button size="small" type="danger" @click="act(Number(row.id), 'ban')">封禁</el-button>
          <el-button size="small" type="success" @click="act(Number(row.id), 'unban')">解封</el-button>
          <el-button size="small" @click="openAdjust(Number(row.id))">调账</el-button>
          <el-button
            size="small"
            type="danger"
            plain
            :disabled="row.status === 'DISABLED'"
            @click="removeUser(Number(row.id))"
          >
            删除
          </el-button>
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

    <AdminUserAdjustDialog
      v-model="adjustVisible"
      :user-id="adjustUserId"
      @success="load"
    />
  </div>
</template>

<style scoped>
.filters {
  margin-bottom: 12px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
</style>
