<script setup lang="ts">
export type CrumbItem = {
  label: string
  /** 有 to 则可点击跳转；末级通常不传 */
  to?: string
}

defineProps<{
  items: CrumbItem[]
}>()
</script>

<template>
  <nav class="wood-crumbs" aria-label="页面路径">
    <ol class="crumb-list">
      <li v-for="(item, i) in items" :key="`${item.label}-${i}`" class="crumb-item">
        <RouterLink
          v-if="item.to && i < items.length - 1"
          :to="item.to"
          class="wood-block link"
        >
          {{ item.label }}
        </RouterLink>
        <span v-else class="wood-block current" aria-current="page">{{ item.label }}</span>
      </li>
    </ol>
  </nav>
</template>

<style scoped>
.wood-crumbs {
  margin-bottom: 14px;
  max-width: 100%;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
}
.crumb-list {
  display: flex;
  flex-wrap: nowrap;
  align-items: stretch;
  gap: 0;
  list-style: none;
  margin: 0;
  padding: 0;
  width: max-content;
  min-width: 100%;
}
.crumb-item {
  display: flex;
  align-items: stretch;
  position: relative;
}
.crumb-item + .crumb-item {
  margin-left: -1px;
}
.wood-block {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 36px;
  padding: 6px 14px;
  font-family: var(--jh-font-display);
  font-size: 14px;
  letter-spacing: 0.08em;
  white-space: nowrap;
  color: #3a2a18;
  text-decoration: none;
  border: 1px solid rgba(90, 66, 40, 0.4);
  border-radius: 0;
}
.crumb-item:first-child .wood-block {
  border-radius: var(--jh-wood-radius) 0 0 var(--jh-wood-radius);
}
.crumb-item:last-child .wood-block {
  border-radius: 0 var(--jh-wood-radius) var(--jh-wood-radius) 0;
}
.crumb-item:only-child .wood-block {
  border-radius: var(--jh-wood-radius);
}
.wood-block.link {
  cursor: pointer;
  color: #3a2a18;
}
.wood-block.link:hover {
  color: var(--jh-seal);
  z-index: 1;
}
.wood-block.current {
  font-weight: 600;
}
@media (max-width: 480px) {
  .wood-block {
    min-height: 34px;
    padding: 5px 10px;
    font-size: 13px;
    letter-spacing: 0.04em;
  }
}
</style>
