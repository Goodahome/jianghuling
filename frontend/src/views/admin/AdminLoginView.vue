<script setup lang="ts">
import { reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAdminAuthStore } from '@/stores/adminAuth'

const adminAuth = useAdminAuthStore()
const router = useRouter()
const route = useRoute()
const form = reactive({ username: '', password: '' })

async function onSubmit() {
  await adminAuth.login(form.username, form.password)
  ElMessage.success('登录成功')
  router.replace(String(route.query.redirect || '/admin'))
}
</script>

<template>
  <div class="wrap">
    <el-card class="card">
      <h2>武林盟后台登录</h2>
      <el-form label-position="top" @submit.prevent="onSubmit">
        <el-form-item label="管理员账号">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-button type="primary" native-type="submit" :loading="adminAuth.loading" style="width: 100%">
          登录
        </el-button>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.wrap {
  min-height: 100vh;
  display: grid;
  place-items: center;
  background: #f4f4f5;
  padding: 16px;
}
.card {
  width: min(400px, 92vw);
}
h2 {
  margin: 0 0 16px;
  text-align: center;
}
</style>
