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
const agreed = ref(false)
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
  if (!agreed.value) {
    ElMessage.warning('请先阅读并同意《用户服务协议》和《隐私政策》')
    return
  }
  await auth.register({ ...form })
  ElMessage.success('注册成功，欢迎入江湖')
  router.replace('/')
}
</script>

<template>
  <div class="auth-page">
    <div class="panel">
      <h1 class="brand-title">初入江湖</h1>
      <p class="slogan">MVP 仅邀请注册 · 内测中</p>
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
        <div class="agree-row">
          <el-checkbox v-model="agreed" />
          <span class="agree-text">
            我已阅读并同意
            <RouterLink to="/legal/user-agreement" target="_blank">《用户服务协议》</RouterLink>
            和
            <RouterLink to="/legal/privacy" target="_blank">《隐私政策》</RouterLink>
          </span>
        </div>
        <el-button
          class="jh-btn-seal"
          type="primary"
          native-type="submit"
          :loading="auth.loading"
          :disabled="!agreed"
          style="width: 100%"
        >
          初入江湖
        </el-button>
      </el-form>
      <p class="foot">
        已有身份？
        <RouterLink to="/login">踏入江湖</RouterLink>
      </p>
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  min-height: min(70vh, 720px);
  display: grid;
  place-items: center;
  padding: 24px 16px 32px;
  background: transparent;
}
.panel {
  width: min(460px, 100%);
  background: rgba(42, 34, 24, 0.12);
  border: 1px solid rgba(196, 163, 90, 0.35);
  border-radius: var(--jh-radius);
  padding: 28px 24px 22px;
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
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
  color: rgba(247, 240, 221, 0.78);
}
.slogan {
  margin: 4px 0 16px;
}
.hint {
  font-size: 12px;
  margin-top: 4px;
  text-align: left;
}
.panel :deep(.el-form-item__label) {
  color: rgba(247, 240, 221, 0.85);
}
.panel :deep(.el-input__wrapper) {
  background: transparent;
  box-shadow: 0 0 0 1px rgba(196, 163, 90, 0.45) inset;
}
.panel :deep(.el-input__wrapper:hover),
.panel :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--jh-gold) inset;
}
.panel :deep(.el-input__inner) {
  color: #f7f0dd;
}
.panel :deep(.el-input__inner::placeholder) {
  color: rgba(247, 240, 221, 0.45);
}
.row {
  display: flex;
  gap: 8px;
  width: 100%;
}
.agree-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin: 4px 0 16px;
}
.agree-text {
  flex: 1;
  font-size: 13px;
  line-height: 1.55;
  color: rgba(247, 240, 221, 0.82);
  padding-top: 2px;
}
.agree-text a {
  color: var(--jh-gold-bright);
  text-decoration: underline;
  text-underline-offset: 2px;
}
.agree-row :deep(.el-checkbox__label) {
  display: none;
}
.foot {
  margin-top: 16px;
}
.foot a {
  color: var(--jh-gold-bright);
}
@media (max-width: 480px) {
  .auth-page {
    min-height: auto;
    padding: 16px 10px calc(28px + env(safe-area-inset-bottom));
    width: 100%;
    max-width: 100%;
    box-sizing: border-box;
  }
  .panel {
    width: 100%;
    max-width: 100%;
    padding: 20px 14px 16px;
    box-sizing: border-box;
  }
  .row {
    flex-direction: column;
  }
  .row .el-button {
    width: 100%;
  }
}
</style>
