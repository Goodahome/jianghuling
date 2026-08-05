<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
</script>

<template>
  <div>
    <h2>执事堂概览</h2>
    <p>欢迎，{{ auth.user?.nickname }}。请在侧栏进入令审/验功队列。回避规则由服务端强制。</p>
    <el-alert
      type="info"
      show-icon
      :closable="false"
      title="职司仅持有治理权限（L1），不可见武林盟核心权限（L0）。"
    />
  </div>
</template>
