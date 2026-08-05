<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { adminListNotices, adminSaveNotice } from '@/api/admin'

const list = ref<Record<string, unknown>[]>([])
const form = reactive({
  title: '',
  category: 'ANNOUNCE',
  content: '',
  pinned: false,
})

async function load() {
  const data = await adminListNotices({ page: 1, pageSize: 50 })
  list.value = data.list || []
}

async function save() {
  await adminSaveNotice({ ...form })
  ElMessage.success('已保存')
  form.title = ''
  form.content = ''
  await load()
}

onMounted(load)
</script>

<template>
  <div>
    <h2>告示管理</h2>
    <el-form :inline="false" label-width="80px" style="max-width: 640px; margin-bottom: 16px">
      <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
      <el-form-item label="分类">
        <el-select v-model="form.category">
          <el-option label="规则" value="RULES" />
          <el-option label="防骗" value="ANTI_FRAUD" />
          <el-option label="遵义租房" value="ZUNYI_RENT" />
          <el-option label="公告" value="ANNOUNCE" />
        </el-select>
      </el-form-item>
      <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="4" /></el-form-item>
      <el-form-item label="置顶"><el-switch v-model="form.pinned" /></el-form-item>
      <el-button type="primary" @click="save">发布</el-button>
    </el-form>
    <el-table :data="list">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="category" label="分类" width="140" />
      <el-table-column prop="pinned" label="置顶" width="80" />
      <el-table-column prop="createdAt" label="时间" width="180" />
    </el-table>
  </div>
</template>
