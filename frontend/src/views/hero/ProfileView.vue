<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { submitRealName, updateProfile } from '@/api/user'
import { getLevelProgress } from '@/api/growth'
import { applyLord, getMyLordApplication } from '@/api/rank'
import { useAuthStore } from '@/stores/auth'
import type { LevelProgress } from '@/types/models'
import { formatAmount } from '@/utils/labels'
import ImageUpload from '@/components/ImageUpload.vue'
import JhPageHeader from '@/components/JhPageHeader.vue'

const auth = useAuthStore()
const level = ref<LevelProgress | null>(null)
const form = reactive({ nickname: '', bio: '', avatarUrl: '' })
const realName = reactive({ realName: '', idNumber: '' })
const avatarUrls = ref<string[]>([])
const lordApp = ref<Record<string, unknown> | null>(null)
const lordStatement = ref('')
const lordSubmitting = ref(false)

onMounted(async () => {
  await auth.fetchMe()
  form.nickname = auth.me?.nickname || ''
  form.bio = auth.me?.bio || ''
  form.avatarUrl = auth.me?.avatarUrl || ''
  avatarUrls.value = form.avatarUrl ? [form.avatarUrl] : []
  level.value = await getLevelProgress().catch(() => null)
  lordApp.value = await getMyLordApplication().catch(() => null)
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

async function onApplyLord() {
  lordSubmitting.value = true
  try {
    await applyLord(lordStatement.value || '愿行侠仗义，护航同城互助。')
    ElMessage.success('盟主申请已提交')
    lordApp.value = await getMyLordApplication().catch(() => null)
  } finally {
    lordSubmitting.value = false
  }
}
</script>

<template>
  <section class="jh-section">
    <div class="jh-container narrow">
      <JhPageHeader title="侠士资料" />
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
          <RouterLink to="/feedbacks">意见反馈</RouterLink>
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

      <div class="jh-panel block lord-apply">
        <h2>申请武林盟主</h2>
        <p class="jh-muted">默认需声望榜第 1；是否任命由武林盟审批。现任盟主荣耀位见英雄榜。</p>
        <p v-if="auth.me?.isLord" class="status ok">你已是现任武林盟主。</p>
        <template v-else>
          <p v-if="lordApp" class="status">
            当前申请状态：{{ lordApp.status || '无' }}
            <span v-if="lordApp.rejectReason"> · {{ lordApp.rejectReason }}</span>
          </p>
          <el-input
            v-model="lordStatement"
            type="textarea"
            :rows="3"
            placeholder="申请陈述（可选）"
          />
          <el-button
            type="primary"
            class="jh-btn-seal"
            style="margin-top: 10px"
            :loading="lordSubmitting"
            @click="onApplyLord"
          >
            提交申请
          </el-button>
        </template>
      </div>
    </div>
  </section>
</template>

<style scoped>
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
.status {
  color: var(--jh-muted);
  margin: 0 0 8px;
}
.status.ok {
  color: var(--jh-ok);
}
</style>
