<script setup lang="ts">
import { computed, nextTick, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  cancelBounty,
  claimBounty,
  getBounty,
  listBountySubmissions,
  listEvaluations,
  quitClaim,
} from '@/api/bounty'
import { getWarrantTemplates } from '@/api/meta'
import { useAuthStore } from '@/stores/auth'
import { useMineAttentionStore } from '@/stores/mineAttention'
import type {
  BountyCapabilities,
  BountyDetail,
  BountySubmissionListItem,
  WarrantFieldDef,
  WarrantTemplate,
} from '@/types/models'
import {
  difficultyLabel,
  formatAmount,
  formatWarrantValue,
  isWarrantValueEmpty,
  resolveBountyTypeLabel,
  warrantFieldFallbackLabel,
} from '@/utils/labels'
import StatusTag from '@/components/StatusTag.vue'
import PageBreadcrumb from '@/components/PageBreadcrumb.vue'

const EMPTY_CAPS: BountyCapabilities = {
  canCancel: false,
  canSendMessage: false,
  canReadMessages: false,
  canViewSubmissions: false,
  canSubmit: false,
  canSettle: false,
  canQuitClaim: false,
  canRepublish: false,
  canDispute: false,
}

const submissionStatusLabel: Record<BountySubmissionListItem['status'], string> = {
  PENDING: '待审核',
  APPROVED: '已通过',
  REJECTED: '已驳回',
}

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const mineAttention = useMineAttentionStore()
const loading = ref(false)
const claiming = ref(false)
const quitting = ref(false)
const detail = ref<BountyDetail | null>(null)
const warrantTemplates = ref<WarrantTemplate[]>([])
const evaluations = ref<Record<string, unknown>[]>([])
const submissions = ref<BountySubmissionListItem[]>([])
const submissionsLoading = ref(false)
const submissionsPanel = ref<HTMLElement | null>(null)
const showSubmissions = ref(false)

const crumbs = computed(() => {
  const from = String(route.query.from || '')
  if (from === 'mine') {
    return [
      { label: '我的悬赏', to: '/mine' },
      { label: '悬赏详情' },
    ]
  }
  return [
    { label: '悬赏榜', to: '/plaza' },
    { label: '悬赏详情' },
  ]
})

/** 详情已加载但无 capabilities（D-V1810-03）：勿静默藏光全部入口 */
const capsMissing = computed(
  () => !!detail.value && detail.value.capabilities == null,
)

const caps = computed((): BountyCapabilities => {
  const c = detail.value?.capabilities
  if (!c) return EMPTY_CAPS
  return { ...EMPTY_CAPS, ...c }
})

/** 再发一令：仅认 capabilities（顶层 canRepublish 由后端镜像，不作旁路） */
const canRepublish = computed(() => caps.value.canRepublish)

/**
 * 一键揭榜：契约无 canClaim，仍用身份+状态（api.md 揭榜前置）。
 * 其余操作入口一律只认 capabilities，勿用 IN_COLLAB 等状态旁路藏/显按钮。
 */
const canClaim = computed(() => {
  if (!detail.value || !auth.isLoggedIn) return false
  if (detail.value.isPublisher || detail.value.claimedByMe) return false
  return ['OPEN', 'IN_COLLAB'].includes(detail.value.status)
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

async function loadSubmissions() {
  if (!detail.value || !caps.value.canViewSubmissions) {
    submissions.value = []
    return
  }
  submissionsLoading.value = true
  try {
    const data = await listBountySubmissions(detail.value.id, { page: 1, pageSize: 50 })
    submissions.value = data?.list || []
  } catch {
    submissions.value = []
  } finally {
    submissionsLoading.value = false
  }
}

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
    const viewSub = bounty.capabilities?.canViewSubmissions
    if (viewSub && showSubmissions.value) {
      await loadSubmissions()
    } else if (!viewSub) {
      showSubmissions.value = false
      submissions.value = []
    }
    // 打开详情：消掉「新成果 / 状态变更」水位；会话未读仍须进协作会话
    if (bounty?.id) {
      const subCount =
        submissions.value.length ||
        Number(
          (mineAttention.published.find((x) => x.id === bounty.id) ||
            mineAttention.claimed.find((x) => x.id === bounty.id))?.submissionCount ?? 0,
        )
      mineAttention.markSeenDetail(bounty.id, bounty.status, subCount)
      void mineAttention.refresh()
    }
  } finally {
    loading.value = false
  }
}

async function openSubmissions() {
  showSubmissions.value = true
  await loadSubmissions()
  await nextTick()
  submissionsPanel.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
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
  if (!detail.value) return
  const hasSubs = !!detail.value.hasSubmissions
  if (hasSubs) {
    await ElMessageBox.confirm(
      '本令已有成果提交。取消后不可全额退回，须进入分配流程将托管赏银分给有成果的揭榜侠。确认取消并进入分配？',
      '取消悬赏（须分配）',
      {
        type: 'warning',
        confirmButtonText: '确认取消并分配',
        cancelButtonText: '再想想',
      },
    )
  } else {
    await ElMessageBox.confirm(
      '确认取消悬赏？无成果时托管赏银将全额退回。',
      '取消悬赏',
      { type: 'warning', confirmButtonText: '确认取消' },
    )
  }

  let reason = '令主取消'
  if (['IN_COLLAB', 'PENDING_SETTLE'].includes(detail.value.status)) {
    const { value } = await ElMessageBox.prompt('请填写取消原因', '取消悬赏', {
      inputPlaceholder: '必填',
      inputValidator: (v) => (!!(v || '').trim() ? true : '请填写取消原因'),
    })
    reason = (value || '').trim()
  }

  try {
    const result = await cancelBounty(detail.value.id, reason)
    const allocate =
      result?.cancelOutcome === 'ALLOCATE' ||
      result?.settlementRequired === true ||
      (result?.hasSubmissions === true && result?.cancelAllocationPending === true)

    if (allocate) {
      ElMessage.success('已取消进入待分配，请完成成果分配')
      router.push({
        path: `/bounties/${detail.value.id}/settle`,
        query: { settlementKind: 'CANCEL_ALLOCATE' },
      })
      return
    }

    ElMessage.success('已取消，托管赏银已全额退回')
    await load()
  } catch {
    /* 43010/43011 等由 request 层友好提示 */
  }
}

function goSubmissionDetail(s: BountySubmissionListItem) {
  const sid = s.submissionId
  if (!sid) {
    ElMessage.warning('成果编号缺失，请刷新后重试')
    return
  }
  router.push(`/bounties/${detail.value?.id}/submissions/${sid}`)
}

async function onQuitClaim() {
  await ElMessageBox.confirm(
    '退出后不可再揭本令，已提交成果保留审计；体力返还，当日揭榜次数不返还。确认退出？',
    '退出揭榜',
    { type: 'warning' },
  )
  quitting.value = true
  try {
    await quitClaim(route.params.id as string)
    ElMessage.success('已退出揭榜')
    await auth.fetchMe()
    await load()
  } finally {
    quitting.value = false
  }
}

onMounted(load)
</script>

<template>
  <section class="jh-section" v-loading="loading">
    <div class="jh-container narrow" v-if="detail">
      <PageBreadcrumb :items="crumbs" />
      <div class="head jh-panel">
        <div class="tags">
          <div class="tags-left">
            <span class="type">{{ resolveBountyTypeLabel(detail.type, detail.typeDisplayName) }}</span>
            <StatusTag :status="detail.status" />
          </div>
          <span class="difficulty">{{ difficultyLabel[detail.difficulty] }}</span>
        </div>
        <h1>{{ detail.title }}</h1>
        <p class="reward">赏银 {{ formatAmount(detail.rewardAmount) }} 两 · 揭榜 {{ detail.claimCount }} 人</p>
        <p class="jh-muted">
          {{ detail.district || '遵义' }} · 截止 {{ detail.deadlineAt }}
        </p>
        <el-alert
          v-if="capsMissing"
          class="caps-alert"
          type="warning"
          show-icon
          :closable="false"
          title="能力状态加载失败"
        >
          <p class="caps-alert-desc">
            未能取得本单操作权限，入口已暂时隐藏。请刷新重试。
          </p>
          <el-button size="small" type="primary" plain :loading="loading" @click="load">
            刷新
          </el-button>
        </el-alert>
        <div class="actions">
          <el-button v-if="canClaim" type="primary" class="jh-btn-seal action-main" :loading="claiming" @click="onClaim">
            一键揭榜
          </el-button>
          <el-button
            v-if="caps.canReadMessages || caps.canSendMessage"
            class="action-item"
            @click="router.push({ path: `/bounties/${detail.id}/chat`, query: { from: route.query.from } })"
          >
            协作会话
          </el-button>
          <el-button
            v-if="caps.canViewSubmissions"
            class="action-item"
            @click="openSubmissions"
          >
            成果查看
          </el-button>
          <el-button
            v-if="caps.canSubmit"
            type="success"
            class="action-item"
            @click="router.push(`/bounties/${detail.id}/submit`)"
          >
            提交成果
          </el-button>
          <el-button
            v-if="caps.canSettle"
            type="warning"
            class="action-item"
            @click="
              router.push({
                path: `/bounties/${detail.id}/settle`,
                query: detail.cancelAllocationPending
                  ? { settlementKind: 'CANCEL_ALLOCATE' }
                  : undefined,
              })
            "
          >
            {{ detail.cancelAllocationPending ? '取消后分配' : '完结分配' }}
          </el-button>
          <el-button
            v-if="caps.canCancel"
            class="action-item"
            @click="onCancel"
          >
            取消悬赏
          </el-button>
          <el-button
            v-if="caps.canQuitClaim"
            class="action-item"
            type="danger"
            plain
            :loading="quitting"
            @click="onQuitClaim"
          >
            退出揭榜
          </el-button>
          <el-button
            v-if="canRepublish"
            type="primary"
            class="jh-btn-seal action-item"
            @click="router.push({ path: '/bounties/publish', query: { republishFrom: String(detail.id) } })"
          >
            再发一令
          </el-button>
          <el-button
            v-if="caps.canDispute && detail.status !== 'IN_DISPUTE'"
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

      <div
        v-if="showSubmissions && caps.canViewSubmissions"
        ref="submissionsPanel"
        class="jh-panel block submissions"
        v-loading="submissionsLoading"
      >
        <h2>成果查看</h2>
        <el-empty v-if="!submissionsLoading && !submissions.length" description="暂无成果提交" />
        <ul v-else class="sub-list">
          <li
            v-for="s in submissions"
            :key="s.submissionId"
            class="sub-item sub-clickable"
            role="button"
            tabindex="0"
            @click="goSubmissionDetail(s)"
            @keydown.enter="goSubmissionDetail(s)"
          >
            <div class="sub-head">
              <strong>{{ s.claimerNickname || (s.claimerUserId ? `侠士#${s.claimerUserId}` : '佚名侠士') }}</strong>
              <span class="jh-muted"> · v{{ s.versionNo }}</span>
              <el-tag size="small" class="sub-status">
                {{ submissionStatusLabel[s.status] || s.status }}
              </el-tag>
            </div>
            <p class="sub-summary">{{ s.summary || '（无摘要）' }}</p>
            <p class="jh-muted sub-meta">
              提交 {{ s.createdAt }}
              <template v-if="s.reviewedAt"> · 审核 {{ s.reviewedAt }}</template>
            </p>
            <p v-if="s.reviewReason" class="sub-reason">驳回原因：{{ s.reviewReason }}</p>
            <p class="sub-link">查看完整正文 →</p>
          </li>
        </ul>
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
.caps-alert {
  margin: 12px 0 4px;
}
.caps-alert-desc {
  margin: 0 0 8px;
  font-size: 13px;
  line-height: 1.5;
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
.submissions {
  margin-bottom: 16px;
}
.sub-list {
  list-style: none;
  padding: 0;
  margin: 0;
}
.sub-item {
  padding: 12px 0;
  border-bottom: 1px solid var(--jh-line);
}
.sub-clickable {
  cursor: pointer;
}
.sub-clickable:hover .sub-link,
.sub-clickable:focus .sub-link {
  color: var(--jh-seal);
}
.sub-item:last-child {
  border-bottom: none;
}
.sub-head {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
  margin-bottom: 4px;
}
.sub-status {
  margin-left: auto;
}
.sub-summary {
  margin: 0 0 4px;
  color: var(--jh-ink);
}
.sub-meta {
  margin: 0;
  font-size: 12px;
}
.sub-reason {
  margin: 6px 0 0;
  font-size: 13px;
  color: var(--jh-seal);
}
.sub-link {
  margin: 6px 0 0;
  font-size: 13px;
  color: var(--jh-muted);
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
    background: transparent;
    border: 1px solid var(--jh-line);
    border-radius: var(--jh-radius);
    backdrop-filter: blur(6px);
  }
  .action-main,
  .action-item {
    flex: 1 1 calc(50% - 8px);
    margin: 0 !important;
  }
  .action-main {
    flex-basis: 100%;
  }
  .sub-status {
    margin-left: 0;
  }
}
</style>
