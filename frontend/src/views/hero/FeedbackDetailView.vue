<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getMyFeedback } from '@/api/feedback'
import type { FeedbackDetail } from '@/types/models'
import { resolveFeedbackStatusLabel, resolveFeedbackTypeLabel } from '@/utils/labels'
import PageBreadcrumb from '@/components/PageBreadcrumb.vue'
import JhPageHeader from '@/components/JhPageHeader.vue'

const route = useRoute()
const detail = ref<FeedbackDetail | null>(null)
const loading = ref(false)

const crumbs = computed(() => [
  { label: '首页', to: '/' },
  { label: '意见反馈', to: '/feedbacks' },
  { label: '反馈详情' },
])

const attachments = computed(() =>
  Array.isArray(detail.value?.attachmentUrls) ? detail.value!.attachmentUrls! : [],
)

onMounted(async () => {
  loading.value = true
  try {
    detail.value = await getMyFeedback(route.params.id as string)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <section class="jh-section" v-loading="loading">
    <div class="jh-container narrow" v-if="detail">
      <JhPageHeader title="反馈详情" />
      <PageBreadcrumb :items="crumbs" />
      <div class="jh-panel block">
        <div class="meta">
          <span>{{ resolveFeedbackTypeLabel(detail.type) }}</span>
          <span>·</span>
          <span>{{ resolveFeedbackStatusLabel(detail.status) }}</span>
          <span>·</span>
          <span>{{ detail.createdAt }}</span>
        </div>
        <h2>{{ detail.title }}</h2>
        <p class="content">{{ detail.content }}</p>
        <el-descriptions :column="1" border size="small" class="desc">
          <el-descriptions-item label="联系方式">{{ detail.contact || '—' }}</el-descriptions-item>
          <el-descriptions-item label="相关页面 / 悬赏">
            {{ detail.relatedRef || '—' }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ detail.updatedAt || '—' }}</el-descriptions-item>
        </el-descriptions>
        <div v-if="attachments.length" class="atts">
          <p class="label">附件</p>
          <div class="thumbs">
            <a v-for="(u, i) in attachments" :key="i" :href="u" target="_blank" rel="noopener">
              <img :src="u" alt="" />
            </a>
          </div>
        </div>
        <p class="jh-muted tip">处理进度由武林盟更新；本期不展示内部处理备注。</p>
      </div>
    </div>
  </section>
</template>

<style scoped>
.block {
  padding: 18px;
}
.meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 8px;
  font-size: 13px;
  color: var(--jh-muted);
}
h2 {
  margin: 0 0 12px;
  font-family: var(--jh-font-display);
  font-size: 22px;
}
.content {
  margin: 0 0 16px;
  white-space: pre-wrap;
  line-height: 1.7;
}
.desc {
  margin-bottom: 14px;
}
.atts .label {
  margin: 0 0 8px;
  font-weight: 600;
}
.thumbs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.thumbs a {
  display: block;
  width: 96px;
  height: 72px;
  border: 1px solid var(--jh-line);
  border-radius: var(--jh-radius);
  overflow: hidden;
  background: transparent;
}
.thumbs img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.tip {
  margin: 14px 0 0;
  font-size: 12px;
}
</style>
