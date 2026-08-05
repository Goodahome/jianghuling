<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  adminApproveOffice,
  adminListOfficeApplications,
  adminListOfficeDefs,
  adminPutOfficeDefs,
  adminRejectOffice,
  adminRevokeOfficeHolder,
  adminSuspendOfficeHolder,
} from '@/api/admin'

const tab = ref('defs')
const defs = ref<Record<string, unknown>[]>([])
const apps = ref<Record<string, unknown>[]>([])
const newDef = reactive({
  code: '',
  name: '',
  minLevel: 1,
  quota: 10,
  termDays: 90,
  status: 'ACTIVE',
})

async function loadDefs() {
  defs.value = (await adminListOfficeDefs()) || []
}

async function loadApps() {
  const data = await adminListOfficeApplications({ page: 1, pageSize: 50 })
  apps.value = data.list || []
}

async function saveDefs() {
  await adminPutOfficeDefs(defs.value)
  ElMessage.success('职司定义已保存')
  await loadDefs()
}

async function addDef() {
  if (!newDef.code.trim()) {
    ElMessage.warning('请填写 code')
    return
  }
  await adminPutOfficeDefs([{ ...newDef }])
  ElMessage.success('已新增职司')
  newDef.code = ''
  newDef.name = ''
  await loadDefs()
}

async function approve(id: number) {
  await adminApproveOffice(id)
  ElMessage.success('已授予')
  await loadApps()
  await loadDefs()
}

async function reject(id: number) {
  await adminRejectOffice(id, '不符合条件')
  ElMessage.success('已驳回')
  await loadApps()
}

async function suspendHolder() {
  const { value } = await ElMessageBox.prompt('输入任职记录 ID（user_office.id）', '停职')
  await adminSuspendOfficeHolder(value)
  ElMessage.success('已停职')
  await loadDefs()
}

async function revokeHolder() {
  const { value } = await ElMessageBox.prompt('输入任职记录 ID（user_office.id）', '撤职')
  await adminRevokeOfficeHolder(value)
  ElMessage.success('已撤职')
  await loadDefs()
}

onMounted(async () => {
  await Promise.all([loadDefs(), loadApps()])
})
</script>

<template>
  <div>
    <h2>职司管理</h2>
    <el-tabs v-model="tab">
      <el-tab-pane label="职司定义" name="defs">
        <el-form :inline="true" style="margin-bottom: 12px">
          <el-form-item label="code"><el-input v-model="newDef.code" /></el-form-item>
          <el-form-item label="名称"><el-input v-model="newDef.name" /></el-form-item>
          <el-form-item label="最低等级"><el-input-number v-model="newDef.minLevel" :min="1" /></el-form-item>
          <el-form-item label="名额"><el-input-number v-model="newDef.quota" :min="0" /></el-form-item>
          <el-button type="primary" @click="addDef">新增</el-button>
          <el-button @click="saveDefs">保存全部</el-button>
          <el-button @click="suspendHolder">停职</el-button>
          <el-button type="danger" @click="revokeHolder">撤职</el-button>
        </el-form>
        <el-table :data="defs">
          <el-table-column prop="code" label="code" width="120" />
          <el-table-column label="名称" min-width="120">
            <template #default="{ row }"><el-input v-model="row.name" size="small" /></template>
          </el-table-column>
          <el-table-column label="最低等级" width="120">
            <template #default="{ row }"><el-input-number v-model="row.minLevel" size="small" :min="1" /></template>
          </el-table-column>
          <el-table-column label="名额" width="110">
            <template #default="{ row }"><el-input-number v-model="row.quota" size="small" :min="0" /></template>
          </el-table-column>
          <el-table-column label="任期天" width="110">
            <template #default="{ row }"><el-input-number v-model="row.termDays" size="small" :min="1" /></template>
          </el-table-column>
          <el-table-column prop="holderCount" label="在任" width="80" />
          <el-table-column label="状态" width="120">
            <template #default="{ row }">
              <el-select v-model="row.status" size="small">
                <el-option label="ACTIVE" value="ACTIVE" />
                <el-option label="INACTIVE" value="INACTIVE" />
              </el-select>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="申请审批" name="apps">
        <el-table :data="apps">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="nickname" label="申请人" />
          <el-table-column prop="officeCode" label="职司" />
          <el-table-column prop="statement" label="陈述" />
          <el-table-column prop="status" label="状态" width="120" />
          <el-table-column label="操作" width="180">
            <template #default="{ row }">
              <el-button size="small" type="success" @click="approve(Number(row.id))">授予</el-button>
              <el-button size="small" type="danger" @click="reject(Number(row.id))">驳回</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>
