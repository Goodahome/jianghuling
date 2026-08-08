<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createFeedback, listMyFeedbacks } from '@/api/feedback'
import type { FeedbackStatus, FeedbackType } from '@/types/api'
import type { FeedbackSummary } from '@/types/models'
import {
  feedbackStatusLabel,
  feedbackTypeLabel,
  resolveFeedbackStatusLabel,
  resolveFeedbackTypeLabel,
} from '@/utils/labels'
import EmptyState from '@/components/EmptyState.vue'
import ImageUpload from '@/components/ImageUpload.vue'
import JhPageHeader from '@/components/JhPageHeader.vue'
import PageBreadcrumb from '@/components/PageBreadcrumb.vue'

const list = ref<FeedbackSummary[]>([])
const loading = ref(false)
const submitting = ref(false)
const query = reactive({ page: 1, pageSize: 20, status: '' as FeedbackStatus | '' })
const total = ref(0)

const form = reactive({
  type: 'BUG' as FeedbackType,
  title: '',
  content: '',
  contact: '',
  relatedRef: '',
  attachmentUrls: [] as string[],
})

const crumbs = [
  { label: '首页', to: '/' },
  { label: '意见反馈' },
]

const typeOptions = (Object.keys(feedbackTypeLabel) as FeedbackType[]).map((k) => ({
  value: k,
  label: feedbackTypeLabel[k],
}))

const statusOptions = (Object.keys(feedbackStatusLabel) as FeedbackStatus[]).map((k) => ({
  value: k,
  label: feedbackStatusLabel[k],
}))

async function load() {
  loading.value = true
  try {
    const data = await listMyFeedbacks({
      page: query.page,
      pageSize: query.pageSize,
      status: query.status || undefined,
    })
    list.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

async function onSubmit() {
  if (!form.type) return ElMessage.warning('请选择反馈类型')
  if (!form.title.trim()) return ElMessage.warning('请填写标题')
  if (!form.content.trim()) return ElMessage.warning('请填写正文')
  if (form.title.trim().length > 100) return ElMessage.warning('标题建议不超过 100 字')
  if (form.content.trim().length > 2000) return ElMessage.warning('正文建议不超过 2000 字')

  submitting.value = true
  try {
    await createFeedback({
      type: form.type,
      title: form.title.trim(),
      content: form.content.trim(),
      contact: form.contact.trim() || undefined,
      relatedRef: form.relatedRef.trim() || undefined,
      attachmentUrls: form.attachmentUrls.length ? form.attachmentUrls : undefined,
    })
    ElMessage.success('已送达武林盟，可在下方「我的反馈」查看状态')
    form.title = ''
    form.content = ''
    form.contact = ''
    form.relatedRef = ''
    form.attachmentUrls = []
    query.page = 1
    await load()
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>

<template>
  <section class="jh-section">
    <div class="jh-container narrow">
      <JhPageHeader title="意见反馈" />
      <PageBreadcrumb :items="crumbs" />
      <p class="jh-muted intro">有话直说，武林盟听着。提交后可在下方「我的反馈」查看处理状态。</p>

      <el-form class="jh-panel block" label-position="top" @submit.prevent="onSubmit">
        <h2>投递反馈</h2>
        <el-form-item label="类型" required>
          <el-select v-model="form.type" style="width: 100%">
            <el-option v-for="o in typeOptions" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题" required>
          <el-input v-model="form.title" maxlength="100" show-word-limit placeholder="一句话说明问题" />
        </el-form-item>
        <el-form-item label="正文" required>
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="5"
            maxlength="2000"
            show-word-limit
            placeholder="复现步骤、期望与现状等"
          />
        </el-form-item>
        <el-form-item label="联系方式（选填）">
          <el-input v-model="form.contact" maxlength="64" placeholder="手机 / 微信号，便于回访" />
        </el-form-item>
        <el-form-item label="相关页面 / 悬赏 ID（选填）">
          <el-input v-model="form.relatedRef" maxlength="128" placeholder="如 /bounties/88 或悬赏编号" />
        </el-form-item>
        <el-form-item label="附件截图（选填，最多 3 张）">
          <ImageUpload v-model="form.attachmentUrls" :limit="3" tip="上传问题截图" />
        </el-form-item>
        <el-button type="primary" class="jh-btn-seal" native-type="submit" :loading="submitting">
          送达武林盟
        </el-button>
      </el-form>

      <div class="list-head">
        <h2>我的反馈</h2>
        <el-select
          v-model="query.status"
          clearable
          placeholder="全部状态"
          style="width: 140px"
          @change="query.page = 1; load()"
        >
          <el-option v-for="o in statusOptions" :key="o.value" :label="o.label" :value="o.value" />
        </el-select>
      </div>

      <EmptyState v-if="!loading && !list.length" title="暂无反馈" description="投递后可在此查看状态" />
      <RouterLink
        v-for="item in list"
        :key="item.id"
        :to="`/feedbacks/${item.id}`"
        class="jh-panel item"
      >
        <div class="item-top">
          <strong>{{ item.title }}</strong>
          <span class="status">{{ resolveFeedbackStatusLabel(item.status) }}</span>
        </div>
        <p class="jh-muted">
          {{ resolveFeedbackTypeLabel(item.type) }} · {{ item.createdAt }}
        </p>
      </RouterLink>

      <el-pagination
        v-if="total > query.pageSize"
        v-model:current-page="query.page"
        :page-size="query.pageSize"
        :total="total"
        layout="prev, pager, next"
        class="pager"
        @current-change="load"
      />
    </div>
  </section>
</template>

<style scoped>
.intro {
  margin: 0 0 14px;
  font-size: 13px;
}
.block,
.item {
  display: block;
  padding: 16px;
  margin-bottom: 12px;
  text-decoration: none;
  color: inherit;
}
.block h2,
.list-head h2 {
  margin: 0 0 12px;
  font-family: var(--jh-font-display);
  font-size: 18px;
}
.list-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin: 8px 0 12px;
}
.list-head h2 {
  margin: 0;
}
.item-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
}
.item-top strong {
  flex: 1;
  min-width: 0;
}
.status {
  flex-shrink: 0;
  font-size: 13px;
  color: var(--jh-gold-deep);
}
.pager {
  margin-top: 8px;
  justify-content: center;
}
</style>
