<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { listBounties } from '@/api/bounty'
import { getTopNotices } from '@/api/notice'
import type { BountyListItem, Notice } from '@/types/models'
import {
  bountyTypeLabel,
  difficultyLabel,
  formatAmount,
} from '@/utils/labels'
import StatusTag from '@/components/StatusTag.vue'
import EmptyState from '@/components/EmptyState.vue'

const loading = ref(false)
const list = ref<BountyListItem[]>([])
const total = ref(0)
const tops = ref<Notice[]>([])
const query = reactive({
  page: 1,
  pageSize: 12,
  type: '' as '' | 'RENT_SEEK' | 'RENT_OUT',
  keyword: '',
  district: '',
})

async function load() {
  loading.value = true
  try {
    const data = await listBounties({
      page: query.page,
      pageSize: query.pageSize,
      type: query.type || undefined,
      keyword: query.keyword || undefined,
      district: query.district || undefined,
      status: 'OPEN,IN_COLLAB',
    })
    list.value = data.list || []
    total.value = data.total || 0
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  try {
    tops.value = (await getTopNotices('ANTI_FRAUD', 3)) || []
  } catch {
    tops.value = []
  }
  await load()
})
</script>

<template>
  <section class="hero-band">
    <div class="band-inner">
      <div class="copy">
        <p class="eyebrow">遵义城 · 告示牌前</p>
        <h1 class="brand-title">江湖令</h1>
        <p class="slogan">天下有悬赏，江湖有侠士。</p>
        <p class="belief">江湖不让善意吃亏。</p>
        <div class="cta-row">
          <RouterLink class="btn primary" to="/bounties/publish">张贴悬赏</RouterLink>
          <RouterLink class="btn ghost" to="/notices">先看告示</RouterLink>
        </div>
      </div>
    </div>
  </section>

  <section class="jh-section jinbang-section">
    <div class="jh-container">
      <div v-if="tops.length" class="notice-strip jh-panel">
        <strong>防骗箴言</strong>
        <RouterLink v-for="n in tops" :key="n.id" :to="`/notices/${n.id}`">{{ n.title }}</RouterLink>
      </div>

      <div class="jinbang-board" aria-label="悬赏金榜">
        <header class="jinbang-header">
          <span class="header-ornament" aria-hidden="true" />
          <div class="header-copy">
            <h2 class="jinbang-title">悬赏金榜</h2>
            <p class="jinbang-sub">揭榜行侠 · 赏银分明</p>
          </div>
          <span class="header-ornament mirror" aria-hidden="true" />
        </header>

        <div class="toolbar">
          <el-radio-group v-model="query.type" @change="query.page = 1; load()">
            <el-radio-button value="">全部</el-radio-button>
            <el-radio-button value="RENT_SEEK">求租</el-radio-button>
            <el-radio-button value="RENT_OUT">出租/转租</el-radio-button>
          </el-radio-group>
          <div class="filters">
            <el-input v-model="query.district" class="filter-district" placeholder="片区" clearable />
            <el-input
              v-model="query.keyword"
              class="filter-keyword"
              placeholder="搜寻榜文"
              clearable
              @keyup.enter="query.page = 1; load()"
            />
            <el-button type="primary" class="jh-btn-seal filter-btn" @click="query.page = 1; load()">
              筛榜
            </el-button>
          </div>
        </div>

        <div v-loading="loading" class="jinbang-grid">
          <EmptyState
            v-if="!loading && !list.length"
            class="grid-empty"
            title="金榜暂虚位以待"
            description="尚未有人张贴新令，可先发一榜。"
          >
            <RouterLink to="/bounties/publish">
              <el-button type="primary" class="jh-btn-seal">去张贴</el-button>
            </RouterLink>
          </EmptyState>

          <div
            v-for="(item, index) in list"
            :key="item.id"
            class="paper-wrap"
            :style="{ '--tilt': `${((index % 5) - 2) * 0.8}deg` }"
          >
            <RouterLink :to="`/bounties/${item.id}`" class="paper-poster">
              <span class="nail" aria-hidden="true">
                <span class="nail-head" />
                <span class="nail-hole" />
              </span>
              <div class="poster-top">
                <span class="type">{{ bountyTypeLabel[item.type] }}</span>
                <StatusTag :status="item.status" />
              </div>
              <h3 class="poster-title">{{ item.title }}</h3>
              <p class="meta">
                {{ item.district || '遵义' }} · {{ difficultyLabel[item.difficulty] }}
              </p>
              <p class="meta claims">揭榜 {{ item.claimCount || 0 }} 人</p>
              <div class="poster-bottom">
                <strong class="reward">赏银 {{ formatAmount(item.rewardAmount) }} 两</strong>
                <span class="deadline">截止 {{ item.deadlineAt?.slice(0, 10) }}</span>
              </div>
              <span class="poster-seal" aria-hidden="true">令</span>
            </RouterLink>
          </div>
        </div>

        <div v-if="total > query.pageSize" class="pager">
          <el-pagination
            v-model:current-page="query.page"
            :page-size="query.pageSize"
            :total="total"
            layout="prev, pager, next"
            @current-change="load"
          />
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.hero-band {
  min-height: auto;
  display: flex;
  align-items: center;
  padding: 20px clamp(12px, 4vw, 32px) 8px;
  color: #f7f0dd;
}
.band-inner {
  width: 100%;
  max-width: 1120px;
  margin: 0 auto;
}
.copy {
  max-width: 36em;
  padding: 16px 18px;
  background: rgba(28, 22, 16, 0.45);
  border: 1px solid rgba(196, 163, 90, 0.35);
}
.eyebrow {
  margin: 0 0 8px;
  font-size: 13px;
  letter-spacing: 0.28em;
  color: var(--jh-gold-bright);
}
.brand-title {
  margin: 0;
  font-size: clamp(36px, 6vw, 56px);
  line-height: 1.1;
  color: #f7f0dd;
}
.slogan {
  margin: 12px 0 6px;
  font-family: var(--jh-font-display);
  font-size: clamp(18px, 2.8vw, 24px);
  letter-spacing: 0.06em;
  line-height: 1.4;
  color: var(--jh-gold-bright);
}
.belief {
  margin: 0 0 20px;
  font-size: 15px;
  line-height: 1.6;
  color: rgba(247, 240, 221, 0.8);
}
.cta-row {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 10px 16px;
  min-height: 40px;
  border-radius: 2px;
  border: 1px solid transparent;
  font-family: var(--jh-font-display);
  letter-spacing: 0.12em;
}
.btn.primary {
  background: linear-gradient(180deg, #c9a24a, var(--jh-gold-deep));
  color: #1c1408;
  border-color: var(--jh-gold-bright);
}
.btn.ghost {
  border-color: rgba(228, 200, 120, 0.55);
  color: var(--jh-gold-bright);
}
.notice-strip {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 18px;
  padding: 14px 18px;
  margin-bottom: 20px;
  align-items: center;
  background: rgba(251, 246, 232, 0.9) !important;
}
.notice-strip a {
  color: var(--jh-seal);
}

.jinbang-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-bottom: 18px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(196, 163, 90, 0.35);
}
.header-copy {
  text-align: center;
}
.jinbang-title {
  margin: 0;
  font-family: var(--jh-font-display);
  font-size: clamp(26px, 4vw, 34px);
  letter-spacing: 0.35em;
  color: var(--jh-gold-bright);
  text-indent: 0.35em;
  text-shadow: 0 1px 0 rgba(0, 0, 0, 0.35);
}
.jinbang-sub {
  margin: 6px 0 0;
  font-size: 13px;
  letter-spacing: 0.2em;
  color: rgba(247, 240, 221, 0.7);
}
.header-ornament {
  width: 48px;
  height: 2px;
  background: linear-gradient(90deg, transparent, var(--jh-gold), transparent);
}
.header-ornament.mirror {
  transform: scaleX(-1);
}

.toolbar {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 18px;
}
.filters {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.filter-district {
  width: 120px;
}
.filter-keyword {
  width: 180px;
}
.toolbar :deep(.el-radio-button__inner) {
  background: rgba(251, 246, 232, 0.85);
  border-color: rgba(196, 163, 90, 0.45);
  color: var(--jh-plaque-ink);
}
.toolbar :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: rgba(196, 163, 90, 0.35);
  border-color: var(--jh-gold);
  color: #1c1408;
  box-shadow: none;
}

/* 小 A4 纸钉墙：一行多张 */
.jinbang-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 22px 18px;
  min-height: 160px;
  align-items: start;
  justify-items: center;
}
.grid-empty {
  grid-column: 1 / -1;
  width: 100%;
  background: rgba(251, 246, 232, 0.85);
  border: 1px solid rgba(196, 163, 90, 0.35);
}

.paper-wrap {
  width: min(100%, 220px);
  transform: rotate(var(--tilt, 0deg));
  transition: transform 0.18s ease;
}
.paper-wrap:hover {
  transform: rotate(0deg) translateY(-2px);
  z-index: 2;
}
.paper-poster {
  position: relative;
  display: flex;
  flex-direction: column;
  /* 小 A4 比例 210:297 */
  aspect-ratio: 210 / 297;
  width: 100%;
  padding: 28px 16px 18px;
  color: var(--jh-plaque-ink);
  background-color: #f7f1e3;
  background-image:
    linear-gradient(180deg, rgba(255, 255, 255, 0.35), rgba(230, 214, 180, 0.15)),
    url('/textures/rice-paper.png');
  background-size: cover, 180px 180px;
  border: 1px solid rgba(180, 160, 120, 0.45);
  border-radius: 1px;
  box-shadow:
    0 1px 0 rgba(255, 255, 255, 0.45) inset,
    0 10px 18px rgba(20, 14, 8, 0.28),
    0 2px 4px rgba(20, 14, 8, 0.18);
}
.paper-poster:hover {
  box-shadow:
    0 1px 0 rgba(255, 255, 255, 0.5) inset,
    0 14px 24px rgba(20, 14, 8, 0.32),
    0 3px 6px rgba(20, 14, 8, 0.2);
}
/* 钉子钉在纸上方正中 */
.nail {
  position: absolute;
  top: 8px;
  left: 50%;
  width: 18px;
  height: 18px;
  margin-left: -9px;
  z-index: 2;
  pointer-events: none;
}
.nail-hole {
  position: absolute;
  left: 50%;
  top: 50%;
  width: 8px;
  height: 8px;
  margin: -3px 0 0 -4px;
  border-radius: 50%;
  background: rgba(40, 28, 16, 0.28);
  box-shadow: 0 0 0 1px rgba(40, 28, 16, 0.12);
}
.nail-head {
  position: absolute;
  left: 50%;
  top: 50%;
  width: 14px;
  height: 14px;
  margin: -8px 0 0 -7px;
  border-radius: 50%;
  background:
    radial-gradient(circle at 32% 28%, #f0e6d0 0%, #b8a070 42%, #6a5840 78%, #3a3020 100%);
  box-shadow:
    0 1px 2px rgba(0, 0, 0, 0.45),
    inset 0 1px 1px rgba(255, 255, 255, 0.35);
}
.poster-top {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
  margin-bottom: 8px;
}
.type {
  color: var(--jh-seal);
  font-size: 13px;
  font-family: var(--jh-font-display);
}
.poster-title {
  margin: 0 0 8px;
  font-family: var(--jh-font-display);
  font-size: 17px;
  line-height: 1.4;
  letter-spacing: 0.04em;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 2.8em;
}
.meta {
  margin: 0;
  font-size: 12px;
  color: var(--jh-muted);
}
.claims {
  margin-top: 4px;
}
.poster-bottom {
  margin-top: auto;
  padding-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.reward {
  color: var(--jh-seal);
  font-size: 16px;
  font-family: var(--jh-font-display);
}
.deadline {
  font-size: 12px;
  color: var(--jh-muted);
}
.poster-seal {
  position: absolute;
  right: 14px;
  bottom: 16px;
  width: 34px;
  height: 34px;
  display: grid;
  place-items: center;
  border: 2px solid rgba(178, 58, 45, 0.65);
  color: rgba(178, 58, 45, 0.75);
  font-family: var(--jh-font-display);
  font-size: 15px;
  transform: rotate(-10deg);
  opacity: 0.72;
  pointer-events: none;
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
.pager :deep(.el-pagination) {
  --el-pagination-bg-color: transparent;
  --el-pagination-button-color: var(--jh-gold-bright);
  --el-pagination-hover-color: var(--jh-gold);
  --el-text-color-regular: rgba(247, 240, 221, 0.8);
}

@media (max-width: 960px) {
  .jinbang-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .hero-band {
    padding: 16px 8px 8px;
  }
  .cta-row {
    flex-direction: column;
  }
  .btn {
    width: 100%;
  }
  .jinbang-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  .paper-wrap {
    width: min(100%, 260px);
    transform: none;
  }
  .jinbang-grid {
    justify-items: center;
  }
  .toolbar {
    flex-direction: column;
    align-items: stretch;
  }
  .toolbar :deep(.el-radio-group) {
    display: flex;
    width: 100%;
  }
  .toolbar :deep(.el-radio-button) {
    flex: 1;
  }
  .toolbar :deep(.el-radio-button__inner) {
    width: 100%;
  }
  .filters {
    width: 100%;
  }
  .filter-district,
  .filter-keyword {
    flex: 1;
    width: auto !important;
    min-width: 0;
  }
  .filter-btn {
    width: 100%;
  }
}
</style>
