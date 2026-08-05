<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { sendSms, validateInvite } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()
const sending = ref(false)
const inviteHint = ref('')
const form = reactive({
  inviteCode: '',
  phone: '',
  smsCode: '',
  username: '',
  password: '',
  nickname: '',
})

onMounted(async () => {
  const code = String(route.params.code || route.query.invite || '')
  if (code) {
    form.inviteCode = code
    await checkInvite()
  }
})

async function checkInvite() {
  if (!form.inviteCode) return
  const res = await validateInvite(form.inviteCode)
  inviteHint.value = res.valid
    ? `邀请有效${res.inviterNickname ? ` · 邀请人：${res.inviterNickname}` : ''}`
    : '邀请码无效'
}

async function onSendSms() {
  if (!form.phone) return ElMessage.warning('请填写手机号')
  sending.value = true
  try {
    await sendSms(form.phone, 'REGISTER')
    ElMessage.success('验证码已发送（Mock 见后端日志）')
  } finally {
    sending.value = false
  }
}

async function onSubmit() {
  await auth.register({ ...form })
  ElMessage.success('注册成功，欢迎入江湖')
  router.replace('/')
}
</script>

<template>
  <div class="auth-page">
    <div class="panel">
      <h1 class="brand-title">持令入江湖</h1>
      <p class="slogan">MVP 仅邀请注册 · 遵义试点</p>
      <el-form label-position="top" @submit.prevent="onSubmit">
        <el-form-item label="邀请码">
          <el-input v-model="form.inviteCode" @blur="checkInvite" />
          <div class="hint">{{ inviteHint }}</div>
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" placeholder="如：遵义某侠" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" maxlength="11" />
        </el-form-item>
        <el-form-item label="验证码">
          <div class="row">
            <el-input v-model="form.smsCode" />
            <el-button :loading="sending" @click="onSendSms">获取验证码</el-button>
          </div>
        </el-form-item>
        <el-form-item label="用户名（可选，便于密码登录）">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码（可选）">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-button class="jh-btn-seal" type="primary" native-type="submit" :loading="auth.loading" style="width: 100%">
          注册并登录
        </el-button>
      </el-form>
      <p class="foot">
        已有身份？
        <RouterLink to="/login">去登录</RouterLink>
      </p>
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  min-height: 100vh;
  min-height: 100dvh;
  display: grid;
  place-items: center;
  padding: max(16px, env(safe-area-inset-top)) 16px max(16px, env(safe-area-inset-bottom));
  background: var(--jh-paper);
}
.panel {
  width: min(460px, 100%);
  background: #fff;
  border: 1px solid var(--jh-line);
  border-radius: var(--jh-radius);
  padding: 28px 24px 22px;
}
.brand-title {
  margin: 0;
  font-size: clamp(28px, 8vw, 34px);
  text-align: center;
}
.slogan,
.hint,
.foot {
  text-align: center;
  color: var(--jh-muted);
}
.slogan {
  margin: 4px 0 16px;
}
.hint {
  font-size: 12px;
  margin-top: 4px;
  text-align: left;
}
.row {
  display: flex;
  gap: 8px;
  width: 100%;
}
.foot {
  margin-top: 16px;
}
.foot a {
  color: var(--jh-seal);
}
@media (max-width: 480px) {
  .panel {
    padding: 22px 16px 18px;
  }
  .row {
    flex-direction: column;
  }
}
</style>
