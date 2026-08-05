<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { submitRealName, updateProfile } from '@/api/user'
import { getLevelProgress } from '@/api/growth'
import { useAuthStore } from '@/stores/auth'
import type { LevelProgress } from '@/types/models'
import { formatAmount } from '@/utils/labels'
import ImageUpload from '@/components/ImageUpload.vue'

const auth = useAuthStore()
const level = ref<LevelProgress | null>(null)
const form = reactive({ nickname: '', bio: '', avatarUrl: '' })
const realName = reactive({ realName: '', idNumber: '' })
const avatarUrls = ref<string[]>([])

onMounted(async () => {
  await auth.fetchMe()
  form.nickname = auth.me?.nickname || ''
  form.bio = auth.me?.bio || ''
  form.avatarUrl = auth.me?.avatarUrl || ''
  avatarUrls.value = form.avatarUrl ? [form.avatarUrl] : []
  level.value = await getLevelProgress().catch(() => null)
})

async function saveProfile() {
  form.avatarUrl = avatarUrls.value[0] || ''
  await updateProfile(form)
  await auth.fetchMe()
  ElMessage.success('资料已更新')
}

async function saveRealName() {
  const res = await submitRealName(realName)
  ElMessage.success(`实名已提交：${res.status}`)
  await auth.fetchMe()
}
</script>

<template>
  <section class="jh-section">
    <div class="jh-container narrow">
      <h1 class="brand-title">侠士资料</h1>
      <div class="jh-panel block">
        <p>
          等级：{{ level?.levelTitle || auth.me?.levelTitle }} · 侠义
          {{ formatAmount(auth.me?.chivalry) }} · 体力 {{ auth.me?.stamina }} · 今日揭榜
          {{ auth.me?.claimCountToday }}/{{ auth.me?.claimLimitToday }}
        </p>
        <el-progress
          v-if="level"
          :percentage="Math.round((level.progress || 0) * 100)"
          :stroke-width="10"
        />
        <p class="jh-muted">
          声望 {{ auth.me?.reputationScore }} · 完成单 {{ auth.me?.completedOrders }} · 好评率
          {{ ((auth.me?.goodRate || 0) * 100).toFixed(1) }}%
          <span v-if="auth.me?.isLord"> · 现任武林盟主</span>
        </p>
        <div class="links">
          <RouterLink to="/growth">成长兑换</RouterLink>
          <RouterLink to="/invites">邀请同道</RouterLink>
          <RouterLink to="/offices">职司申请</RouterLink>
          <RouterLink to="/disputes">我的纠纷</RouterLink>
        </div>
      </div>

      <el-form class="jh-panel block" label-position="top" @submit.prevent="saveProfile">
        <h2>基础资料</h2>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.bio" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="头像">
          <ImageUpload v-model="avatarUrls" :limit="1" tip="上传一张头像图" />
        </el-form-item>
        <el-button type="primary" class="jh-btn-seal" native-type="submit">保存</el-button>
      </el-form>

      <el-form class="jh-panel block" label-position="top" @submit.prevent="saveRealName">
        <h2>实名认证（非硬门槛）</h2>
        <p class="jh-muted">当前状态：{{ auth.me?.realNameStatus || 'NONE' }}</p>
        <el-form-item label="真实姓名">
          <el-input v-model="realName.realName" />
        </el-form-item>
        <el-form-item label="证件号">
          <el-input v-model="realName.idNumber" />
        </el-form-item>
        <el-button native-type="submit">提交实名</el-button>
      </el-form>
    </div>
  </section>
</template>

<style scoped>
.narrow {
  max-width: 720px;
}
h1 {
  margin: 0 0 14px;
  font-size: 32px;
}
.block {
  padding: 18px;
  margin-bottom: 14px;
}
h2 {
  margin: 0 0 12px;
  font-family: var(--jh-font-display);
}
.links {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 12px;
}
.links a {
  color: var(--jh-seal);
}
</style>
