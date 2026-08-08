<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useMineAttentionStore } from '@/stores/mineAttention'
import { difficultyLabel, formatAmount, mineBountySortRank, resolveBountyTypeLabel } from '@/utils/labels'
import StatusTag from '@/components/StatusTag.vue'
import EmptyState from '@/components/EmptyState.vue'
import JhPageHeader from '@/components/JhPageHeader.vue'
import JhBoardPoster from '@/components/JhBoardPoster.vue'
import JhBoardPosterGrid from '@/components/JhBoardPosterGrid.vue'
import '@/components/JhBoardPosterMeta.css'

const router = useRouter()
const mineAttention = useMineAttentionStore()
const { published, claimed } = storeToRefs(mineAttention)

const tab = ref('published')
const loading = ref(false)

const sourceList = computed(() => (tab.value === 'published' ? published.value : claimed.value))

const sortedList = computed(() => {
  return [...sourceList.value].sort((a, b) => {
    const d = mineBountySortRank(a.status) - mineBountySortRank(b.status)
    if (d !== 0) return d
    return Number(b.id) - Number(a.id)
  })
})

async function load() {
  loading.value = true
  try {
    await mineAttention.refresh()
  } finally {
    loading.value = false
  }
}

function goRepublish(id: number) {
  router.push({ path: '/bounties/publish', query: { republishFrom: String(id) } })
}

function bountyLink(id: number) {
  return { path: `/bounties/${id}`, query: { from: 'mine' } }
}

function unreadLabel(n?: number | null) {
  if (!n || n <= 0) return ''
  return n > 99 ? '99+' : String(n)
}

onMounted(() => {
  void load()
})
</script>

<template>
  <section class="jh-section">
    <div class="jh-container">
      <JhPageHeader title="我的悬赏" />
      <el-tabs v-model="tab" class="tabs">
        <el-tab-pane label="我发布的" name="published" />
        <el-tab-pane label="我揭榜的" name="claimed" />
      </el-tabs>

      <JhBoardPosterGrid :loading="loading && !sortedList.length" :empty="!loading && !sortedList.length">
        <template #empty>
          <EmptyState title="暂无记录" description="" />
        </template>
        <JhBoardPoster
          v-for="(item, index) in sortedList"
          :key="item.id"
          :to="bountyLink(item.id)"
          :title="item.title"
          :index="index"
          :show-dot="mineAttention.isPosterHot(item.id)"
          seal="令"
        >
          <template #top>
            <span class="jh-poster-type">{{ resolveBountyTypeLabel(item.type, item.typeDisplayName) }}</span>
            <StatusTag :status="item.status" scene="mine" />
          </template>
          <p>{{ item.district || '遵义' }} · {{ difficultyLabel[item.difficulty] || item.difficulty }}</p>
          <p>揭榜 {{ item.claimCount || 0 }} 人</p>
          <div class="jh-poster-stats">
            <span
              v-if="item.unreadCollabCount && item.unreadCollabCount > 0"
              class="jh-poster-badge is-hot"
              title="协作会话未读"
            >
              会话 {{ unreadLabel(item.unreadCollabCount) }}
            </span>
            <span class="jh-poster-badge" title="已提交成果数">
              成果 {{ item.submissionCount ?? 0 }}
            </span>
          </div>
          <template #bottom>
            <strong class="jh-poster-reward">赏银 {{ formatAmount(item.rewardAmount) }} 两</strong>
            <span class="jh-poster-deadline">截止 {{ item.deadlineAt?.slice(0, 10) }}</span>
          </template>
          <template v-if="tab === 'published' && item.canRepublish" #actions>
            <el-button size="small" type="primary" class="jh-btn-seal" @click="goRepublish(item.id)">
              再发一令
            </el-button>
          </template>
        </JhBoardPoster>
      </JhBoardPosterGrid>
    </div>
  </section>
</template>

<style scoped>
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
</style>
