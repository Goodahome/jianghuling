<script setup lang="ts">
defineProps<{
  title?: string
  description?: string
}>()
</script>

<template>
  <div class="jh-empty">
    <p class="title">{{ title || '暂无内容' }}</p>
    <p v-if="description" class="jh-muted">{{ description }}</p>
    <div v-if="$slots.default" class="actions">
      <slot />
    </div>
  </div>
</template>

<style scoped>
.title {
  font-family: var(--jh-font-display);
  font-size: 20px;
  margin: 0 0 8px;
}
.actions {
  margin-top: 16px;
}
</style>
