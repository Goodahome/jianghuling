<script setup lang="ts">
import type { RouteLocationRaw } from 'vue-router'

export type CrumbItem = {
  label: string
  /** 有 to 则可点击跳转；末级通常不传 */
  to?: RouteLocationRaw
}

defineProps<{
  items: CrumbItem[]
}>()
</script>

<template>
  <nav class="page-crumbs" aria-label="页面路径">
    <ol class="crumb-list">
      <li v-for="(item, i) in items" :key="`${item.label}-${i}`" class="crumb-item">
        <template v-if="i > 0">
          <span class="crumb-sep" aria-hidden="true"> / </span>
        </template>
        <RouterLink
          v-if="item.to && i < items.length - 1"
          :to="item.to"
          class="crumb-link"
        >
          {{ item.label }}
        </RouterLink>
        <span v-else class="crumb-current" aria-current="page">{{ item.label }}</span>
      </li>
    </ol>
  </nav>
</template>

<style scoped>
.page-crumbs {
  display: block;
  width: fit-content;
  max-width: 100%;
  margin: 0 0 14px;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: thin;
}
.crumb-list {
  display: flex;
  flex-wrap: nowrap;
  align-items: baseline;
  list-style: none;
  margin: 0;
  padding: 0;
  width: max-content;
  /* 禁止通栏拉满：勿设 min-width: 100% */
}
.crumb-item {
  display: inline-flex;
  align-items: baseline;
  min-width: 0;
  font-family: var(--jh-font-body);
  font-size: 13px;
  line-height: 1.5;
  letter-spacing: 0.02em;
  white-space: nowrap;
}
.crumb-sep {
  color: rgba(196, 163, 90, 0.55);
  user-select: none;
}
.crumb-link {
  color: rgba(228, 200, 120, 0.88);
  text-decoration: none;
  max-width: 12em;
  overflow: hidden;
  text-overflow: ellipsis;
}
.crumb-link:hover {
  color: var(--jh-gold-bright);
  text-decoration: underline;
  text-underline-offset: 3px;
}
.crumb-current {
  color: rgba(247, 240, 221, 0.72);
  font-weight: 500;
  max-width: 16em;
  overflow: hidden;
  text-overflow: ellipsis;
}
@media (max-width: 480px) {
  .crumb-item {
    font-size: 12px;
  }
  .crumb-link {
    max-width: 8em;
  }
  .crumb-current {
    max-width: 10em;
  }
}
</style>
