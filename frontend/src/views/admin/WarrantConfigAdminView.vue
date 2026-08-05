<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  adminCreateWarrantConfig,
  adminDeleteWarrantConfig,
  adminListWarrantConfigs,
  adminUpdateWarrantConfig,
} from '@/api/admin'

const list = ref<Record<string, unknown>[]>([])
const templateCode = ref('')
const form = reactive({
  templateCode: 'RENT_SEEK',
  templateName: '求租',
  fieldKey: '',
  label: '',
  fieldType: 'text',
  required: false,
  sortNo: 0,
  status: 'ACTIVE',
})

async function load() {
  const data = await adminListWarrantConfigs({
    page: 1,
    pageSize: 100,
    templateCode: templateCode.value || undefined,
  })
  list.value = data.list || []
}

async function create() {
  await adminCreateWarrantConfig({ ...form })
  ElMessage.success('已创建字段')
  form.fieldKey = ''
  form.label = ''
  await load()
}

async function save(row: Record<string, unknown>) {
  await adminUpdateWarrantConfig(Number(row.id), {
    label: row.label,
    fieldType: row.fieldType,
    required: row.required,
    sortNo: row.sortNo,
    status: row.status,
    maskUntilClaimed: row.maskUntilClaimed,
  })
  ElMessage.success('已更新（extra 的 label 服务端固定为「补充说明」）')
  await load()
}

async function remove(id: number) {
  await ElMessageBox.confirm('确认删除该字段配置？', '提示')
  await adminDeleteWarrantConfig(id)
  ElMessage.success('已删除')
  await load()
}

onMounted(load)
</script>

<template>
  <div>
    <h2>令状字段配置</h2>
    <div style="margin-bottom: 12px; display: flex; gap: 8px">
      <el-select v-model="templateCode" clearable placeholder="模板筛选" style="width: 180px" @change="load">
        <el-option label="全部" value="" />
        <el-option label="求租 RENT_SEEK" value="RENT_SEEK" />
        <el-option label="出租 RENT_OUT" value="RENT_OUT" />
      </el-select>
      <el-button @click="load">刷新</el-button>
    </div>
    <el-form :inline="true" style="margin-bottom: 12px">
      <el-form-item label="模板">
        <el-select v-model="form.templateCode" style="width: 140px">
          <el-option label="RENT_SEEK" value="RENT_SEEK" />
          <el-option label="RENT_OUT" value="RENT_OUT" />
        </el-select>
      </el-form-item>
      <el-form-item label="key"><el-input v-model="form.fieldKey" /></el-form-item>
      <el-form-item label="label"><el-input v-model="form.label" /></el-form-item>
      <el-form-item label="类型">
        <el-select v-model="form.fieldType" style="width: 110px">
          <el-option label="text" value="text" />
          <el-option label="number" value="number" />
          <el-option label="date" value="date" />
          <el-option label="select" value="select" />
          <el-option label="boolean" value="boolean" />
          <el-option label="textarea" value="textarea" />
        </el-select>
      </el-form-item>
      <el-form-item label="必填"><el-switch v-model="form.required" /></el-form-item>
      <el-button type="primary" @click="create">新增</el-button>
    </el-form>
    <el-table :data="list">
      <el-table-column prop="templateCode" label="模板" width="120" />
      <el-table-column prop="fieldKey" label="key" width="140" />
      <el-table-column label="label" min-width="140">
        <template #default="{ row }"><el-input v-model="row.label" size="small" /></template>
      </el-table-column>
      <el-table-column prop="fieldType" label="类型" width="100" />
      <el-table-column label="必填" width="80">
        <template #default="{ row }"><el-switch v-model="row.required" /></template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="90" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" @click="save(row)">保存</el-button>
          <el-button size="small" type="danger" @click="remove(Number(row.id))">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
