<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createInvite, listMyInvites } from '@/api/user'

const code = ref('')
const link = ref('')
const remain = ref(0)
const records = ref<Record<string, unknown>[]>([])

async function load() {
  const data = await listMyInvites({ page: 1, pageSize: 50 })
  records.value = data.list || []
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
      <h1 class="brand-title">邀请同道</h1>
      <p class="jh-muted">受每日额度限制 · 被邀请人凭码注册</p>
      <div class="jh-panel block">
        <el-button type="primary" class="jh-btn-seal" @click="onCreate">生成邀请码</el-button>
        <p v-if="code">邀请码：<strong>{{ code }}</strong> · 今日剩余 {{ remain }}</p>
        <p v-if="link">链接：{{ link }}</p>
        <el-button v-if="code || link" @click="copy">复制</el-button>
      </div>
      <el-table :data="records" class="jh-panel">
        <el-table-column prop="code" label="邀请码" />
        <el-table-column prop="inviteeNickname" label="被邀请人" />
        <el-table-column prop="createdAt" label="时间" />
      </el-table>
    </div>
  </section>
</template>

<style scoped>
.narrow {
  max-width: 720px;
}
h1 {
  margin: 0 0 6px;
  font-size: 32px;
}
.block {
  padding: 16px;
  margin: 14px 0;
}
</style>
