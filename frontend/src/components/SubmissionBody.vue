<script setup lang="ts">
/**
 * §8.0 SubmissionDetail 正文展示（清单项 + 图片附件）
 * C 端 / 执事堂 / Admin 复用，禁止各端另造平行字段。
 */
import type { SubmissionItemDetail } from '@/types/models'

defineProps<{
  items: SubmissionItemDetail[]
  emptyText?: string
}>()
</script>

<template>
  <div class="submission-body">
    <el-empty v-if="!items?.length" :description="emptyText || '暂无清单填写'" :image-size="64" />
    <div v-for="it in items" :key="it.itemCode" class="item">
      <div class="item-head">
        <strong>{{ it.itemName }}</strong>
        <el-tag size="small" :type="it.done ? 'success' : 'info'">
          {{ it.done ? '已完成' : '未完成' }}
        </el-tag>
      </div>
      <p class="text">{{ it.text || '—' }}</p>
      <div v-if="it.mediaUrls?.length" class="imgs">
        <a
          v-for="u in it.mediaUrls"
          :key="u"
          :href="u"
          target="_blank"
          rel="noopener noreferrer"
        >
          <img :src="u" alt="" />
        </a>
      </div>
    </div>
  </div>
</template>

<style scoped>
.item {
  padding: 12px 0;
  border-bottom: 1px solid var(--jh-line, #ebeef5);
}
.item:last-child {
  border-bottom: none;
}
.item-head {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 6px;
}
.text {
  margin: 0;
  white-space: pre-wrap;
  line-height: 1.6;
  color: var(--jh-ink, #303133);
}
.imgs {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 8px;
}
.imgs a {
  display: block;
  width: 96px;
  height: 72px;
  border: 1px solid var(--jh-line, #e5e7eb);
  border-radius: 4px;
  overflow: hidden;
  background: #fff;
}
.imgs img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
</style>
