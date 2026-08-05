<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  adminCreateProduct,
  adminDeleteProduct,
  adminListProducts,
  adminListRedeemOrders,
  adminUpdateProduct,
  adminUpdateRedeemOrder,
} from '@/api/admin'

const tab = ref('products')
const products = ref<Record<string, unknown>[]>([])
const orders = ref<Record<string, unknown>[]>([])
const form = reactive({
  name: '',
  description: '',
  costChivalry: 10,
  stock: 100,
  status: 'ACTIVE',
})

async function loadProducts() {
  const data = await adminListProducts({ page: 1, pageSize: 50 })
  products.value = data.list || []
}

async function loadOrders() {
  const data = await adminListRedeemOrders({ page: 1, pageSize: 50 })
  orders.value = data.list || []
}

async function create() {
  await adminCreateProduct({ ...form })
  ElMessage.success('已创建奖品')
  form.name = ''
  form.description = ''
  await loadProducts()
}

async function remove(id: number) {
  await ElMessageBox.confirm('确认删除该奖品？', '提示')
  await adminDeleteProduct(id)
  ElMessage.success('已删除')
  await loadProducts()
}

async function updateStock(row: Record<string, unknown>) {
  await adminUpdateProduct(Number(row.id), {
    name: row.name,
    description: row.description,
    costChivalry: row.costChivalry,
    stock: row.stock,
    status: row.status,
  })
  ElMessage.success('已更新')
}

async function markOrder(id: number, status: string) {
  await adminUpdateRedeemOrder(id, { status })
  ElMessage.success('订单状态已更新')
  await loadOrders()
}

onMounted(async () => {
  await Promise.all([loadProducts(), loadOrders()])
})
</script>

<template>
  <div>
    <h2>奖品与兑换单</h2>
    <el-tabs v-model="tab">
      <el-tab-pane label="奖品" name="products">
        <el-form :inline="true" style="margin-bottom: 12px">
          <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
          <el-form-item label="侠义"><el-input-number v-model="form.costChivalry" :min="1" /></el-form-item>
          <el-form-item label="库存"><el-input-number v-model="form.stock" :min="0" /></el-form-item>
          <el-button type="primary" @click="create">新增</el-button>
        </el-form>
        <el-input v-model="form.description" placeholder="奖品说明" style="margin-bottom: 12px" />
        <el-table :data="products">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="name" label="名称" />
          <el-table-column prop="costChivalry" label="侠义" width="90" />
          <el-table-column label="库存" width="120">
            <template #default="{ row }">
              <el-input-number v-model="row.stock" :min="0" size="small" @change="updateStock(row)" />
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100" />
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button size="small" type="danger" @click="remove(Number(row.id))">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="兑换单" name="orders">
        <el-table :data="orders">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="userId" label="用户" width="90" />
          <el-table-column prop="productName" label="奖品" />
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column prop="status" label="状态" width="100" />
          <el-table-column prop="createdAt" label="时间" min-width="160" />
          <el-table-column label="操作" width="200">
            <template #default="{ row }">
              <el-button size="small" @click="markOrder(Number(row.id), 'SHIPPED')">发货</el-button>
              <el-button size="small" type="success" @click="markOrder(Number(row.id), 'DONE')">完成</el-button>
              <el-button size="small" type="danger" @click="markOrder(Number(row.id), 'CANCELLED')">取消</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>
