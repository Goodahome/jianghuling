<script setup lang="ts">
/** 页眉大标题：对齐悬赏金榜样式 */
defineProps<{
  title: string
  subtitle?: string
}>()
</script>

<template>
  <header class="jh-page-header">
    <span class="ornament" aria-hidden="true" />
    <div class="copy">
      <h1 class="title">{{ title }}</h1>
      <p v-if="subtitle" class="sub">{{ subtitle }}</p>
    </div>
    <span class="ornament mirror" aria-hidden="true" />
  </header>
</template>

<style scoped>
.jh-page-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-bottom: 18px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(196, 163, 90, 0.35);
}
.copy {
  text-align: center;
}
.title {
  margin: 0;
  font-family: var(--jh-font-display);
  font-size: clamp(26px, 4vw, 34px);
  letter-spacing: 0.35em;
  text-indent: 0.35em;
  color: var(--jh-gold-bright);
  text-shadow: 0 1px 0 rgba(0, 0, 0, 0.35);
  font-weight: normal;
}
.sub {
  margin: 6px 0 0;
  font-size: 13px;
  letter-spacing: 0.2em;
  color: rgba(247, 240, 221, 0.7);
}
.ornament {
  width: 48px;
  height: 2px;
  flex-shrink: 0;
  background: linear-gradient(90deg, transparent, var(--jh-gold), transparent);
}
.ornament.mirror {
  transform: scaleX(-1);
}

@media (max-width: 768px) {
  .jh-page-header {
    gap: 10px;
    margin-bottom: 14px;
  }
  .title {
    letter-spacing: 0.22em;
    text-indent: 0.22em;
  }
  .ornament {
    width: 28px;
  }
}
</style>
