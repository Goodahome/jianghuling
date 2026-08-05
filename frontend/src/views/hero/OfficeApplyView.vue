<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  applyOffice,
  getMyOfficeApplications,
  getMyOffices,
  listOfficeDefs,
} from '@/api/office'
import type { OfficeBrief, OfficeDef } from '@/types/models'

const defs = ref<OfficeDef[]>([])
const mine = ref<OfficeBrief[]>([])
const apps = ref<Record<string, unknown>[]>([])
const statement = ref('')

async function load() {
  ;[defs.value, mine.value, apps.value] = await Promise.all([
    listOfficeDefs(),
    getMyOffices(),
    getMyOfficeApplications(),
  ])
}

async function onApply(code: string) {
  await applyOffice(code, statement.value || '愿尽职司，守护悬赏秩序。')
  ElMessage.success('职司申请已提交')
  await load()
}

onMounted(load)
</script>

<template>
  <section class="jh-section">
    <div class="jh-container narrow">
      <h1 class="brand-title">职司申请</h1>
      <p class="jh-muted">令审使 / 验功使 · 由武林盟授予 · 持职后可进入执事堂</p>
      <div v-if="mine.length" class="jh-panel block">
        <h2>我的职司</h2>
        <el-tag v-for="o in mine" :key="o.code" style="margin-right: 8px">
          {{ o.name }} · {{ o.status }}
        </el-tag>
        <div style="margin-top: 12px">
          <RouterLink to="/hall"><el-button type="primary" class="jh-btn-seal">进入执事堂</el-button></RouterLink>
        </div>
      </div>
      <el-input v-model="statement" type="textarea" :rows="2" placeholder="申请陈述" class="block" />
      <div v-for="d in defs" :key="d.code" class="jh-panel block">
        <h3>{{ d.name }}</h3>
        <p class="jh-muted">{{ d.description }} · 门槛等级 {{ d.minLevel }} · 名额 {{ d.quota }}</p>
        <el-button :disabled="!d.canApply" @click="onApply(d.code)">申请</el-button>
      </div>
      <div v-if="apps.length" class="jh-panel block">
        <h2>申请记录</h2>
        <pre>{{ apps }}</pre>
      </div>
    </div>
  </section>
</template>

<style scoped>
.narrow {
  max-width: 720px;
}
h1 {
  margin: 0 0 6px;
  font-size: 32px;
}
.block {
  padding: 16px;
  margin: 12px 0;
}
</style>
