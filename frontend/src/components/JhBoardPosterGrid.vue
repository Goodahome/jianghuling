<script setup lang="ts">
/** 金榜钉墙网格：承载 JhBoardPoster 列表 */
defineProps<{
  loading?: boolean
  /** 为 true 时展示 empty 插槽并隐藏列表项由父级控制 */
  empty?: boolean
}>()
</script>

<template>
  <div v-loading="loading" class="jinbang-grid">
    <div v-if="empty" class="grid-empty">
      <slot name="empty" />
    </div>
    <slot v-else />
  </div>
</template>

<style scoped>
.jinbang-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, 200px);
  gap: 16px 14px;
  min-height: 160px;
  align-items: start;
  justify-content: center;
  justify-items: stretch;
}
.grid-empty {
  grid-column: 1 / -1;
  width: 100%;
}
.grid-empty :deep(.jh-empty) {
  background: transparent;
  border: none;
  box-shadow: none;
}

@media (max-width: 768px) {
  .jinbang-grid {
    /* 常见手机（含 360 宽）两列；仅极窄屏再单列 */
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 10px;
    justify-content: stretch;
  }
}

@media (max-width: 300px) {
  .jinbang-grid {
    grid-template-columns: 1fr;
  }
}
</style>
