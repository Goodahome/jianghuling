<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  exchangeStamina,
  getLevelProgress,
  listProducts,
  listRedeemOrders,
  redeemProduct,
} from '@/api/growth'
import { getGrowthConfig } from '@/api/meta'
import type { GrowthConfig, LevelProgress, Product } from '@/types/models'
import { useAuthStore } from '@/stores/auth'
import JhPageHeader from '@/components/JhPageHeader.vue'

const auth = useAuthStore()
const level = ref<LevelProgress | null>(null)
const config = ref<GrowthConfig | null>(null)
const products = ref<Product[]>([])
const orders = ref<Record<string, unknown>[]>([])
const staminaPoints = ref(1)

async function loadOrders() {
  const page = await listRedeemOrders({ page: 1, pageSize: 20 }).catch(() => null)
  orders.value = page?.list || []
}

onMounted(async () => {
  ;[level.value, config.value] = await Promise.all([getLevelProgress(), getGrowthConfig()])
  const page = await listProducts({ page: 1, pageSize: 50 })
  products.value = page.list || []
  await Promise.all([auth.fetchMe(), loadOrders()])
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
  await loadOrders()
}
</script>

<template>
  <section class="jh-section">
    <div class="jh-container">
      <JhPageHeader title="成长与兑换" />
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

      <div class="jh-panel block orders">
        <h2>我的兑换订单</h2>
        <el-empty v-if="!orders.length" description="暂无兑换记录" />
        <el-table v-else :data="orders">
          <el-table-column prop="id" label="单号" width="90" />
          <el-table-column prop="productName" label="奖品" />
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column prop="costChivalry" label="消耗侠义" width="110" />
          <el-table-column prop="status" label="状态" width="100" />
          <el-table-column prop="createdAt" label="时间" min-width="160" />
        </el-table>
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
.orders {
  margin-top: 8px;
}
.orders h2 {
  margin: 0 0 12px;
  font-size: 18px;
  font-family: var(--jh-font-display);
}
</style>
