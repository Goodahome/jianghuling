<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  adminCreateAdmin,
  adminDisableAdmin,
  adminEnableAdmin,
  adminListAdmins,
  adminListRoles,
  adminResetAdminPassword,
  adminUpdateAdmin,
} from '@/api/admin'

const list = ref<Record<string, unknown>[]>([])
const roles = ref<Record<string, unknown>[]>([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const query = reactive({ page: 1, pageSize: 20, keyword: '', status: '' })
const form = reactive({
  username: '',
  password: '',
  displayName: '',
  roleCodes: [] as string[],
  status: 'ACTIVE',
})

async function load() {
  loading.value = true
  try {
    const data = await adminListAdmins({
      page: query.page,
      pageSize: query.pageSize,
      keyword: query.keyword || undefined,
      status: query.status || undefined,
    })
    list.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

async function loadRoles() {
  try {
    roles.value = await adminListRoles()
  } catch {
    roles.value = []
  }
}

function openCreate() {
  editingId.value = null
  form.username = ''
  form.password = ''
  form.displayName = ''
  form.roleCodes = []
  form.status = 'ACTIVE'
  dialogVisible.value = true
}

function openEdit(row: Record<string, unknown>) {
  editingId.value = Number(row.id)
  form.username = String(row.username || '')
  form.password = ''
  form.displayName = String(row.displayName || '')
  form.roleCodes = Array.isArray(row.roleCodes) ? [...(row.roleCodes as string[])] : []
  form.status = String(row.status || 'ACTIVE')
  dialogVisible.value = true
}

async function save() {
  if (editingId.value == null) {
    if (!form.username.trim() || !form.password.trim()) {
      ElMessage.warning('用户名与初始密码必填')
      return
    }
    await adminCreateAdmin({
      username: form.username.trim(),
      password: form.password,
      displayName: form.displayName.trim() || form.username.trim(),
      roleCodes: form.roleCodes,
      status: form.status,
    })
    ElMessage.success('已创建')
  } else {
    await adminUpdateAdmin(editingId.value, {
      displayName: form.displayName.trim(),
      roleCodes: form.roleCodes,
      status: form.status,
    })
    ElMessage.success('已更新')
  }
  dialogVisible.value = false
  await load()
}

async function resetPwd(id: number) {
  const { value } = await ElMessageBox.prompt('新密码', '重置密码')
  await adminResetAdminPassword(id, value)
  ElMessage.success('密码已重置')
}

async function setEnabled(id: number, enable: boolean) {
  if (enable) await adminEnableAdmin(id)
  else await adminDisableAdmin(id)
  ElMessage.success(enable ? '已启用' : '已停用')
  await load()
}

onMounted(async () => {
  await loadRoles()
  await load()
})
</script>

<template>
  <div>
    <div class="toolbar">
      <h2>管理员账号</h2>
      <el-button type="primary" @click="openCreate">新建管理员</el-button>
    </div>
    <div class="filters">
      <el-input v-model="query.keyword" placeholder="用户名/显示名" style="width: 200px" />
      <el-select v-model="query.status" clearable placeholder="状态" style="width: 120px">
        <el-option label="ACTIVE" value="ACTIVE" />
        <el-option label="DISABLED" value="DISABLED" />
      </el-select>
      <el-button type="primary" :loading="loading" @click="query.page = 1; load()">查询</el-button>
    </div>
    <el-table v-loading="loading" :data="list">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="displayName" label="显示名" width="140" />
      <el-table-column label="角色" min-width="160">
        <template #default="{ row }">
          {{ Array.isArray(row.roleCodes) ? row.roleCodes.join(', ') : '—' }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" @click="resetPwd(Number(row.id))">重置密码</el-button>
          <el-button
            v-if="row.status === 'ACTIVE'"
            size="small"
            type="warning"
            @click="setEnabled(Number(row.id), false)"
          >
            停用
          </el-button>
          <el-button v-else size="small" type="success" @click="setEnabled(Number(row.id), true)">启用</el-button>
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

    <el-dialog v-model="dialogVisible" :title="editingId == null ? '新建管理员' : '编辑管理员'" width="480px">
      <el-form label-width="96px">
        <el-form-item v-if="editingId == null" label="用户名" required>
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item v-if="editingId == null" label="初始密码" required>
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="显示名">
          <el-input v-model="form.displayName" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.roleCodes" multiple style="width: 100%">
            <el-option
              v-for="r in roles"
              :key="String(r.code)"
              :label="`${r.name || r.code} (${r.code})`"
              :value="String(r.code)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 160px">
            <el-option label="ACTIVE" value="ACTIVE" />
            <el-option label="DISABLED" value="DISABLED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
h2 {
  margin: 0;
}
.filters {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}
</style>
