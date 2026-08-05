<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createDispute, listMyDisputes } from '@/api/dispute'
import type { Dispute } from '@/types/models'
import EmptyState from '@/components/EmptyState.vue'

const list = ref<Dispute[]>([])
const form = reactive({ bountyId: '', reason: '', evidenceText: '' })

async function load() {
  const data = await listMyDisputes({ page: 1, pageSize: 50 })
  list.value = data.list || []
}

async function onCreate() {
  await createDispute(form.bountyId, {
    reason: form.reason,
    evidenceText: form.evidenceText,
  })
  ElMessage.success('纠纷已发起（结算后 7 日内）')
  form.bountyId = ''
  form.reason = ''
  form.evidenceText = ''
  await load()
}

onMounted(load)
</script>

<template>
  <section class="jh-section">
    <div class="jh-container narrow">
      <h1 class="brand-title">我的纠纷</h1>
      <el-form class="jh-panel block" label-position="top" @submit.prevent="onCreate">
        <h2>发起纠纷</h2>
        <el-form-item label="悬赏令 ID">
          <el-input v-model="form.bountyId" />
        </el-form-item>
        <el-form-item label="原因">
          <el-input v-model="form.reason" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="举证说明">
          <el-input v-model="form.evidenceText" type="textarea" :rows="2" />
        </el-form-item>
        <el-button type="primary" class="jh-btn-seal" native-type="submit">提交</el-button>
      </el-form>
      <EmptyState v-if="!list.length" title="暂无纠纷" />
      <div v-for="d in list" :key="d.id" class="jh-panel item">
        <strong>#{{ d.id }} · 悬赏 {{ d.bountyId }}</strong>
        <p>{{ d.reason }}</p>
        <p class="jh-muted">{{ d.status }} · {{ d.createdAt }}</p>
      </div>
    </div>
  </section>
</template>

<style scoped>
.narrow {
  max-width: 720px;
}
h1 {
  margin: 0 0 12px;
  font-size: 32px;
}
.block,
.item {
  padding: 16px;
  margin-bottom: 12px;
}
</style>
