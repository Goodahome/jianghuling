<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  adminCreateChecklistTemplate,
  adminDeleteChecklistTemplate,
  adminListChecklistTemplates,
  adminUpdateChecklistTemplate,
} from '@/api/admin'

const list = ref<Record<string, unknown>[]>([])
const form = reactive({
  itemCode: '',
  itemName: '',
  required: false,
  tagsText: '',
  sortNo: 0,
  status: 'ACTIVE',
})

async function load() {
  const data = await adminListChecklistTemplates({ page: 1, pageSize: 100 })
  list.value = data.list || []
}

async function create() {
  await adminCreateChecklistTemplate({
    itemCode: form.itemCode,
    itemName: form.itemName,
    required: form.required,
    tags: form.tagsText
      .split(/[,，]/)
      .map((s) => s.trim())
      .filter(Boolean),
    sortNo: form.sortNo,
    status: form.status,
  })
  ElMessage.success('已创建')
  form.itemCode = ''
  form.itemName = ''
  form.tagsText = ''
  await load()
}

async function save(row: Record<string, unknown>) {
  await adminUpdateChecklistTemplate(Number(row.id), {
    itemName: row.itemName,
    required: row.required,
    tags: row.tags,
    sortNo: row.sortNo,
    status: row.status,
  })
  ElMessage.success('已更新')
}

async function remove(id: number) {
  await ElMessageBox.confirm('将标记为 INACTIVE', '删除模板')
  await adminDeleteChecklistTemplate(id)
  ElMessage.success('已下线')
  await load()
}

onMounted(load)
</script>

<template>
  <div>
    <h2>探子清单模板</h2>
    <el-form :inline="true" style="margin-bottom: 12px">
      <el-form-item label="code"><el-input v-model="form.itemCode" /></el-form-item>
      <el-form-item label="名称"><el-input v-model="form.itemName" /></el-form-item>
      <el-form-item label="标签"><el-input v-model="form.tagsText" placeholder="帮带看,帮验房" /></el-form-item>
      <el-form-item label="必验"><el-switch v-model="form.required" /></el-form-item>
      <el-button type="primary" @click="create">新增</el-button>
    </el-form>
    <el-table :data="list">
      <el-table-column prop="itemCode" label="code" width="160" />
      <el-table-column label="名称" min-width="140">
        <template #default="{ row }"><el-input v-model="row.itemName" size="small" /></template>
      </el-table-column>
      <el-table-column label="必验" width="80">
        <template #default="{ row }"><el-switch v-model="row.required" /></template>
      </el-table-column>
      <el-table-column prop="tags" label="标签" min-width="140">
        <template #default="{ row }">{{ Array.isArray(row.tags) ? row.tags.join(',') : row.tags }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" @click="save(row)">保存</el-button>
          <el-button size="small" type="danger" @click="remove(Number(row.id))">下线</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
