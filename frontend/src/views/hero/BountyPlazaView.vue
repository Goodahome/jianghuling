<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { listBounties } from '@/api/bounty'
import { getTopNotices } from '@/api/notice'
import type { BountyListItem, Notice } from '@/types/models'
import {
  difficultyLabel,
  formatAmount,
  resolveBountyTypeLabel,
} from '@/utils/labels'
import StatusTag from '@/components/StatusTag.vue'
import EmptyState from '@/components/EmptyState.vue'
import JhPageHeader from '@/components/JhPageHeader.vue'
import JhBoardPoster from '@/components/JhBoardPoster.vue'
import JhBoardPosterGrid from '@/components/JhBoardPosterGrid.vue'
import '@/components/JhBoardPosterMeta.css'

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
  type: '' as '' | 'RENT_SEEK' | 'RENT_OUT' | 'RENT_TRANSFER',
  keyword: '',
  district: '',
})

/** 与「我的悬赏」同款 el-tabs；全部用 ALL，请求时再映射为空 */
const typeTab = ref('ALL')

function onTypeTab(name: string | number) {
  const key = String(name)
  query.type = (key === 'ALL' ? '' : key) as typeof query.type
  query.page = 1
  void load()
}

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
        <JhPageHeader title="悬赏榜" />

        <el-tabs v-model="typeTab" class="tabs" @tab-change="onTypeTab">
          <el-tab-pane label="全部" name="ALL" />
          <el-tab-pane label="租房悬赏" name="RENT_SEEK" />
          <el-tab-pane label="出租悬赏" name="RENT_OUT" />
          <el-tab-pane label="转租悬赏" name="RENT_TRANSFER" />
        </el-tabs>

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

        <JhBoardPosterGrid :loading="loading" :empty="!loading && !list.length">
          <template #empty>
            <EmptyState
              title="金榜暂虚位以待"
              description="尚未有人张贴新令，可先发一榜。"
            >
              <RouterLink to="/bounties/publish">
                <el-button type="primary" class="jh-btn-seal">去张贴</el-button>
              </RouterLink>
            </EmptyState>
          </template>
          <JhBoardPoster
            v-for="(item, index) in list"
            :key="item.id"
            :to="`/bounties/${item.id}?from=plaza`"
            :title="item.title"
            :index="index"
            seal="令"
          >
            <template #top>
              <span class="jh-poster-type">{{ resolveBountyTypeLabel(item.type, item.typeDisplayName) }}</span>
              <StatusTag :status="item.status" scene="plaza" />
            </template>
            <p>{{ item.district || '遵义' }} · {{ difficultyLabel[item.difficulty] }}</p>
            <p>揭榜 {{ item.claimCount || 0 }} 人</p>
            <template #bottom>
              <strong class="jh-poster-reward">赏银 {{ formatAmount(item.rewardAmount) }} 两</strong>
              <span class="jh-poster-deadline">截止 {{ item.deadlineAt?.slice(0, 10) }}</span>
            </template>
          </JhBoardPoster>
        </JhBoardPosterGrid>

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
.filters {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 18px;
}
.filter-district {
  width: 120px;
}
.filter-keyword {
  width: 180px;
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
  .tabs :deep(.el-tabs__item) {
    padding: 0 12px;
    font-size: 14px;
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

</style>
