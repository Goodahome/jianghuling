<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cancelBounty, claimBounty, getBounty, listEvaluations } from '@/api/bounty'
import { getWarrantTemplates } from '@/api/meta'
import { useAuthStore } from '@/stores/auth'
import type { BountyDetail, WarrantFieldDef, WarrantTemplate } from '@/types/models'
import {
  bountyTypeLabel,
  difficultyLabel,
  formatAmount,
  formatWarrantValue,
  isWarrantValueEmpty,
  warrantFieldFallbackLabel,
} from '@/utils/labels'
import StatusTag from '@/components/StatusTag.vue'
import PageBreadcrumb from '@/components/PageBreadcrumb.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const claiming = ref(false)
const detail = ref<BountyDetail | null>(null)
const warrantTemplates = ref<WarrantTemplate[]>([])
const evaluations = ref<Record<string, unknown>[]>([])

const crumbs = computed(() => {
  const title = detail.value?.title?.trim()
  const short = title && title.length > 12 ? `${title.slice(0, 12)}…` : title || '悬赏详情'
  return [
    { label: '悬赏广场', to: '/plaza' },
    { label: short },
  ]
})

const canClaim = computed(() => {
  if (!detail.value || !auth.isLoggedIn) return false
  if (detail.value.isPublisher || detail.value.claimedByMe) return false
  return ['OPEN', 'IN_COLLAB'].includes(detail.value.status)
})

const canDispute = computed(() => {
  if (!detail.value || !auth.isLoggedIn) return false
  if (!(detail.value.isPublisher || detail.value.claimedByMe)) return false
  return detail.value.status === 'COMPLETED' || detail.value.status === 'IN_DISPUTE'
})

const warrantFieldDefs = computed((): WarrantFieldDef[] => {
  const type = detail.value?.type
  if (!type) return []
  const tpl = warrantTemplates.value.find((t) => t.type === type || (t as { code?: string }).code === type)
  return tpl?.fields || []
})

/** 按模板顺序展示中文标签；空可选字段不展示 */
const warrantRows = computed(() => {
  const fields = detail.value?.warrantFields || {}
  const defs = warrantFieldDefs.value
  const rows: { key: string; label: string; text: string }[] = []
  const seen = new Set<string>()

  for (const def of defs) {
    seen.add(def.key)
    const val = fields[def.key]
    if (isWarrantValueEmpty(val) && !def.required) continue
    rows.push({
      key: def.key,
      label: def.label || warrantFieldFallbackLabel[def.key] || def.key,
      text: formatWarrantValue(val) || '—',
    })
  }

  // 模板外残留字段（兼容旧数据）
  for (const [key, val] of Object.entries(fields)) {
    if (seen.has(key) || isWarrantValueEmpty(val)) continue
    rows.push({
      key,
      label: warrantFieldFallbackLabel[key] || key,
      text: formatWarrantValue(val),
    })
  }
  return rows
})

async function load() {
  loading.value = true
  try {
    const [bounty, templates] = await Promise.all([
      getBounty(route.params.id as string),
      getWarrantTemplates().catch(() => [] as WarrantTemplate[]),
    ])
    detail.value = bounty
    warrantTemplates.value = templates
    if (bounty.status === 'COMPLETED' || bounty.status === 'IN_DISPUTE') {
      const evals = await listEvaluations(bounty.id).catch(() => null)
      evaluations.value = Array.isArray(evals)
        ? evals
        : ((evals as { list?: Record<string, unknown>[] } | null)?.list || [])
    } else {
      evaluations.value = []
    }
  } finally {
    loading.value = false
  }
}

async function onClaim() {
  if (!auth.isLoggedIn) {
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }
  await ElMessageBox.confirm('揭榜将消耗体力，且同令仅可揭榜一次。确认揭榜？', '一键揭榜')
  claiming.value = true
  try {
    await claimBounty(route.params.id as string)
    ElMessage.success('揭榜成功，已加入协作')
    await auth.fetchMe()
    await load()
  } finally {
    claiming.value = false
  }
}

async function onCancel() {
  const { value } = await ElMessageBox.prompt('请填写取消原因', '取消悬赏')
  await cancelBounty(route.params.id as string, value || '取消')
  ElMessage.success('已取消，托管赏银将退回')
  await load()
}

onMounted(load)
</script>

<template>
  <section class="jh-section" v-loading="loading">
    <div class="jh-container" v-if="detail">
      <PageBreadcrumb :items="crumbs" />
      <div class="head jh-panel">
        <div class="tags">
          <div class="tags-left">
            <span class="type">{{ bountyTypeLabel[detail.type] }}</span>
            <StatusTag :status="detail.status" />
          </div>
          <span class="difficulty">{{ difficultyLabel[detail.difficulty] }}</span>
        </div>
        <h1>{{ detail.title }}</h1>
        <p class="reward">赏银 {{ formatAmount(detail.rewardAmount) }} 两 · 揭榜 {{ detail.claimCount }} 人</p>
        <p class="jh-muted">
          {{ detail.district || '遵义' }} · 截止 {{ detail.deadlineAt }}
        </p>
        <div class="actions">
          <el-button v-if="canClaim" type="primary" class="jh-btn-seal action-main" :loading="claiming" @click="onClaim">
            一键揭榜
          </el-button>
          <el-button
            v-if="detail.claimedByMe || detail.isPublisher"
            class="action-item"
            @click="router.push(`/bounties/${detail.id}/chat`)"
          >
            协作会话
          </el-button>
          <el-button
            v-if="detail.claimedByMe"
            type="success"
            class="action-item"
            @click="router.push(`/bounties/${detail.id}/submit`)"
          >
            提交成果
          </el-button>
          <el-button
            v-if="detail.isPublisher && ['IN_COLLAB', 'PENDING_SETTLE'].includes(detail.status)"
            type="warning"
            class="action-item"
            @click="router.push(`/bounties/${detail.id}/settle`)"
          >
            完结分配
          </el-button>
          <el-button
            v-if="detail.isPublisher && ['OPEN', 'PENDING_REVIEW', 'IN_COLLAB'].includes(detail.status)"
            class="action-item"
            @click="onCancel"
          >
            取消悬赏
          </el-button>
          <el-button
            v-if="detail.canRepublish"
            type="primary"
            class="jh-btn-seal action-item"
            @click="router.push({ path: '/bounties/publish', query: { republishFrom: String(detail.id) } })"
          >
            再发一令
          </el-button>
          <el-button
            v-if="canDispute && detail.status === 'COMPLETED'"
            class="action-item"
            type="danger"
            @click="router.push({ path: '/disputes', query: { bountyId: String(detail.id) } })"
          >
            发起纠纷
          </el-button>
          <el-button
            v-if="detail.status === 'IN_DISPUTE'"
            class="action-item"
            @click="router.push('/disputes')"
          >
            查看纠纷
          </el-button>
        </div>
      </div>

      <div class="cols">
        <div class="jh-panel block">
          <h2>租房令状</h2>
          <el-empty v-if="!warrantRows.length" description="暂无令状信息" />
          <el-descriptions v-else :column="1" border>
            <el-descriptions-item
              v-for="row in warrantRows"
              :key="row.key"
              :label="row.label"
            >
              {{ row.text }}
            </el-descriptions-item>
          </el-descriptions>
          <div v-if="detail.taskTags?.length" class="tags-row">
            <el-tag v-for="t in detail.taskTags" :key="t" size="small">{{ t }}</el-tag>
          </div>
        </div>
        <div class="jh-panel block">
          <h2>探子清单</h2>
          <el-empty v-if="!detail.checklist?.length" description="暂无清单项" />
          <ul v-else class="checklist">
            <li v-for="item in detail.checklist" :key="item.itemCode">
              <span>{{ item.itemName }}</span>
              <el-tag v-if="item.required" size="small" type="danger">必验</el-tag>
            </li>
          </ul>
        </div>
      </div>

      <div v-if="evaluations.length" class="jh-panel block evals">
        <h2>互评</h2>
        <div v-for="(e, idx) in evaluations" :key="idx" class="eval-item">
          <strong>{{ e.fromNickname || e.fromUserId }} → {{ e.toNickname || e.toUserId }}</strong>
          <span class="jh-muted"> · {{ e.score }} 分</span>
          <p>{{ e.content || '（无文字）' }}</p>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.head {
  padding: 22px;
  margin-bottom: 16px;
}
.tags {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}
.tags-left {
  display: flex;
  gap: 8px;
  align-items: center;
  color: var(--jh-seal);
}
.type {
  font-size: 13px;
}
.difficulty {
  flex-shrink: 0;
  font-size: 13px;
  color: var(--jh-muted);
}
h1 {
  margin: 0 0 8px;
  font-size: clamp(22px, 4vw, 28px);
  font-family: var(--jh-font-doc);
  color: var(--jh-ink);
  text-shadow: none;
  font-weight: 600;
}
.reward {
  font-size: 18px;
  color: var(--jh-seal);
  margin: 0 0 4px;
}
.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 16px;
}
.cols {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: 16px;
}
.block {
  padding: 18px;
}
.evals {
  margin-top: 16px;
}
.eval-item {
  padding: 10px 0;
  border-bottom: 1px solid var(--jh-line);
}
.eval-item:last-child {
  border-bottom: none;
}
h2 {
  margin: 0 0 12px;
  font-size: 18px;
  font-family: var(--jh-font-display);
}
.checklist {
  list-style: none;
  padding: 0;
  margin: 0;
}
.checklist li {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  padding: 10px 0;
  border-bottom: 1px solid var(--jh-line);
}
.tags-row {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 12px;
}
@media (max-width: 768px) {
  .head {
    padding: 16px;
  }
  .tags {
    flex-wrap: wrap;
  }
  .cols {
    grid-template-columns: 1fr;
  }
  .block {
    padding: 14px;
  }
  .actions {
    position: sticky;
    bottom: calc(56px + env(safe-area-inset-bottom));
    z-index: 10;
    margin: 12px -4px 0;
    padding: 10px;
    background: #fff;
    border: 1px solid var(--jh-line);
    border-radius: var(--jh-radius);
  }
  .action-main,
  .action-item {
    flex: 1 1 calc(50% - 8px);
    margin: 0 !important;
  }
  .action-main {
    flex-basis: 100%;
  }
}
</style>
