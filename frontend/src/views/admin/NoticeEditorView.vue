<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { adminListNotices, adminSaveNotice } from '@/api/admin'
import { getNotice } from '@/api/notice'
import { noticeCategoryLabel } from '@/utils/labels'
import type { NoticeCategory } from '@/types/api'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const saving = ref(false)

const noticeId = computed(() => {
  const raw = route.params.id
  if (raw == null || raw === '') return null
  const n = Number(Array.isArray(raw) ? raw[0] : raw)
  return Number.isFinite(n) && n > 0 ? n : null
})
const isEdit = computed(() => noticeId.value != null)

const form = reactive({
  title: '',
  category: 'ANNOUNCE' as NoticeCategory,
  content: '',
  pinned: false,
  status: 'PUBLISHED',
})

const categories = (Object.keys(noticeCategoryLabel) as NoticeCategory[]).map((value) => ({
  value,
  label: noticeCategoryLabel[value],
}))

async function load() {
  if (!noticeId.value) return
  loading.value = true
  try {
    // 优先公开详情；下架告示可能仍可读；失败则从后台列表回填
    try {
      const n = await getNotice(noticeId.value)
      form.title = n.title || ''
      form.category = (n.category || 'ANNOUNCE') as NoticeCategory
      form.content = n.content || ''
      form.pinned = !!n.pinned
      form.status = (n as { status?: string }).status || 'PUBLISHED'
      return
    } catch {
      /* fall through */
    }
    const data = await adminListNotices({ page: 1, pageSize: 100 })
    const hit = (data.list || []).find((x) => Number(x.id) === noticeId.value)
    if (!hit) {
      ElMessage.error('告示不存在')
      router.replace('/admin/notices')
      return
    }
    form.title = String(hit.title || '')
    form.category = String(hit.category || 'ANNOUNCE') as NoticeCategory
    form.content = String(hit.content || '')
    form.pinned = !!hit.pinned
    form.status = String(hit.status || 'PUBLISHED')
  } finally {
    loading.value = false
  }
}

async function save() {
  if (!form.title.trim() || !form.content.trim()) {
    ElMessage.warning('标题和内容必填')
    return
  }
  saving.value = true
  try {
    await adminSaveNotice(
      {
        title: form.title.trim(),
        category: form.category,
        content: form.content,
        pinned: form.pinned,
        status: form.status,
      },
      noticeId.value ?? undefined,
    )
    ElMessage.success(isEdit.value ? '已更新' : '已发布')
    router.push('/admin/notices')
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<template>
  <div v-loading="loading" class="page">
    <div class="toolbar">
      <el-button @click="router.push('/admin/notices')">返回列表</el-button>
      <h2>{{ isEdit ? `编辑告示 #${noticeId}` : '发布告示' }}</h2>
    </div>

    <el-form label-width="88px" style="max-width: 720px">
      <el-form-item label="标题" required>
        <el-input v-model="form.title" maxlength="80" show-word-limit />
      </el-form-item>
      <el-form-item label="分类" required>
        <el-select v-model="form.category" style="width: 220px">
          <el-option v-for="c in categories" :key="c.value" :label="c.label" :value="c.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="内容" required>
        <el-input v-model="form.content" type="textarea" :rows="12" placeholder="告示正文" />
      </el-form-item>
      <el-form-item label="置顶">
        <el-switch v-model="form.pinned" />
      </el-form-item>
      <el-form-item label="状态">
        <el-radio-group v-model="form.status">
          <el-radio value="PUBLISHED">已发布</el-radio>
          <el-radio value="OFFLINE">已下架</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="saving" @click="save">
          {{ isEdit ? '保存' : '发布' }}
        </el-button>
        <el-button @click="router.push('/admin/notices')">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<style scoped>
.page {
  max-width: 800px;
}
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}
h2 {
  margin: 0;
  font-size: 20px;
}
</style>
