<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { applyLord, getMyLordApplication, getMyRank, getRanks } from '@/api/rank'
import type { RankType } from '@/types/api'
import type { RankItem } from '@/types/models'
import { useAuthStore } from '@/stores/auth'
import JhPageHeader from '@/components/JhPageHeader.vue'

const auth = useAuthStore()
const type = ref<RankType>('REPUTATION')
const list = ref<RankItem[]>([])
const lord = ref<{ userId: number; nickname: string } | null>(null)
const myRank = ref<Record<string, unknown> | null>(null)
const lordApp = ref<Record<string, unknown> | null>(null)
const statement = ref('')

async function load() {
  const data = await getRanks(type.value, { page: 1, pageSize: 50 })
  list.value = data.list || []
  lord.value = data.lord || null
  if (auth.isLoggedIn) {
    ;[myRank.value, lordApp.value] = await Promise.all([
      getMyRank().catch(() => null),
      getMyLordApplication().catch(() => null),
    ])
  }
}

async function onApplyLord() {
  await applyLord(statement.value || '愿行侠仗义，护航同城互助。')
  ElMessage.success('盟主申请已提交')
  lordApp.value = await getMyLordApplication().catch(() => null)
}

onMounted(load)
</script>

<template>
  <section class="jh-section">
    <div class="jh-container">
      <JhPageHeader title="英雄榜" subtitle="声望与侠义排行" />
      <div v-if="lord" class="lord jh-panel">
        <span>武林盟主荣耀位</span>
        <strong>{{ lord.nickname }}</strong>
      </div>
      <el-radio-group v-model="type" class="tabs" @change="load">
        <el-radio-button value="REPUTATION">声望榜</el-radio-button>
        <el-radio-button value="CHIVALRY">侠义榜</el-radio-button>
        <el-radio-button value="COMPLETED">完令榜</el-radio-button>
      </el-radio-group>
      <div v-if="myRank" class="mine jh-panel">
        我的排名：第 {{ myRank.rank ?? myRank[type] ?? '-' }} 名
        <span v-if="myRank.score != null"> · 分值 {{ myRank.score }}</span>
        <span v-if="myRank.reputationRank != null"> · 声望榜 #{{ myRank.reputationRank }}</span>
        <span v-if="myRank.chivalryRank != null"> · 侠义榜 #{{ myRank.chivalryRank }}</span>
        <span v-if="myRank.completedRank != null"> · 完令榜 #{{ myRank.completedRank }}</span>
      </div>
      <el-table :data="list" class="jh-panel">
        <el-table-column prop="rank" label="排名" width="80" />
        <el-table-column prop="nickname" label="侠士" />
        <el-table-column prop="levelTitle" label="头衔" />
        <el-table-column prop="score" label="分值" />
      </el-table>

      <div v-if="auth.isLoggedIn" class="jh-panel apply">
        <h2>申请武林盟主</h2>
        <p class="jh-muted">默认需声望榜第 1；是否任命由武林盟审批</p>
        <p v-if="lordApp" class="status">
          当前申请状态：{{ lordApp.status || '无' }}
          <span v-if="lordApp.rejectReason"> · {{ lordApp.rejectReason }}</span>
        </p>
        <el-input v-model="statement" type="textarea" :rows="2" placeholder="申请陈述" />
        <el-button type="primary" class="jh-btn-seal" style="margin-top: 8px" @click="onApplyLord">
          提交申请
        </el-button>
      </div>
    </div>
  </section>
</template>

<style scoped>
h1 {
  margin: 0 0 12px;
  font-size: 32px;
}
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
  margin-bottom: 12px;
}
.mine {
  padding: 12px 16px;
  margin-bottom: 12px;
  font-size: 14px;
}
.apply {
  margin-top: 16px;
  padding: 16px;
}
.status {
  color: var(--jh-muted);
  margin: 0 0 8px;
}
</style>
