<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createInvite, listMyInvites } from '@/api/user'
import JhPageHeader from '@/components/JhPageHeader.vue'

const code = ref('')
const link = ref('')
const remain = ref(0)
const records = ref<Record<string, unknown>[]>([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const data = await listMyInvites({ page: 1, pageSize: 50 })
    records.value = data.list || []
  } finally {
    loading.value = false
  }
}

async function onCreate() {
  const res = await createInvite()
  code.value = res.code
  link.value = res.link
  remain.value = res.remainQuotaToday
  ElMessage.success('邀请码已生成')
  await load()
}

async function copy() {
  await navigator.clipboard.writeText(link.value || code.value)
  ElMessage.success('已复制')
}

onMounted(load)
</script>

<template>
  <section class="jh-section">
    <div class="jh-container narrow">
      <JhPageHeader title="邀请同道" />
      <div class="jh-panel block">
        <el-button type="primary" class="jh-btn-seal" :loading="loading" @click="onCreate">
          生成邀请码
        </el-button>
        <p v-if="code">邀请码：<strong>{{ code }}</strong> · 今日剩余 {{ remain }}</p>
        <p v-if="link">链接：{{ link }}</p>
        <el-button v-if="code || link" @click="copy">复制</el-button>
      </div>
      <el-table v-loading="loading" :data="records" class="jh-panel">
        <el-table-column prop="code" label="邀请码" min-width="100" />
        <el-table-column prop="status" label="状态" width="90" />
        <el-table-column prop="usedCount" label="已用" width="70" />
        <el-table-column prop="quota" label="配额" width="70" />
        <el-table-column prop="inviteeId" label="被邀请人ID" width="110">
          <template #default="{ row }">
            {{ row.inviteeId ?? '—' }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="时间" min-width="160" />
      </el-table>
    </div>
  </section>
</template>

<style scoped>
h1 {
  margin: 0 0 6px;
  font-size: 32px;
}
.block {
  padding: 16px;
  margin: 14px 0;
}
</style>
