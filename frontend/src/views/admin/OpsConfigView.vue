<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  adminGetGrowthConfig,
  adminGetLevels,
  adminGetRanksConfig,
  adminGetRewardSuggestConfig,
  adminPutGrowthConfig,
  adminPutLevels,
  adminPutRanksConfig,
  adminPutRewardSuggestConfig,
} from '@/api/admin'

type GrowthField = {
  key: string
  label: string
  unit?: string
  hint?: string
  step?: number
  precision?: number
  min?: number
}

/** 与 GET/PUT /admin/configs/growth 字段一致，顺序按业务分组 */
const GROWTH_GROUPS: { title: string; fields: GrowthField[] }[] = [
  {
    title: '揭榜与体力',
    fields: [
      { key: 'claimDayLimit', label: '每日揭榜上限', unit: '次', min: 0, step: 1 },
      { key: 'dailyFreeStamina', label: '每日免费体力', unit: '点', min: 0, step: 1 },
      { key: 'claimStaminaCost', label: '单次揭榜耗体力', unit: '点', min: 0, step: 1 },
      {
        key: 'chivalryPerStamina',
        label: '兑换 1 点体力所需侠义',
        unit: '侠义',
        min: 0,
        step: 1,
      },
    ],
  },
  {
    title: '成果提交',
    fields: [
      {
        key: 'submitCooldownSeconds',
        label: '提交冷却时间',
        unit: '秒',
        hint: '例如 600 = 10 分钟',
        min: 0,
        step: 60,
      },
      { key: 'submitDayLimit', label: '每日提交上限', unit: '次', min: 0, step: 1 },
    ],
  },
  {
    title: '奖励与邀请',
    fields: [
      { key: 'chivalryPerComplete', label: '完结基础侠义奖励', unit: '侠义', min: 0, step: 1 },
      { key: 'inviteDailyQuota', label: '每日邀请码额度', unit: '个', min: 0, step: 1 },
    ],
  },
  {
    title: '赏银与手续费',
    fields: [
      { key: 'minReward', label: '最低赏银', unit: '两', min: 0, step: 50 },
      {
        key: 'feeRate',
        label: '平台服务费比例',
        hint: '小数，如 0.1 表示 10%',
        min: 0,
        step: 0.01,
        precision: 4,
      },
    ],
  },
]

const GROWTH_KEYS = GROWTH_GROUPS.flatMap((g) => g.fields.map((f) => f.key))

const route = useRoute()
const router = useRouter()

const VALID_TABS = new Set(['growth', 'reward', 'levels', 'ranks'])
const tab = ref('growth')
const growth = reactive<Record<string, number>>({})
const ranksText = ref('{}')
const levelsText = ref('[]')
const reward = reactive<{ minReward: number; difficulties: Record<string, unknown>[] }>({
  minReward: 200,
  difficulties: [],
})

const feeRatePercent = computed(() => {
  const n = Number(growth.feeRate)
  if (!Number.isFinite(n)) return '—'
  return `${(n * 100).toFixed(2)}%`
})

function syncTabFromRoute() {
  const raw = Array.isArray(route.query.tab) ? route.query.tab[0] : route.query.tab
  const next = String(raw || 'growth')
  tab.value = VALID_TABS.has(next) ? next : 'growth'
}

watch(
  () => route.query.tab,
  () => syncTabFromRoute(),
  { immediate: true },
)

watch(tab, (name) => {
  if (route.query.tab === name) return
  router.replace({ path: '/admin/ops', query: { tab: name } })
})

function toNum(v: unknown, fallback = 0) {
  const n = Number(v)
  return Number.isFinite(n) ? n : fallback
}

async function load() {
  const [g, r, l, s] = await Promise.all([
    adminGetGrowthConfig(),
    adminGetRanksConfig(),
    adminGetLevels(),
    adminGetRewardSuggestConfig(),
  ])
  for (const key of GROWTH_KEYS) {
    growth[key] = toNum(g?.[key], 0)
  }
  // 兼容接口多出的未知键，仍可保存时不丢失：合并进 growth
  if (g) {
    for (const [k, v] of Object.entries(g)) {
      if (!(k in growth)) growth[k] = toNum(v, 0)
    }
  }
  ranksText.value = JSON.stringify(r || {}, null, 2)
  levelsText.value = JSON.stringify(l || [], null, 2)
  reward.minReward = toNum(s?.minReward, 200)
  reward.difficulties = (s?.difficulties as Record<string, unknown>[]) || []
}

async function saveGrowth() {
  const payload: Record<string, number> = {}
  for (const key of Object.keys(growth)) {
    payload[key] = toNum(growth[key], 0)
  }
  await adminPutGrowthConfig(payload)
  ElMessage.success('成长参数已保存')
  await load()
}

async function saveRanks() {
  await adminPutRanksConfig(JSON.parse(ranksText.value))
  ElMessage.success('英雄谱规则已保存')
  await load()
}

async function saveLevels() {
  await adminPutLevels(JSON.parse(levelsText.value))
  ElMessage.success('等级配置已保存')
  await load()
}

async function saveReward() {
  await adminPutRewardSuggestConfig({
    minReward: reward.minReward,
    difficulties: reward.difficulties,
  })
  ElMessage.success('赏银建议已保存')
  await load()
}

onMounted(load)
</script>

<template>
  <div class="ops-page">
    <h2>运营参数</h2>
    <el-tabs v-model="tab">
      <el-tab-pane label="成长参数" name="growth">
        <div class="growth-board">
          <section v-for="group in GROWTH_GROUPS" :key="group.title" class="growth-group">
            <h3>{{ group.title }}</h3>
            <el-form label-width="200px" class="growth-form">
              <el-form-item v-for="field in group.fields" :key="field.key">
                <template #label>
                  <span class="field-label">{{ field.label }}</span>
                </template>
                <div class="field-control">
                  <el-input-number
                    v-model="growth[field.key]"
                    :min="field.min ?? 0"
                    :step="field.step ?? 1"
                    :precision="field.precision"
                    controls-position="right"
                  />
                  <span v-if="field.unit" class="field-unit">{{ field.unit }}</span>
                  <span v-if="field.key === 'feeRate'" class="field-hint">≈ {{ feeRatePercent }}</span>
                  <span v-else-if="field.hint" class="field-hint">{{ field.hint }}</span>
                </div>
                <div class="field-key">配置键 {{ field.key }}</div>
              </el-form-item>
            </el-form>
          </section>
          <el-button type="primary" @click="saveGrowth">保存成长参数</el-button>
        </div>
      </el-tab-pane>

      <el-tab-pane label="赏银建议" name="reward">
        <el-form label-width="100px" class="reward-form">
          <el-form-item label="最低赏银">
            <div class="field-control">
              <el-input-number v-model="reward.minReward" :min="0" :step="50" controls-position="right" />
              <span class="field-unit">两</span>
            </div>
          </el-form-item>
        </el-form>
        <el-table :data="reward.difficulties" style="margin-bottom: 12px">
          <el-table-column prop="code" label="档位代码" width="120" />
          <el-table-column prop="name" label="名称" width="140">
            <template #default="{ row }"><el-input v-model="row.name" /></template>
          </el-table-column>
          <el-table-column prop="suggestMin" label="建议下限（两）" width="160">
            <template #default="{ row }">
              <el-input-number v-model="row.suggestMin" :min="0" :step="50" controls-position="right" />
            </template>
          </el-table-column>
          <el-table-column prop="suggestMax" label="建议上限（两）" width="160">
            <template #default="{ row }">
              <el-input-number v-model="row.suggestMax" :min="0" :step="50" controls-position="right" />
            </template>
          </el-table-column>
        </el-table>
        <el-button type="primary" @click="saveReward">保存赏银建议</el-button>
      </el-tab-pane>

      <el-tab-pane label="等级配置" name="levels">
        <el-input v-model="levelsText" type="textarea" :rows="14" />
        <el-button type="primary" style="margin-top: 12px" @click="saveLevels">保存 JSON</el-button>
      </el-tab-pane>
      <el-tab-pane label="英雄谱规则" name="ranks">
        <el-input v-model="ranksText" type="textarea" :rows="12" />
        <el-button type="primary" style="margin-top: 12px" @click="saveRanks">保存 JSON</el-button>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<style scoped>
.ops-page h2 {
  margin: 0 0 12px;
}
.growth-board {
  max-width: 720px;
}
.growth-group {
  margin-bottom: 20px;
  padding: 14px 16px 4px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  background: var(--el-fill-color-blank);
}
.growth-group h3 {
  margin: 0 0 10px;
  font-size: 15px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}
.growth-form :deep(.el-form-item) {
  margin-bottom: 14px;
}
.field-label {
  line-height: 1.3;
}
.field-control {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}
.field-control :deep(.el-input-number) {
  width: 180px;
}
.field-unit {
  color: var(--el-text-color-regular);
  font-size: 13px;
}
.field-hint {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
.field-key {
  margin-top: 4px;
  font-size: 12px;
  color: var(--el-text-color-placeholder);
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
}
.reward-form {
  margin-bottom: 8px;
}
</style>
