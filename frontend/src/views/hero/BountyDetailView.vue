<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cancelBounty, claimBounty, getBounty } from '@/api/bounty'
import { useAuthStore } from '@/stores/auth'
import type { BountyDetail } from '@/types/models'
import {
  bountyTypeLabel,
  difficultyLabel,
  formatAmount,
} from '@/utils/labels'
import StatusTag from '@/components/StatusTag.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const claiming = ref(false)
const detail = ref<BountyDetail | null>(null)

const canClaim = computed(() => {
  if (!detail.value || !auth.isLoggedIn) return false
  if (detail.value.isPublisher || detail.value.claimedByMe) return false
  return ['OPEN', 'IN_COLLAB'].includes(detail.value.status)
})

async function load() {
  loading.value = true
  try {
    detail.value = await getBounty(route.params.id as string)
  } finally {
    loading.value = false
  }
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
  const { value } = await ElMessageBox.prompt('请填写取消原因', '取消悬赏')
  await cancelBounty(route.params.id as string, value || '取消')
  ElMessage.success('已取消，托管赏银将退回')
  await load()
}

onMounted(load)
</script>

<template>
  <section class="jh-section" v-loading="loading">
    <div class="jh-container" v-if="detail">
      <div class="head jh-panel">
        <div class="tags">
          <span>{{ bountyTypeLabel[detail.type] }}</span>
          <StatusTag :status="detail.status" />
          <span>{{ difficultyLabel[detail.difficulty] }}</span>
        </div>
        <h1>{{ detail.title }}</h1>
        <p class="reward">赏银 {{ formatAmount(detail.rewardAmount) }} 两 · 揭榜 {{ detail.claimCount }} 人</p>
        <p class="jh-muted">
          {{ detail.district || '遵义' }} · 截止 {{ detail.deadlineAt }}
        </p>
        <div class="actions">
          <el-button v-if="canClaim" type="primary" class="jh-btn-seal action-main" :loading="claiming" @click="onClaim">
            一键揭榜
          </el-button>
          <el-button
            v-if="detail.claimedByMe || detail.isPublisher"
            class="action-item"
            @click="router.push(`/bounties/${detail.id}/chat`)"
          >
            协作会话
          </el-button>
          <el-button
            v-if="detail.claimedByMe"
            type="success"
            class="action-item"
            @click="router.push(`/bounties/${detail.id}/submit`)"
          >
            提交成果
          </el-button>
          <el-button
            v-if="detail.isPublisher && ['IN_COLLAB', 'PENDING_SETTLE'].includes(detail.status)"
            type="warning"
            class="action-item"
            @click="router.push(`/bounties/${detail.id}/settle`)"
          >
            完结分配
          </el-button>
          <el-button
            v-if="detail.isPublisher && ['OPEN', 'PENDING_REVIEW', 'IN_COLLAB'].includes(detail.status)"
            class="action-item"
            @click="onCancel"
          >
            取消悬赏
          </el-button>
        </div>
      </div>

      <div class="cols">
        <div class="jh-panel block">
          <h2>租房令状</h2>
          <el-descriptions :column="1" border>
            <el-descriptions-item
              v-for="(val, key) in detail.warrantFields || {}"
              :key="key"
              :label="String(key)"
            >
              {{ val === true ? '是' : val === false ? '否' : val }}
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
  gap: 8px;
  align-items: center;
  margin-bottom: 8px;
  color: var(--jh-seal);
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
    background: rgba(255, 255, 255, 0.96);
    border: 1px solid var(--jh-line);
    border-radius: 12px;
    box-shadow: 0 -4px 20px rgba(28, 36, 48, 0.08);
  }
  .action-main,
  .action-item {
    flex: 1 1 calc(50% - 8px);
    margin: 0 !important;
  }
  .action-main {
    flex-basis: 100%;
  }
}
</style>
