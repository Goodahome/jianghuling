<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { getMyRank, getRanks } from '@/api/rank'
import type { RankType } from '@/types/api'
import type { RankItem } from '@/types/models'
import { useAuthStore } from '@/stores/auth'
import JhPageHeader from '@/components/JhPageHeader.vue'

type MyRankPayload = {
  reputationRank?: number | null
  chivalryRank?: number | null
  completedRank?: number | null
  reputationScore?: number | string | null
  chivalry?: number | null
  completedOrders?: number | null
}

const auth = useAuthStore()
const type = ref<RankType>('REPUTATION')
const list = ref<RankItem[]>([])
const lord = ref<{ userId: number; nickname: string } | null>(null)
const myRank = ref<MyRankPayload | null>(null)

const currentRank = computed(() => {
  const m = myRank.value
  if (!m) return null
  if (type.value === 'CHIVALRY') return m.chivalryRank ?? null
  if (type.value === 'COMPLETED') return m.completedRank ?? null
  return m.reputationRank ?? null
})

const currentScore = computed(() => {
  const m = myRank.value
  if (!m) return null
  if (type.value === 'CHIVALRY') return m.chivalry ?? null
  if (type.value === 'COMPLETED') return m.completedOrders ?? null
  return m.reputationScore ?? null
})

async function load() {
  const data = await getRanks(type.value, { page: 1, pageSize: 50 })
  list.value = data.list || []
  lord.value = data.lord || null
  if (auth.isLoggedIn) {
    myRank.value = (await getMyRank().catch(() => null)) as MyRankPayload | null
  }
}

function onTypeTab(name: string | number) {
  type.value = String(name) as RankType
  void load()
}

onMounted(load)
</script>

<template>
  <section class="jh-section">
    <div class="jh-container">
      <JhPageHeader title="英雄榜" />
      <div v-if="lord" class="lord jh-panel">
        <span>武林盟主荣耀位</span>
        <strong>{{ lord.nickname }}</strong>
      </div>
      <el-tabs v-model="type" class="tabs" @tab-change="onTypeTab">
        <el-tab-pane label="声望榜" name="REPUTATION" />
        <el-tab-pane label="侠义榜" name="CHIVALRY" />
        <el-tab-pane label="完令榜" name="COMPLETED" />
      </el-tabs>
      <div v-if="myRank" class="mine jh-panel">
        我的排名：第 {{ currentRank != null && currentRank > 0 ? currentRank : '—' }} 名
        <span v-if="currentScore != null"> · 分值 {{ currentScore }}</span>
      </div>
      <div class="jh-panel table-wrap">
        <el-table :data="list">
          <el-table-column prop="rank" label="排名" width="80" />
          <el-table-column prop="nickname" label="侠士" />
          <el-table-column prop="levelTitle" label="头衔" />
          <el-table-column prop="score" label="分值" />
        </el-table>
      </div>
    </div>
  </section>
</template>

<style scoped>
.lord {
  padding: 16px;
  margin-bottom: 12px;
  display: flex;
  gap: 12px;
  align-items: baseline;
}
.lord strong {
  font-size: 24px;
  font-family: var(--jh-font-display);
  color: var(--jh-seal);
}
.tabs {
  margin-bottom: 14px;
}
.tabs :deep(.el-tabs__item) {
  color: rgba(247, 240, 221, 0.75);
}
.tabs :deep(.el-tabs__item.is-active) {
  color: var(--jh-gold-bright);
}
.tabs :deep(.el-tabs__active-bar) {
  background-color: var(--jh-gold);
}
.tabs :deep(.el-tabs__nav-wrap::after) {
  background-color: rgba(196, 163, 90, 0.25);
}
.mine {
  padding: 12px 16px;
  margin-bottom: 12px;
  font-size: 14px;
}
.table-wrap {
  padding: 0;
  margin-bottom: 14px;
  overflow: hidden;
}
.table-wrap :deep(.el-table) {
  --el-table-border-color: var(--jh-line);
}
.table-wrap :deep(.el-table::before),
.table-wrap :deep(.el-table::after) {
  background-color: var(--jh-line);
}
.table-wrap :deep(.el-table__inner-wrapper::before) {
  background-color: var(--jh-line);
}

@media (max-width: 768px) {
  .tabs :deep(.el-tabs__item) {
    padding: 0 12px;
    font-size: 14px;
  }
}
</style>
