<script setup lang="ts">
/** 执事堂顶栏：木块面包屑 + 右侧操作（通过/驳回等） */
import PageBreadcrumb, { type CrumbItem } from '@/components/PageBreadcrumb.vue'

defineProps<{
  items: CrumbItem[]
}>()
</script>

<template>
  <div class="hall-crumb-bar">
    <PageBreadcrumb :items="items" />
    <div v-if="$slots.default" class="hall-crumb-actions">
      <slot />
    </div>
  </div>
</template>

<style scoped>
.hall-crumb-bar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 4px;
}
.hall-crumb-bar :deep(.wood-crumbs) {
  margin-bottom: 10px;
  flex: 1;
  min-width: 0;
}
.hall-crumb-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  flex-shrink: 0;
  margin-bottom: 10px;
}
</style>
