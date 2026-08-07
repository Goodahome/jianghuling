<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getBounty, listClaimSubmissions, submitResult } from '@/api/bounty'
import type { BountyDetail, Submission } from '@/types/models'
import ImageUpload from '@/components/ImageUpload.vue'
import PageBreadcrumb from '@/components/PageBreadcrumb.vue'

const route = useRoute()
const router = useRouter()
const bountyId = String(route.params.id || '')
const crumbs = [
  { label: '悬赏榜', to: '/plaza' },
  { label: '悬赏详情', to: `/bounties/${bountyId}` },
  { label: '提交成果' },
]
const detail = ref<BountyDetail | null>(null)
const history = ref<Submission[]>([])
const loading = ref(false)
const form = reactive({
  summary: '',
  items: [] as {
    itemCode: string
    itemName: string
    required: boolean
    done: boolean
    text: string
    mediaUrls: string[]
  }[],
})

onMounted(async () => {
  detail.value = await getBounty(route.params.id as string)
  form.items = (detail.value.checklist || []).map((c) => ({
    itemCode: c.itemCode,
    itemName: c.itemName,
    required: !!c.required,
    done: false,
    text: '',
    mediaUrls: [],
  }))
  if (detail.value.claimId) {
    history.value = await listClaimSubmissions(detail.value.id, detail.value.claimId)
  }
})

async function onSubmit() {
  if (!form.summary.trim()) {
    ElMessage.warning('请填写成果摘要')
    return
  }
  const missing = form.items.filter((i) => i.required && !i.done).map((i) => i.itemName || i.itemCode)
  if (missing.length) {
    ElMessage.warning(`以下必验项尚未勾选「已完成」：${missing.join('、')}`)
    return
  }
  loading.value = true
  try {
    await submitResult(route.params.id as string, {
      summary: form.summary,
      items: form.items.map(({ itemCode, done, text, mediaUrls }) => ({
        itemCode,
        done,
        text,
        mediaUrls,
      })),
    })
    ElMessage.success('成果已提交，等待验功')
    router.replace(`/bounties/${route.params.id}`)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <section class="jh-section">
    <div class="jh-container narrow">
      <PageBreadcrumb :items="crumbs" />
      <h1 class="brand-title">提交成果</h1>
      <p class="jh-muted">按探子清单逐项填写；可多次提交，受冷却与日限约束</p>
      <el-form class="jh-panel form" label-position="top" @submit.prevent="onSubmit">
        <el-form-item label="摘要" required>
          <el-input v-model="form.summary" type="textarea" :rows="3" />
        </el-form-item>
        <div v-for="item in form.items" :key="item.itemCode" class="item">
          <div class="row">
            <strong>
              {{ item.itemName }}
              <span v-if="item.required" class="req">必验</span>
            </strong>
            <el-switch v-model="item.done" active-text="已完成" />
          </div>
          <el-input v-model="item.text" type="textarea" :rows="2" placeholder="说明 / 带看记录等" />
          <div class="media">
            <ImageUpload v-model="item.mediaUrls" :limit="3" tip="该项现场图 / 凭证" />
          </div>
        </div>
        <el-button type="primary" class="jh-btn-seal" native-type="submit" :loading="loading">
          提交本版成果
        </el-button>
      </el-form>

      <div v-if="history.length" class="jh-panel hist">
        <h2>历史版本</h2>
        <el-timeline>
          <el-timeline-item v-for="h in history" :key="h.id" :timestamp="h.createdAt">
            v{{ h.versionNo }} · {{ h.status }} · {{ h.contentSummary }}
          </el-timeline-item>
        </el-timeline>
      </div>
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
.form,
.hist {
  padding: 18px;
  margin-top: 14px;
}
.item {
  margin-bottom: 14px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--jh-line);
}
.row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}
.req {
  margin-left: 6px;
  font-size: 12px;
  font-weight: 500;
  color: var(--jh-seal);
}
.media {
  margin-top: 8px;
}
</style>
