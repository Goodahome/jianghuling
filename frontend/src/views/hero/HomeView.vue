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

  <section class="jh-section">
    <div class="jh-container">
      <div v-if="tops.length" class="notice-strip jh-panel">
        <strong>防骗置顶</strong>
        <RouterLink v-for="n in tops" :key="n.id" :to="`/notices/${n.id}`">{{ n.title }}</RouterLink>
      </div>

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
            placeholder="搜索标题"
            clearable
            @keyup.enter="query.page = 1; load()"
          />
          <el-button type="primary" class="jh-btn-seal filter-btn" @click="query.page = 1; load()">筛选</el-button>
        </div>
      </div>

      <div v-loading="loading" class="grid">
        <EmptyState
          v-if="!loading && !list.length"
          title="广场暂无张贴"
          description="成为令主，发布第一条租房悬赏吧。"
        >
          <RouterLink to="/bounties/publish"><el-button type="primary" class="jh-btn-seal">去发令</el-button></RouterLink>
        </EmptyState>
        <RouterLink
          v-for="item in list"
          :key="item.id"
          :to="`/bounties/${item.id}`"
          class="bounty-item jh-panel"
        >
          <div class="top">
            <span class="type">{{ bountyTypeLabel[item.type] }}</span>
            <StatusTag :status="item.status" />
          </div>
          <h3>{{ item.title }}</h3>
          <p class="meta">
            {{ item.district || '遵义' }} · {{ difficultyLabel[item.difficulty] }} · 揭榜 {{ item.claimCount || 0 }} 人
          </p>
          <div class="bottom">
            <strong>{{ formatAmount(item.rewardAmount) }} 两</strong>
            <span class="jh-muted">截止 {{ item.deadlineAt?.slice(0, 10) }}</span>
          </div>
        </RouterLink>
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
  </section>
</template>

<style scoped>
.hero-band {
  min-height: auto;
  display: flex;
  align-items: center;
  background: var(--jh-ink);
  color: #fff;
  padding: 40px clamp(32px, 10vw, 120px);
}
.band-inner {
  width: 100%;
  max-width: 1120px;
  margin: 0 auto;
  padding-inline: 0;
}
.copy {
  max-width: 36em;
  margin-inline: clamp(8px, 3vw, 40px) auto;
}
.brand-title {
  margin: 0;
  font-size: clamp(48px, 8vw, 84px);
  line-height: 1.05;
  animation: rise 0.8s ease both;
}
.slogan {
  margin: 14px 0 8px;
  font-family: var(--jh-font-display);
  font-size: clamp(20px, 3.2vw, 28px);
  letter-spacing: 0.06em;
  line-height: 1.4;
  animation: rise 0.9s ease 0.08s both;
}
.belief {
  margin: 0 0 22px;
  font-size: 15px;
  line-height: 1.7;
  opacity: 0.82;
  animation: rise 0.95s ease 0.12s both;
}
.cta-row {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  animation: rise 1s ease 0.16s both;
}
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 12px 18px;
  min-height: 44px;
  border-radius: 999px;
  border: 1px solid transparent;
}
.btn.primary {
  background: var(--jh-seal);
  color: #fff;
}
.btn.ghost {
  border-color: rgba(255, 255, 255, 0.45);
}
.notice-strip {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 18px;
  padding: 14px 18px;
  margin-bottom: 20px;
  align-items: center;
}
.notice-strip a {
  color: var(--jh-seal);
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
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 14px;
  min-height: 160px;
}
.bounty-item {
  padding: 16px;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.bounty-item:hover {
  transform: translateY(-2px);
}
.top {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}
.type {
  color: var(--jh-seal);
  font-size: 13px;
}
h3 {
  margin: 0 0 8px;
  font-size: 18px;
  line-height: 1.4;
}
.meta,
.bottom {
  font-size: 13px;
}
.bottom {
  display: flex;
  justify-content: space-between;
  margin-top: 14px;
  align-items: baseline;
}
.bottom strong {
  color: var(--jh-seal);
  font-size: 18px;
}
.pager {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
@keyframes rise {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: none;
  }
}

@media (max-width: 768px) {
  .hero-band {
    padding: 28px 20px;
  }
  .copy {
    margin-inline: 0;
  }
  .brand-title {
    font-size: clamp(40px, 12vw, 56px);
  }
  .slogan {
    font-size: clamp(18px, 5vw, 22px);
    margin: 12px 0 6px;
  }
  .belief {
    font-size: 14px;
    margin-bottom: 18px;
  }
  .cta-row {
    flex-direction: column;
  }
  .btn {
    width: 100%;
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
  .grid {
    grid-template-columns: 1fr;
    gap: 10px;
  }
  .bounty-item:hover {
    transform: none;
  }
}
</style>
