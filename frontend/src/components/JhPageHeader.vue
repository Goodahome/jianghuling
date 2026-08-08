<script setup lang="ts">
/** 页眉大标题：对齐悬赏金榜样式（无副题；两侧分割线固定等长） */
defineProps<{
  title: string
}>()
</script>

<template>
  <header class="jh-page-header">
    <span class="ornament" aria-hidden="true" />
    <div class="copy">
      <h1 class="title">{{ title }}</h1>
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
  padding-bottom: 14px;
  /* 底部分割线：固定长度居中，不随容器宽 / 标题字数变化 */
  position: relative;
}
.jh-page-header::after {
  content: '';
  position: absolute;
  left: 50%;
  bottom: 0;
  /* 对齐原全宽 jh-container（1120），按视口封顶，不随 narrow/全宽内容区变化 */
  width: min(1120px, calc(100vw - 64px));
  height: 1px;
  transform: translateX(-50%);
  background: rgba(196, 163, 90, 0.35);
}
.copy {
  text-align: center;
  min-width: 0;
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
/* 左右装饰线固定等长 */
.ornament {
  width: 48px;
  height: 2px;
  flex: 0 0 48px;
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
}
</style>
