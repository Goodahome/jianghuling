<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { exchangeStamina, getLevelProgress, listProducts, redeemProduct } from '@/api/growth'
import { getGrowthConfig } from '@/api/meta'
import type { GrowthConfig, LevelProgress, Product } from '@/types/models'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const level = ref<LevelProgress | null>(null)
const config = ref<GrowthConfig | null>(null)
const products = ref<Product[]>([])
const staminaPoints = ref(1)

onMounted(async () => {
  ;[level.value, config.value] = await Promise.all([getLevelProgress(), getGrowthConfig()])
  const page = await listProducts({ page: 1, pageSize: 50 })
  products.value = page.list || []
  await auth.fetchMe()
})

async function onExchange() {
  await exchangeStamina(staminaPoints.value)
  ElMessage.success('已兑换体力')
  await auth.fetchMe()
  level.value = await getLevelProgress()
}

async function onRedeem(id: number) {
  await redeemProduct(id, 1)
  ElMessage.success('兑换成功')
  await auth.fetchMe()
}
</script>

<template>
  <section class="jh-section">
    <div class="jh-container">
      <h1 class="brand-title">成长与兑换</h1>
      <div class="jh-panel block">
        <p>
          当前 {{ level?.levelTitle }} · 侠义 {{ level?.chivalry }} · 体力 {{ auth.me?.stamina }}
        </p>
        <p class="jh-muted">
          汇率：{{ config?.chivalryPerStamina ?? '-' }} 侠义 / 点体力 · 日揭榜上限
          {{ config?.dailyClaimLimit }}
        </p>
        <div class="row">
          <el-input-number v-model="staminaPoints" :min="1" />
          <el-button type="primary" class="jh-btn-seal" @click="onExchange">侠义兑体力</el-button>
        </div>
      </div>
      <div class="grid">
        <div v-for="p in products" :key="p.id" class="jh-panel product">
          <h3>{{ p.name }}</h3>
          <p class="jh-muted">{{ p.description }}</p>
          <div class="row">
            <strong>{{ p.costChivalry }} 侠义</strong>
            <el-button size="small" @click="onRedeem(p.id)">兑换</el-button>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
h1 {
  margin: 0 0 12px;
  font-size: 32px;
}
.block,
.product {
  padding: 16px;
  margin-bottom: 12px;
}
.row {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-top: 10px;
}
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 12px;
}
</style>
