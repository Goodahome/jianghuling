<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getBounty } from '@/api/bounty'
import { reviewBounty } from '@/api/hall'
import { getWarrantTemplates } from '@/api/meta'
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
import HallBackBar from '@/components/HallBackBar.vue'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const detail = ref<BountyDetail | null>(null)
const warrantTemplates = ref<WarrantTemplate[]>([])

const bountyId = computed(() => String(route.params.id))

const warrantFieldDefs = computed((): WarrantFieldDef[] => {
  const type = detail.value?.type
  if (!type) return []
  const tpl = warrantTemplates.value.find((t) => t.type === type || (t as { code?: string }).code === type)
  return tpl?.fields || []
})

const warrantRows = computed(() => {
  const fields = detail.value?.warrantFields || {}
  const rows: { key: string; label: string; text: string }[] = []
  const seen = new Set<string>()
  for (const def of warrantFieldDefs.value) {
    seen.add(def.key)
    const val = fields[def.key]
    if (isWarrantValueEmpty(val) && !def.required) continue
    rows.push({
      key: def.key,
      label: def.label || warrantFieldFallbackLabel[def.key] || def.key,
      text: formatWarrantValue(val) || '—',
    })
  }
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
      getBounty(bountyId.value),
      getWarrantTemplates().catch(() => [] as WarrantTemplate[]),
    ])
    detail.value = bounty
    warrantTemplates.value = templates
  } finally {
    loading.value = false
  }
}

async function decide(result: 'APPROVE' | 'REJECT') {
  let reason = ''
  if (result === 'REJECT') {
    const { value } = await ElMessageBox.prompt('请填写驳回原因', '驳回发令')
    reason = value
  }
  submitting.value = true
  try {
    await reviewBounty(bountyId.value, { result, reason })
    ElMessage.success(result === 'APPROVE' ? '已通过' : '已驳回')
    router.push('/hall/bounty-reviews')
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>

<template>
  <section class="jh-section" v-loading="loading">
    <div class="jh-container" v-if="detail">
      <HallBackBar
        :items="[
          { label: '令审队列', to: '/hall/bounty-reviews' },
          { label: '令审详情' },
        ]"
      >
        <el-button class="jh-btn-seal" :loading="submitting" @click="decide('APPROVE')">通过</el-button>
        <el-button class="jh-btn-ink" :loading="submitting" @click="decide('REJECT')">驳回</el-button>
      </HallBackBar>

      <div class="head jh-panel">
        <div class="tags">
          <div class="tags-left">
            <span class="type">{{ bountyTypeLabel[detail.type] }}</span>
            <StatusTag :status="detail.status" />
          </div>
          <span class="difficulty">{{ difficultyLabel[detail.difficulty] }}</span>
        </div>
        <h1>{{ detail.title }}</h1>
        <p class="reward">赏银 {{ formatAmount(detail.rewardAmount) }} 两 · 揭榜 {{ detail.claimCount || 0 }} 人</p>
        <p class="jh-muted">
          {{ detail.district || '遵义' }} · 截止 {{ detail.deadlineAt }}
        </p>
      </div>

      <div class="cols">
        <div class="jh-panel block">
          <h2>租房令状</h2>
          <el-empty v-if="!warrantRows.length" description="暂无令状信息" />
          <el-descriptions v-else :column="1" border>
            <el-descriptions-item v-for="row in warrantRows" :key="row.key" :label="row.label">
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
  font-size: clamp(24px, 4vw, 34px);
}
.reward {
  font-size: 18px;
  color: var(--jh-seal);
  margin: 0 0 4px;
}
.cols {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: 16px;
}
.block {
  padding: 18px;
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
  .cols {
    grid-template-columns: 1fr;
  }
  .block {
    padding: 14px;
  }
}
</style>
