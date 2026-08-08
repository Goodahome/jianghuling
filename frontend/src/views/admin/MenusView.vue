<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminCreateMenu, adminDeleteMenu, adminMenusAll, adminUpdateMenu } from '@/api/admin'

const tree = ref<Record<string, unknown>[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const form = reactive({
  parentId: 0 as number,
  type: 'MENU',
  name: '',
  path: '',
  component: '',
  icon: '',
  sort: 0,
  visible: true,
  permissionCode: '',
})

async function load() {
  loading.value = true
  try {
    tree.value = (await adminMenusAll()) || []
  } finally {
    loading.value = false
  }
}

function openCreate(parentId = 0) {
  editingId.value = null
  form.parentId = parentId
  form.type = 'MENU'
  form.name = ''
  form.path = ''
  form.component = ''
  form.icon = ''
  form.sort = 0
  form.visible = true
  form.permissionCode = ''
  dialogVisible.value = true
}

function openEdit(row: Record<string, unknown>) {
  editingId.value = Number(row.id)
  form.parentId = Number(row.parentId || 0)
  form.type = String(row.type || 'MENU')
  form.name = String(row.name || '')
  form.path = String(row.path || '')
  form.component = String(row.component || '')
  form.icon = String(row.icon || '')
  form.sort = Number(row.sort || 0)
  form.visible = row.visible !== false
  form.permissionCode = String(row.permissionCode || '')
  dialogVisible.value = true
}

async function save() {
  if (!form.name.trim()) {
    ElMessage.warning('名称必填')
    return
  }
  const body = {
    parentId: form.parentId,
    type: form.type,
    name: form.name.trim(),
    path: form.path.trim(),
    component: form.component.trim(),
    icon: form.icon.trim(),
    sort: form.sort,
    visible: form.visible,
    permissionCode: form.permissionCode.trim(),
  }
  if (editingId.value == null) {
    await adminCreateMenu(body)
    ElMessage.success('已新增')
  } else {
    await adminUpdateMenu(editingId.value, body)
    ElMessage.success('已更新')
  }
  dialogVisible.value = false
  await load()
}

async function remove(row: Record<string, unknown>) {
  await ElMessageBox.confirm(`删除菜单「${row.name}」？须无子节点。`, '删除确认')
  await adminDeleteMenu(Number(row.id))
  ElMessage.success('已删除')
  await load()
}

onMounted(load)
</script>

<template>
  <div>
    <div class="toolbar">
      <h2>菜单管理</h2>
      <el-button type="primary" @click="openCreate(0)">新增根节点</el-button>
    </div>
    <el-table
      v-loading="loading"
      :data="tree"
      row-key="id"
      default-expand-all
      :tree-props="{ children: 'children' }"
    >
      <el-table-column prop="name" label="名称" min-width="160" />
      <el-table-column prop="type" label="类型" width="90" />
      <el-table-column prop="path" label="路径" min-width="160" />
      <el-table-column prop="permissionCode" label="权限码" min-width="140" />
      <el-table-column prop="sort" label="排序" width="70" />
      <el-table-column label="可见" width="70">
        <template #default="{ row }">{{ row.visible === false ? '否' : '是' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openCreate(Number(row.id))">加子级</el-button>
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editingId == null ? '新增菜单' : '编辑菜单'" width="520px">
      <el-form label-width="100px">
        <el-form-item label="父节点 ID">
          <el-input-number v-model="form.parentId" :min="0" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.type" style="width: 160px">
            <el-option label="DIR" value="DIR" />
            <el-option label="MENU" value="MENU" />
            <el-option label="BUTTON" value="BUTTON" />
          </el-select>
        </el-form-item>
        <el-form-item label="名称" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="路径">
          <el-input v-model="form.path" placeholder="/admin/xxx" />
        </el-form-item>
        <el-form-item label="组件">
          <el-input v-model="form.component" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="form.icon" />
        </el-form-item>
        <el-form-item label="权限码">
          <el-input v-model="form.permissionCode" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item label="可见">
          <el-switch v-model="form.visible" />
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
</style>
