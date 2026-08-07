<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
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
import JhPageHeader from '@/components/JhPageHeader.vue'

const loading = ref(false)
const list = ref<BountyListItem[]>([])
const total = ref(0)
const tops = ref<Notice[]>([])
const mottoViewport = ref<HTMLElement | null>(null)
const mottoTrack = ref<HTMLElement | null>(null)
const shouldScroll = ref(false)
const scrollDuration = ref(18)
let mottoObserver: ResizeObserver | null = null

const query = reactive({
  page: 1,
  pageSize: 12,
  type: '' as '' | 'RENT_SEEK' | 'RENT_OUT',
  keyword: '',
  district: '',
})

const mottoText = computed(() =>
  tops.value
    .map((n) => (n.content || n.summary || n.title || '').replace(/\s+/g, ' ').trim())
    .filter(Boolean)
    .join('　·　'),
)

function measureMotto() {
  const viewport = mottoViewport.value
  const track = mottoTrack.value
  if (!viewport || !track) {
    shouldScroll.value = false
    return
  }
  const textEl = track.querySelector('.notice-text') as HTMLElement | null
  const contentWidth = textEl?.scrollWidth ?? track.scrollWidth
  const style = getComputedStyle(viewport)
  const padX = (parseFloat(style.paddingLeft) || 0) + (parseFloat(style.paddingRight) || 0)
  const overflow = contentWidth > viewport.clientWidth - padX + 2
  shouldScroll.value = overflow
  if (overflow) {
    scrollDuration.value = Math.max(12, Math.min(48, contentWidth / 40))
  }
}

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

watch(mottoText, async () => {
  await nextTick()
  measureMotto()
})

onMounted(async () => {
  try {
    tops.value = (await getTopNotices('ANTI_FRAUD', 3)) || []
  } catch {
    tops.value = []
  }
  await nextTick()
  measureMotto()
  if (typeof ResizeObserver !== 'undefined' && mottoViewport.value) {
    mottoObserver = new ResizeObserver(() => measureMotto())
    mottoObserver.observe(mottoViewport.value)
  }
  await load()
})

onBeforeUnmount(() => {
  mottoObserver?.disconnect()
  mottoObserver = null
})
</script>

<template>
  <section class="jh-section plaza-section">
    <div class="jh-container">
      <div class="jinbang-board" aria-label="悬赏榜">
        <JhPageHeader title="悬赏榜" subtitle="揭榜行侠 · 赏银分明" />

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
            <RouterLink to="/bounties/publish">
              <el-button class="jh-btn-ink filter-btn">张贴悬赏</el-button>
            </RouterLink>
          </div>
        </div>

        <div v-if="mottoText" class="notice-strip" aria-label="防骗箴言">
          <strong class="notice-label">防骗箴言：</strong>
          <div ref="mottoViewport" class="notice-marquee">
            <div
              ref="mottoTrack"
              class="notice-track"
              :class="{ scrolling: shouldScroll }"
              :style="shouldScroll ? { '--marquee-duration': `${scrollDuration}s` } : undefined"
            >
              <span class="notice-text">{{ mottoText }}</span>
              <span v-if="shouldScroll" class="notice-text" aria-hidden="true">{{ mottoText }}</span>
            </div>
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
              <h2 class="poster-title">{{ item.title }}</h2>
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
.notice-strip {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  max-width: 720px;
  margin: 0 0 16px;
  padding: 4px 12px 8px 8px;
  background: transparent;
  border: none;
  box-shadow: none;
  color: rgba(247, 240, 221, 0.92);
}
.notice-label {
  flex: none;
  font-family: var(--jh-font-display);
  letter-spacing: 0.08em;
  color: var(--jh-gold-bright);
  white-space: nowrap;
}
.notice-marquee {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  /* 首字避开左侧淡出带 */
  padding-left: 14px;
  mask-image: linear-gradient(90deg, transparent, #000 12px, #000 calc(100% - 12px), transparent);
}
.notice-track {
  display: inline-flex;
  width: max-content;
  max-width: 100%;
  gap: 48px;
  white-space: nowrap;
}
.notice-track.scrolling {
  max-width: none;
  animation: notice-marquee var(--marquee-duration, 18s) linear infinite;
}
.notice-text {
  color: rgba(247, 240, 221, 0.9);
  font-size: 14px;
  letter-spacing: 0.04em;
}
@keyframes notice-marquee {
  from {
    transform: translateX(0);
  }
  to {
    transform: translateX(-50%);
  }
}
@media (prefers-reduced-motion: reduce) {
  .notice-track.scrolling {
    animation: none;
    max-width: 100%;
  }
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

.jinbang-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, 200px);
  gap: 16px 14px;
  min-height: 160px;
  align-items: start;
  justify-content: center;
  justify-items: stretch;
}
.grid-empty {
  grid-column: 1 / -1;
  width: 100%;
  background: rgba(251, 246, 232, 0.85);
  border: 1px solid rgba(196, 163, 90, 0.35);
}

.paper-wrap {
  width: min(100%, 210px);
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
  /* 略高于上一版，仍短于完整 A4 */
  aspect-ratio: 210 / 235;
  width: 100%;
  padding: 24px 14px 12px;
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
  font-weight: normal;
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

@media (max-width: 768px) {
  .plaza-section {
    overflow-x: clip;
    max-width: 100%;
  }
  .jinbang-board {
    min-width: 0;
    max-width: 100%;
  }
  .notice-strip {
    flex-direction: column;
    align-items: stretch;
    gap: 6px;
    max-width: 100%;
    margin-bottom: 14px;
    padding: 4px 0 8px;
  }
  .notice-label {
    white-space: nowrap;
  }
  .notice-marquee {
    width: 100%;
    padding-left: 8px;
  }
  .notice-text {
    font-size: 13px;
  }
  .jinbang-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 10px;
    justify-content: stretch;
  }
  .paper-wrap {
    width: 100%;
    max-width: 100%;
    transform: none;
  }
  .paper-wrap:hover {
    transform: none;
  }
  .paper-poster {
    padding: 22px 10px 10px;
  }
  .poster-title {
    font-size: 15px;
    min-height: 2.6em;
    -webkit-line-clamp: 2;
  }
  .poster-seal {
    width: 28px;
    height: 28px;
    font-size: 13px;
    right: 10px;
    bottom: 12px;
  }
  .toolbar {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
  }
  .toolbar :deep(.el-radio-group) {
    display: flex;
    width: 100%;
  }
  .toolbar :deep(.el-radio-button) {
    flex: 1;
    min-width: 0;
  }
  .toolbar :deep(.el-radio-button__inner) {
    width: 100%;
    padding: 8px 4px;
    font-size: 13px;
  }
  .filters {
    width: 100%;
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 8px;
  }
  .filter-district,
  .filter-keyword {
    width: 100% !important;
    min-width: 0;
    grid-column: span 1;
  }
  .filter-btn,
  .filters > a {
    grid-column: 1 / -1;
    width: 100%;
  }
  .filters > a .filter-btn,
  .filters .filter-btn {
    width: 100%;
  }
  .pager {
    margin-top: 16px;
    overflow-x: auto;
  }
}

@media (max-width: 360px) {
  .jinbang-grid {
    grid-template-columns: 1fr;
  }
}
</style>
