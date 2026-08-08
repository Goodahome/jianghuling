<script setup lang="ts">
import { computed, watchEffect } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { renderMarkdown } from '@/utils/markdown'
import userAgreementMd from '@/content/legal/user-agreement.md?raw'
import privacyPolicyMd from '@/content/legal/privacy-policy.md?raw'
import PageBreadcrumb from '@/components/PageBreadcrumb.vue'

const route = useRoute()
const router = useRouter()

const DOC_MAP = {
  'user-agreement': {
    title: '用户服务协议',
    version: 'v1.0-draft',
    source: userAgreementMd,
  },
  privacy: {
    title: '隐私政策',
    version: 'v1.0-draft',
    source: privacyPolicyMd,
  },
} as const

type DocKey = keyof typeof DOC_MAP

const docKey = computed(() => {
  const key = String(route.params.doc || '')
  return (key in DOC_MAP ? key : '') as DocKey | ''
})

const doc = computed(() => (docKey.value ? DOC_MAP[docKey.value] : null))

const crumbs = computed(() => [
  { label: '首页', to: '/' },
  { label: doc.value?.title || '法律条款' },
])
const html = computed(() => {
  if (!doc.value) return ''
  // 页头已展示标题，正文去掉文首一级标题避免重复
  const src = doc.value.source.replace(/^#\s+.+\n+/, '')
  return renderMarkdown(src)
})

watchEffect(() => {
  if (!docKey.value) {
    router.replace('/')
    return
  }
  if (doc.value) {
    document.title = `${doc.value.title} · 江湖令`
  }
})

</script>

<template>
  <section v-if="doc" class="jh-section legal-page">
    <div class="jh-container narrow">
      <PageBreadcrumb :items="crumbs" />
      <div class="jh-panel legal">
      <p class="meta jh-muted">
        内测稿 · 版本 {{ doc.version }} · 生效日期以本页展示为准
      </p>
      <h1 class="legal-title">{{ doc.title }}</h1>
      <p class="ops jh-muted">内测运营方·江湖令项目组 · 正式主体信息待确认 · 联系请用站内消息/武林盟通道</p>
      <article class="legal-body" v-html="html" />
      <div class="legal-foot">
        <RouterLink to="/legal/user-agreement">用户协议</RouterLink>
        <span>·</span>
        <RouterLink to="/legal/privacy">隐私政策</RouterLink>
        <span>·</span>
        <RouterLink to="/">返回江湖</RouterLink>
      </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.legal-page {
  padding-bottom: 32px;
}
.legal {
  padding: 24px 22px 28px;
}
.meta {
  margin: 0;
  font-size: 13px;
}
.legal-title {
  margin: 8px 0 6px;
  font-size: clamp(22px, 5vw, 28px);
  text-align: center;
  font-family: var(--jh-font-doc);
  color: var(--jh-ink);
  text-shadow: none;
  letter-spacing: 0.06em;
  font-weight: 600;
}
.ops {
  margin: 0 0 18px;
  font-size: 13px;
  line-height: 1.5;
  text-align: center;
}
.legal-body {
  color: var(--jh-ink);
  line-height: 1.85;
  font-size: 15px;
  font-family: var(--jh-font-body);
}
.legal-body :deep(h1),
.legal-body :deep(h2),
.legal-body :deep(h3) {
  font-family: var(--jh-font-doc);
  color: var(--jh-ink);
  text-shadow: none;
  font-weight: 600;
  letter-spacing: 0.03em;
}
.legal-body :deep(h1) {
  font-size: 1.35em;
  margin: 1.2em 0 0.55em;
}
.legal-body :deep(h2) {
  font-size: 1.15em;
  margin: 1.35em 0 0.5em;
}
.legal-body :deep(h3) {
  font-size: 1.05em;
  margin: 1.1em 0 0.4em;
}
.legal-body :deep(p) {
  margin: 0.55em 0;
}
.legal-body :deep(blockquote) {
  margin: 12px 0 18px;
  padding: 10px 14px;
  background: rgba(90, 66, 40, 0.08);
  border-left: 3px solid rgba(138, 107, 42, 0.55);
  color: var(--jh-muted);
  font-size: 14px;
}
.legal-body :deep(blockquote p) {
  margin: 0.25em 0;
}
.legal-body :deep(ul),
.legal-body :deep(ol) {
  margin: 0.5em 0 0.8em;
  padding-left: 1.35em;
}
.legal-body :deep(li) {
  margin: 0.28em 0;
}
.legal-body :deep(hr) {
  border: none;
  border-top: 1px solid rgba(138, 107, 42, 0.35);
  margin: 1.4em 0;
}
.legal-body :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 0.8em 0 1.1em;
  font-size: 13px;
  overflow-x: auto;
  display: block;
}
.legal-body :deep(th),
.legal-body :deep(td) {
  border: 1px solid rgba(138, 107, 42, 0.35);
  padding: 8px 10px;
  text-align: left;
  vertical-align: top;
}
.legal-body :deep(th) {
  background: rgba(90, 66, 40, 0.1);
  white-space: nowrap;
}
.legal-body :deep(strong) {
  font-weight: 700;
}
.legal-foot {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  margin-top: 28px;
  padding-top: 16px;
  border-top: 1px solid rgba(138, 107, 42, 0.3);
  font-size: 14px;
}
.legal-foot a {
  color: var(--jh-seal);
}
@media (max-width: 480px) {
  .legal {
    padding: 16px 12px 22px;
  }
  .legal-body {
    font-size: 14px;
  }
}
</style>
