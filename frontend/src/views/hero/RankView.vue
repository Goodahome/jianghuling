<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { applyLord, getMyLordApplication, getMyRank, getRanks } from '@/api/rank'
import type { RankType } from '@/types/api'
import type { RankItem } from '@/types/models'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const type = ref<RankType>('REPUTATION')
const list = ref<RankItem[]>([])
const lord = ref<{ userId: number; nickname: string } | null>(null)
const myRank = ref<Record<string, unknown> | null>(null)
const statement = ref('')

async function load() {
  const data = await getRanks(type.value, { page: 1, pageSize: 50 })
  list.value = data.list || []
  lord.value = data.lord || null
  if (auth.isLoggedIn) myRank.value = await getMyRank().catch(() => null)
}

async function onApplyLord() {
  await applyLord(statement.value || '愿行侠仗义，护航同城互助。')
  ElMessage.success('盟主申请已提交')
  await getMyLordApplication()
}

onMounted(load)
</script>

<template>
  <section class="jh-section">
    <div class="jh-container">
      <h1 class="brand-title">英雄谱</h1>
      <div v-if="lord" class="lord jh-panel">
        <span>武林盟主荣耀位</span>
        <strong>{{ lord.nickname }}</strong>
      </div>
      <el-radio-group v-model="type" class="tabs" @change="load">
        <el-radio-button value="REPUTATION">声望榜</el-radio-button>
        <el-radio-button value="CHIVALRY">侠义榜</el-radio-button>
        <el-radio-button value="COMPLETED">完令榜</el-radio-button>
      </el-radio-group>
      <p v-if="myRank" class="jh-muted">我的排名信息已加载</p>
      <el-table :data="list" class="jh-panel">
        <el-table-column prop="rank" label="排名" width="80" />
        <el-table-column prop="nickname" label="侠士" />
        <el-table-column prop="levelTitle" label="头衔" />
        <el-table-column prop="score" label="分值" />
      </el-table>

      <div v-if="auth.isLoggedIn" class="jh-panel apply">
        <h2>申请武林盟主</h2>
        <p class="jh-muted">默认需声望榜第 1；是否任命由武林盟审批</p>
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
.apply {
  margin-top: 16px;
  padding: 16px;
}
</style>
