<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  adminGetUser,
  adminGetUserRealName,
  adminListUserLoginLogs,
  adminUpdateUserRealName,
  adminUpdateUserRemark,
  adminUserAction,
} from '@/api/admin'
import AdminUserAdjustDialog from '@/components/admin/AdminUserAdjustDialog.vue'
import { formatAmount } from '@/utils/labels'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref<Record<string, unknown> | null>(null)
const realName = ref<{ realName?: string; idNumber?: string; status?: string } | null>(null)
const logs = ref<Record<string, unknown>[]>([])
const logQuery = reactive({ page: 1, pageSize: 10 })
const logTotal = ref(0)
const remarkDraft = ref('')
const adjustVisible = ref(false)

const userId = computed(() => String(route.params.id))
const profile = computed(() => (detail.value?.profile || {}) as Record<string, unknown>)
const asset = computed(() => (detail.value?.asset || {}) as Record<string, unknown>)
const wallet = computed(() => (detail.value?.wallet || {}) as Record<string, unknown>)

async function load() {
  loading.value = true
  try {
    detail.value = await adminGetUser(userId.value)
    remarkDraft.value = String(detail.value?.remark || '')
    await Promise.all([loadRealName(), loadLogs()])
  } finally {
    loading.value = false
  }
}

async function loadRealName() {
  try {
    realName.value = await adminGetUserRealName(userId.value)
  } catch {
    realName.value = null
  }
}

async function loadLogs() {
  try {
    const data = await adminListUserLoginLogs(userId.value, logQuery)
    logs.value = data.list || []
    logTotal.value = data.total || 0
  } catch {
    logs.value = []
    logTotal.value = 0
  }
}

async function saveRemark() {
  await adminUpdateUserRemark(userId.value, remarkDraft.value)
  ElMessage.success('备注已保存')
  await load()
}

async function act(action: 'disable' | 'enable' | 'ban' | 'unban') {
  const labels = { disable: '禁用', enable: '启用', ban: '封禁', unban: '解封' }
  await ElMessageBox.confirm(`确认${labels[action]}该侠士？`, '状态变更')
  await adminUserAction(userId.value, action)
  ElMessage.success('已处理')
  await load()
}

/** 产品无硬删接口：删除 = 禁用账号（可再启用） */
async function removeUser() {
  await ElMessageBox.confirm(
    '将禁用该侠士账号（软删除）。禁用后不可登录，可随时再「启用」恢复。确认删除？',
    '删除侠士',
    { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' },
  )
  await adminUserAction(userId.value, 'disable')
  ElMessage.success('已删除（账号已禁用）')
  await load()
}

async function setRealNameStatus(status: string) {
  await adminUpdateUserRealName(userId.value, status)
  ElMessage.success('实名状态已更新')
  await loadRealName()
}

onMounted(load)
</script>

<template>
  <div v-loading="loading" class="page">
    <div class="toolbar">
      <el-button @click="router.push('/admin/users')">返回列表</el-button>
      <div class="ops">
        <el-button size="small" @click="act('enable')">启用</el-button>
        <el-button size="small" @click="act('disable')">禁用</el-button>
        <el-button size="small" type="danger" @click="act('ban')">封禁</el-button>
        <el-button size="small" type="success" @click="act('unban')">解封</el-button>
        <el-button size="small" @click="adjustVisible = true">调账</el-button>
        <el-button
          size="small"
          type="danger"
          plain
          :disabled="detail?.status === 'DISABLED'"
          @click="removeUser"
        >
          删除
        </el-button>
        <el-button size="small" @click="router.push('/admin/offices')">职司管理</el-button>
        <el-button size="small" @click="router.push('/admin/invites')">邀请管理</el-button>
      </div>
    </div>

    <AdminUserAdjustDialog v-model="adjustVisible" :user-id="userId" @success="load" />

    <template v-if="detail">
      <el-card shadow="never" class="block">
        <h2>#{{ detail.id }} · {{ profile.nickname || detail.username }}</h2>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="用户名">{{ detail.username }}</el-descriptions-item>
          <el-descriptions-item label="手机">{{ detail.phone || '—' }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ detail.status }}</el-descriptions-item>
          <el-descriptions-item label="等级">{{ detail.levelTitle || detail.level }}</el-descriptions-item>
          <el-descriptions-item label="简介" :span="2">{{ profile.bio || '—' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-row :gutter="12">
        <el-col :xs="24" :md="12">
          <el-card shadow="never" class="block">
            <h3>资产 / 声望</h3>
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item label="侠义">{{ asset.chivalry ?? '—' }}</el-descriptions-item>
              <el-descriptions-item label="体力">{{ asset.stamina ?? '—' }}</el-descriptions-item>
              <el-descriptions-item label="完成单">{{ asset.completedOrders ?? '—' }}</el-descriptions-item>
              <el-descriptions-item label="好评率">{{ asset.goodRate ?? '—' }}</el-descriptions-item>
              <el-descriptions-item label="声望">{{ asset.reputationScore ?? '—' }}</el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>
        <el-col :xs="24" :md="12">
          <el-card shadow="never" class="block">
            <h3>钱庄</h3>
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item label="余额">{{ formatAmount(Number(wallet.balance || 0)) }} 两</el-descriptions-item>
              <el-descriptions-item label="冻结">{{ formatAmount(Number(wallet.frozen || 0)) }} 两</el-descriptions-item>
              <el-descriptions-item label="币种">{{ wallet.currency || '两' }}</el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>
      </el-row>

      <el-card shadow="never" class="block">
        <h3>运营备注</h3>
        <el-input v-model="remarkDraft" type="textarea" :rows="3" maxlength="500" show-word-limit />
        <el-button type="primary" style="margin-top: 8px" @click="saveRemark">保存备注</el-button>
      </el-card>

      <el-card shadow="never" class="block">
        <h3>实名信息</h3>
        <el-empty v-if="!realName" description="无权限或暂无实名数据" />
        <template v-else>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="姓名">{{ realName.realName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="证件号">{{ realName.idNumber || '—' }}</el-descriptions-item>
            <el-descriptions-item label="状态">{{ realName.status || 'NONE' }}</el-descriptions-item>
          </el-descriptions>
          <div class="ops" style="margin-top: 8px">
            <el-button size="small" type="success" @click="setRealNameStatus('VERIFIED')">核验通过</el-button>
            <el-button size="small" type="warning" @click="setRealNameStatus('REJECTED')">驳回</el-button>
            <el-button size="small" @click="setRealNameStatus('PENDING')">待审</el-button>
          </div>
        </template>
      </el-card>

      <el-card shadow="never" class="block">
        <h3>登录日志</h3>
        <el-table :data="logs" size="small" empty-text="暂无日志">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="ip" label="IP" width="140" />
          <el-table-column prop="result" label="结果" width="100" />
          <el-table-column prop="userAgent" label="UA" min-width="180" show-overflow-tooltip />
          <el-table-column prop="createdAt" label="时间" width="180" />
        </el-table>
        <el-pagination
          v-model:current-page="logQuery.page"
          :page-size="logQuery.pageSize"
          :total="logTotal"
          layout="total, prev, pager, next"
          style="margin-top: 8px"
          @current-change="loadLogs"
        />
      </el-card>
    </template>
  </div>
</template>

<style scoped>
.page {
  max-width: 1000px;
}
.toolbar {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 12px;
}
.ops {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.block {
  margin-bottom: 12px;
}
h2 {
  margin: 0 0 12px;
  font-size: 20px;
}
h3 {
  margin: 0 0 10px;
  font-size: 15px;
}
</style>
