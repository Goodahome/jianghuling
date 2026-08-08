<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createDispute, listMyDisputes } from '@/api/dispute'
import type { Dispute } from '@/types/models'
import EmptyState from '@/components/EmptyState.vue'
import ImageUpload from '@/components/ImageUpload.vue'
import JhPageHeader from '@/components/JhPageHeader.vue'

const route = useRoute()
const router = useRouter()
const list = ref<Dispute[]>([])
const form = reactive({
  bountyId: '',
  reason: '',
  evidenceText: '',
  evidenceUrls: [] as string[],
})

async function load() {
  const data = await listMyDisputes({ page: 1, pageSize: 50 })
  list.value = data.list || []
}

async function onCreate() {
  if (!form.bountyId) return ElMessage.warning('请填写悬赏令 ID')
  if (!form.reason.trim()) return ElMessage.warning('请填写纠纷原因')
  await createDispute(form.bountyId, {
    reason: form.reason,
    evidenceText: form.evidenceText,
    evidenceUrls: form.evidenceUrls,
  })
  ElMessage.success('纠纷已发起（结算后 7 日内）')
  form.reason = ''
  form.evidenceText = ''
  form.evidenceUrls = []
  await load()
  router.push(`/disputes`)
}

onMounted(() => {
  const q = String(route.query.bountyId || '')
  if (q) form.bountyId = q
  load()
})

watch(
  () => route.query.bountyId,
  (v) => {
    if (v) form.bountyId = String(v)
  },
)
</script>

<template>
  <section class="jh-section">
    <div class="jh-container narrow">
      <JhPageHeader title="我的纠纷" />
      <el-form class="jh-panel block" label-position="top" @submit.prevent="onCreate">
        <h2>发起纠纷</h2>
        <el-form-item label="悬赏令 ID" required>
          <el-input v-model="form.bountyId" />
        </el-form-item>
        <el-form-item label="原因" required>
          <el-input v-model="form.reason" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="举证说明">
          <el-input v-model="form.evidenceText" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="举证图片">
          <ImageUpload v-model="form.evidenceUrls" :limit="5" tip="上传聊天截图、凭证等" />
        </el-form-item>
        <el-button type="primary" class="jh-btn-seal" native-type="submit">提交</el-button>
      </el-form>
      <EmptyState v-if="!list.length" title="暂无纠纷" />
      <RouterLink
        v-for="d in list"
        :key="d.id"
        :to="`/disputes/${d.id}`"
        class="jh-panel item"
      >
        <strong>#{{ d.id }} · 悬赏 {{ d.bountyId }}</strong>
        <p>{{ d.reason }}</p>
        <p class="jh-muted">{{ d.status }} · {{ d.createdAt }}</p>
      </RouterLink>
    </div>
  </section>
</template>

<style scoped>
h1 {
  margin: 0 0 12px;
  font-size: 32px;
}
.block,
.item {
  display: block;
  padding: 16px;
  margin-bottom: 12px;
}
</style>
