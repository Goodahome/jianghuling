<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { adminGetRole, adminListRoles, adminPermissionCatalog, adminPutRolePermissions } from '@/api/admin'

const roles = ref<Record<string, unknown>[]>([])
const catalog = ref<Record<string, unknown>[]>([])
const activeCode = ref('')
const selected = ref<string[]>([])
const loading = ref(false)
const saving = ref(false)

const activeRole = computed(() => roles.value.find((r) => String(r.code) === activeCode.value) || null)
const isSuper = computed(() => activeCode.value === 'SUPER_ADMIN')

const catalogByModule = computed(() => {
  const map = new Map<string, Record<string, unknown>[]>()
  for (const item of catalog.value) {
    const mod = String(item.module || 'other')
    if (!map.has(mod)) map.set(mod, [])
    map.get(mod)!.push(item)
  }
  return Array.from(map.entries())
})

async function load() {
  loading.value = true
  try {
    const [r, c] = await Promise.all([adminListRoles(), adminPermissionCatalog()])
    roles.value = r || []
    catalog.value = c || []
    if (!activeCode.value && roles.value.length) {
      activeCode.value = String(roles.value[0].code)
      await selectRole(activeCode.value)
    }
  } finally {
    loading.value = false
  }
}

async function selectRole(code: string) {
  activeCode.value = code
  const detail = await adminGetRole(code)
  selected.value = Array.isArray(detail.permissions) ? [...(detail.permissions as string[])] : []
}

async function save() {
  if (isSuper.value) {
    ElMessage.warning('超管权限只读')
    return
  }
  saving.value = true
  try {
    await adminPutRolePermissions(activeCode.value, selected.value)
    ElMessage.success('权限已保存')
    await load()
    await selectRole(activeCode.value)
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<template>
  <div v-loading="loading" class="page">
    <h2>角色权限</h2>
    <el-row :gutter="16">
      <el-col :xs="24" :md="7">
        <el-card shadow="never">
          <el-menu :default-active="activeCode" @select="selectRole">
            <el-menu-item v-for="r in roles" :key="String(r.code)" :index="String(r.code)">
              {{ r.name || r.code }}
            </el-menu-item>
          </el-menu>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="17">
        <el-card v-if="activeRole" shadow="never">
          <div class="head">
            <div>
              <strong>{{ activeRole.name }}</strong>
              <span class="muted"> · {{ activeRole.code }}</span>
              <p class="muted">{{ activeRole.description || '' }}</p>
            </div>
            <el-button type="primary" :disabled="isSuper" :loading="saving" @click="save">保存权限</el-button>
          </div>
          <el-alert
            v-if="isSuper"
            type="info"
            :closable="false"
            title="SUPER_ADMIN 权限集只读"
            style="margin-bottom: 12px"
          />
          <el-checkbox-group v-model="selected" :disabled="isSuper">
            <div v-for="[mod, items] in catalogByModule" :key="mod" class="mod">
              <h4>{{ mod }}</h4>
              <el-checkbox v-for="p in items" :key="String(p.code)" :label="String(p.code)">
                {{ p.name || p.code }}
                <span class="code">{{ p.code }}</span>
              </el-checkbox>
            </div>
          </el-checkbox-group>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
h2 {
  margin: 0 0 12px;
}
.head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}
.muted {
  color: #909399;
  font-size: 13px;
}
.mod {
  margin-bottom: 14px;
}
.mod h4 {
  margin: 0 0 8px;
  font-size: 14px;
}
.mod :deep(.el-checkbox) {
  display: flex;
  margin: 4px 0;
}
.code {
  margin-left: 6px;
  color: #909399;
  font-size: 12px;
}
</style>
