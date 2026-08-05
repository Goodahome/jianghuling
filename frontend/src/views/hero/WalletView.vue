<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getWalletAccount, listLedgers, recharge, withdraw } from '@/api/wallet'
import type { WalletAccount, WalletLedger } from '@/types/models'
import { clientRequestId, formatAmount, ledgerTypeLabel } from '@/utils/labels'
import EmptyState from '@/components/EmptyState.vue'

const account = ref<WalletAccount | null>(null)
const ledgers = ref<WalletLedger[]>([])
const total = ref(0)
const loading = ref(false)
const amount = ref(100)
const query = reactive({ page: 1, pageSize: 10 })

async function loadAccount() {
  account.value = await getWalletAccount()
}

async function loadLedgers() {
  loading.value = true
  try {
    const data = await listLedgers(query)
    ledgers.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

async function onRecharge() {
  await recharge(amount.value, clientRequestId('recharge'))
  ElMessage.success('模拟充值成功')
  await Promise.all([loadAccount(), loadLedgers()])
}

async function onWithdraw() {
  await withdraw(amount.value, clientRequestId('withdraw'))
  ElMessage.success('模拟提现成功')
  await Promise.all([loadAccount(), loadLedgers()])
}

onMounted(async () => {
  await Promise.all([loadAccount(), loadLedgers()])
})
</script>

<template>
  <section class="jh-section">
    <div class="jh-container">
      <h1 class="brand-title">模拟钱庄</h1>
      <p class="jh-muted">单位：两 · 非真实货币 · 发令将冻结赏银</p>

      <div class="stats">
        <div class="jh-panel stat">
          <span>可用余额</span>
          <strong>{{ formatAmount(account?.balance) }} 两</strong>
        </div>
        <div class="jh-panel stat">
          <span>托管冻结</span>
          <strong>{{ formatAmount(account?.frozen) }} 两</strong>
        </div>
      </div>

      <div class="jh-panel ops">
        <el-input-number v-model="amount" :min="1" :step="50" />
        <el-button type="primary" class="jh-btn-seal" @click="onRecharge">模拟充值</el-button>
        <el-button @click="onWithdraw">模拟提现</el-button>
      </div>

      <div class="jh-panel table-wrap" v-loading="loading">
        <h2>流水</h2>
        <EmptyState v-if="!ledgers.length" title="暂无流水" />
        <el-table v-else :data="ledgers" stripe>
          <el-table-column prop="createdAt" label="时间" min-width="160" />
          <el-table-column label="类型" min-width="120">
            <template #default="{ row }">{{ ledgerTypeLabel[row.type] || row.type }}</template>
          </el-table-column>
          <el-table-column label="金额" min-width="100">
            <template #default="{ row }">{{ formatAmount(row.amount) }}</template>
          </el-table-column>
          <el-table-column label="余额后" min-width="100">
            <template #default="{ row }">{{ formatAmount(row.balanceAfter) }}</template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="140" />
        </el-table>
        <el-pagination
          v-if="total > query.pageSize"
          v-model:current-page="query.page"
          :page-size="query.pageSize"
          :total="total"
          layout="prev, pager, next"
          class="pager"
          @current-change="loadLedgers"
        />
      </div>
    </div>
  </section>
</template>

<style scoped>
h1 {
  margin: 0 0 6px;
  font-size: 36px;
}
.stats {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin: 18px 0;
}
.stat {
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.stat strong {
  font-size: 28px;
  color: var(--jh-seal);
}
.ops,
.table-wrap {
  padding: 16px;
  margin-bottom: 14px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  align-items: center;
}
.table-wrap {
  display: block;
}
h2 {
  margin: 0 0 12px;
  font-family: var(--jh-font-display);
}
.pager {
  margin-top: 12px;
  justify-content: center;
}
@media (max-width: 640px) {
  h1 {
    font-size: 28px;
  }
  .stats {
    grid-template-columns: 1fr 1fr;
    gap: 8px;
  }
  .stat {
    padding: 14px;
  }
  .stat strong {
    font-size: 22px;
  }
  .ops {
    flex-direction: column;
    align-items: stretch;
  }
  .ops :deep(.el-input-number) {
    width: 100%;
  }
  .ops .el-button {
    width: 100%;
    margin: 0 !important;
  }
  .table-wrap {
    padding: 12px;
    overflow-x: auto;
  }
}
</style>
