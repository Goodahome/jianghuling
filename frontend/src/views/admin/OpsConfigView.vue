<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
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

const tab = ref('growth')
const growth = reactive<Record<string, number | string>>({})
const ranksText = ref('{}')
const levelsText = ref('[]')
const reward = reactive<{ minReward: number | string; difficulties: Record<string, unknown>[] }>({
  minReward: 200,
  difficulties: [],
})

async function load() {
  const [g, r, l, s] = await Promise.all([
    adminGetGrowthConfig(),
    adminGetRanksConfig(),
    adminGetLevels(),
    adminGetRewardSuggestConfig(),
  ])
  Object.assign(growth, g || {})
  ranksText.value = JSON.stringify(r || {}, null, 2)
  levelsText.value = JSON.stringify(l || [], null, 2)
  reward.minReward = (s?.minReward as number) ?? 200
  reward.difficulties = (s?.difficulties as Record<string, unknown>[]) || []
}

async function saveGrowth() {
  await adminPutGrowthConfig({ ...growth })
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
  <div>
    <h2>运营参数</h2>
    <el-tabs v-model="tab">
      <el-tab-pane label="成长参数" name="growth">
        <el-form label-width="160px" style="max-width: 520px">
          <el-form-item v-for="key in Object.keys(growth)" :key="key" :label="key">
            <el-input v-model="growth[key]" />
          </el-form-item>
          <el-button type="primary" @click="saveGrowth">保存</el-button>
        </el-form>
      </el-tab-pane>
      <el-tab-pane label="赏银建议" name="reward">
        <el-form-item label="最低赏银">
          <el-input v-model="reward.minReward" style="width: 200px" />
        </el-form-item>
        <el-table :data="reward.difficulties" style="margin-bottom: 12px">
          <el-table-column prop="code" label="code" width="120" />
          <el-table-column prop="name" label="名称" width="120">
            <template #default="{ row }"><el-input v-model="row.name" /></template>
          </el-table-column>
          <el-table-column prop="suggestMin" label="建议下限" width="140">
            <template #default="{ row }"><el-input v-model="row.suggestMin" /></template>
          </el-table-column>
          <el-table-column prop="suggestMax" label="建议上限" width="140">
            <template #default="{ row }"><el-input v-model="row.suggestMax" /></template>
          </el-table-column>
        </el-table>
        <el-button type="primary" @click="saveReward">保存</el-button>
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
