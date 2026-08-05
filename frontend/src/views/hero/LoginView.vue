<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { sendSms } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'
import type { LoginType } from '@/types/api'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()
const loginType = ref<LoginType>('PASSWORD')
const sending = ref(false)
const form = reactive({
  username: '',
  password: '',
  phone: '',
  smsCode: '',
})

async function onSendSms() {
  if (!form.phone) return ElMessage.warning('请填写手机号')
  sending.value = true
  try {
    await sendSms(form.phone, 'LOGIN')
    ElMessage.success('验证码已发送（Mock 见后端日志）')
  } finally {
    sending.value = false
  }
}

async function onSubmit() {
  await auth.login({
    loginType: loginType.value,
    username: form.username,
    password: form.password,
    phone: form.phone,
    smsCode: form.smsCode,
  })
  ElMessage.success('登录成功')
  router.replace(String(route.query.redirect || '/'))
}
</script>

<template>
  <div class="auth-page">
    <div class="panel">
      <h1 class="brand-title">江湖令</h1>
      <p class="slogan">天下有悬赏，江湖有侠士。</p>
      <el-tabs v-model="loginType">
        <el-tab-pane label="账号密码" name="PASSWORD" />
        <el-tab-pane label="手机验证码" name="SMS" />
      </el-tabs>
      <el-form label-position="top" @submit.prevent="onSubmit">
        <template v-if="loginType === 'PASSWORD'">
          <el-form-item label="账号">
            <el-input v-model="form.username" placeholder="用户名" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="form.password" type="password" show-password />
          </el-form-item>
        </template>
        <template v-else>
          <el-form-item label="手机号">
            <el-input v-model="form.phone" maxlength="11" />
          </el-form-item>
          <el-form-item label="验证码">
            <div class="row">
              <el-input v-model="form.smsCode" />
              <el-button :loading="sending" @click="onSendSms">获取验证码</el-button>
            </div>
          </el-form-item>
        </template>
        <el-button class="jh-btn-seal" type="primary" native-type="submit" :loading="auth.loading" style="width: 100%">
          重出江湖
        </el-button>
      </el-form>
      <p class="foot">
        尚无身份？
        <RouterLink to="/register">持邀请码注册</RouterLink>
      </p>
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  min-height: min(70vh, 640px);
  display: grid;
  place-items: center;
  padding: 24px 16px 32px;
  background: transparent;
}
.panel {
  width: min(420px, 100%);
  background: rgba(42, 34, 24, 0.12);
  border: 1px solid rgba(196, 163, 90, 0.35);
  border-radius: var(--jh-radius);
  padding: 28px 24px 22px;
  box-shadow: none;
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
}
.brand-title {
  margin: 0;
  font-size: clamp(32px, 10vw, 40px);
  text-align: center;
}
.slogan {
  text-align: center;
  color: rgba(247, 240, 221, 0.78);
  margin: 4px 0 18px;
}
.panel :deep(.el-form-item__label) {
  color: rgba(247, 240, 221, 0.85);
}
.panel :deep(.el-input__wrapper) {
  background: rgba(255, 253, 246, 0.2);
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
.panel :deep(.el-tabs__item) {
  color: rgba(247, 240, 221, 0.72);
}
.panel :deep(.el-tabs__item.is-active),
.panel :deep(.el-tabs__item:hover) {
  color: var(--jh-gold-bright);
}
.row {
  display: flex;
  gap: 8px;
  width: 100%;
}
.row .el-button {
  flex-shrink: 0;
}
.foot {
  margin-top: 16px;
  text-align: center;
  color: rgba(247, 240, 221, 0.72);
}
.foot a {
  color: var(--jh-gold-bright);
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
