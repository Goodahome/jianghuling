<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { previewSettlement, submitEvaluation, submitSettlement } from '@/api/bounty'
import type { SettlementPreview } from '@/types/models'
import { formatAmount } from '@/utils/labels'
import PageBreadcrumb from '@/components/PageBreadcrumb.vue'

const route = useRoute()
const router = useRouter()
const bountyId = String(route.params.id || '')
const crumbs = [
  { label: '悬赏榜', to: '/plaza' },
  { label: '悬赏详情', to: `/bounties/${bountyId}` },
  { label: '完结分配' },
]
const preview = ref<SettlementPreview | null>(null)
const amounts = ref<Record<number, number>>({})
const chivalry = ref<Record<number, number>>({})
const loading = ref(false)
const evalForm = ref({ toUserId: 0, score: 5, content: '' })

const allocated = computed(() =>
  Object.values(amounts.value).reduce((s, n) => s + (Number(n) || 0), 0),
)
const remain = computed(() => (preview.value?.distributable || 0) - allocated.value)

onMounted(async () => {
  preview.value = await previewSettlement(route.params.id as string)
  preview.value.claimants.forEach((c) => {
    amounts.value[c.userId] = 0
    chivalry.value[c.userId] = 0
  })
})

async function onSettle() {
  if (!preview.value) return
  if (Math.abs(remain.value) > 0.001) {
    return ElMessage.error(`须分完可分配池，当前剩余 ${remain.value} 两`)
  }
  loading.value = true
  try {
    await submitSettlement(
      route.params.id as string,
      preview.value.claimants.map((c) => ({
        userId: c.userId,
        amount: Number(amounts.value[c.userId] || 0),
        chivalryBonus: Number(chivalry.value[c.userId] || 0),
      })),
    )
    ElMessage.success('结算成功，赏银已入账')
    router.replace(`/bounties/${route.params.id}`)
  } finally {
    loading.value = false
  }
}

async function onEval() {
  await submitEvaluation(route.params.id as string, evalForm.value)
  ElMessage.success('评价已提交')
  router.replace(`/bounties/${route.params.id}`)
}
</script>

<template>
  <section class="jh-section">
    <div class="jh-container narrow" v-if="preview">
      <PageBreadcrumb :items="crumbs" />
      <h1 class="brand-title">完结分配</h1>
      <div class="jh-panel block">
        <p>托管赏银 {{ formatAmount(preview.rewardB) }} 两</p>
        <p>服务费 {{ (preview.feeRate * 100).toFixed(0) }}% = {{ formatAmount(preview.fee) }} 两</p>
        <p>
          可分配池 <strong>{{ formatAmount(preview.distributable) }}</strong> 两 · 已分
          {{ formatAmount(allocated) }} · 剩余 {{ formatAmount(remain) }}
        </p>
      </div>
      <div class="jh-panel block">
        <div v-for="c in preview.claimants" :key="c.userId" class="row">
          <div>
            <strong>{{ c.nickname }}</strong>
            <div class="jh-muted">通过成果 {{ c.approvedSubmissionCount }} 条</div>
          </div>
          <el-input-number v-model="amounts[c.userId]" :min="0" :step="10" />
          <el-input-number v-model="chivalry[c.userId]" :min="0" :step="1" placeholder="侠义奖励" />
        </div>
        <el-button type="primary" class="jh-btn-seal" :loading="loading" @click="onSettle">
          确认全额分配并结算
        </el-button>
      </div>
      <div class="jh-panel block">
        <h2>互评（结算后）</h2>
        <el-select v-model="evalForm.toUserId" placeholder="评价对象" style="width: 100%; margin-bottom: 8px">
          <el-option
            v-for="c in preview.claimants"
            :key="c.userId"
            :label="c.nickname"
            :value="c.userId"
          />
        </el-select>
        <el-rate v-model="evalForm.score" />
        <el-input v-model="evalForm.content" type="textarea" :rows="2" style="margin: 8px 0" />
        <el-button @click="onEval">提交评价</el-button>
      </div>
    </div>
  </section>
</template>

<style scoped>
.narrow {
  max-width: 760px;
}
h1 {
  margin: 0 0 12px;
  font-size: 32px;
}
.block {
  padding: 16px;
  margin-bottom: 12px;
}
.row {
  display: grid;
  grid-template-columns: 1fr 140px 140px;
  gap: 10px;
  align-items: center;
  margin-bottom: 12px;
}
@media (max-width: 640px) {
  h1 {
    font-size: 28px;
  }
  .block {
    padding: 12px;
  }
  .row {
    grid-template-columns: 1fr;
  }
  .block .el-button {
    width: 100%;
  }
}
</style>
