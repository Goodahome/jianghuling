<script setup lang="ts">
/**
 * 金榜钉墙纸贴卡片（视觉真相源：原悬赏榜）
 * 仅外壳；字段内容由各页 slot 填入
 */
import type { RouteLocationRaw } from 'vue-router'

withDefaults(
  defineProps<{
    to: RouteLocationRaw
    title: string
    /** 用于轻微倾斜错落，移动端忽略 */
    index?: number
    /** 角印文字，悬赏用「令」，告示可用「告」 */
    seal?: string
    /** 右上角关注红点（新会话/新成果/状态变更） */
    showDot?: boolean
  }>(),
  {
    index: 0,
    seal: '令',
    showDot: false,
  },
)
</script>

<template>
  <div class="paper-wrap" :style="{ '--tilt': `${(((index ?? 0) % 5) - 2) * 0.8}deg` }">
    <RouterLink :to="to" class="paper-poster">
      <span v-if="showDot" class="poster-dot" aria-label="有更新" />
      <span class="nail" aria-hidden="true">
        <span class="nail-head" />
        <span class="nail-hole" />
      </span>
      <div class="poster-top">
        <slot name="top" />
      </div>
      <h2 class="poster-title">{{ title }}</h2>
      <div class="poster-body">
        <slot />
      </div>
      <div class="poster-bottom">
        <slot name="bottom" />
      </div>
      <span v-if="seal" class="poster-seal" aria-hidden="true">{{ seal }}</span>
    </RouterLink>
    <div v-if="$slots.actions" class="poster-actions" @click.stop>
      <slot name="actions" />
    </div>
  </div>
</template>

<style scoped>
.paper-wrap {
  width: min(100%, 210px);
  transform: rotate(var(--tilt, 0deg));
  transition: transform 0.18s ease;
}
.paper-wrap:hover {
  transform: rotate(0deg) translateY(-2px);
  z-index: 2;
}
.paper-poster {
  position: relative;
  display: flex;
  flex-direction: column;
  aspect-ratio: 210 / 235;
  width: 100%;
  padding: 24px 14px 12px;
  color: var(--jh-plaque-ink);
  background-color: #f7f1e3;
  background-image:
    linear-gradient(180deg, rgba(255, 255, 255, 0.35), rgba(230, 214, 180, 0.15)),
    url('/textures/rice-paper.png');
  background-size: cover, 180px 180px;
  border: 1px solid rgba(180, 160, 120, 0.45);
  border-radius: 1px;
  box-shadow:
    0 1px 0 rgba(255, 255, 255, 0.45) inset,
    0 10px 18px rgba(20, 14, 8, 0.28),
    0 2px 4px rgba(20, 14, 8, 0.18);
  text-decoration: none;
}
.poster-dot {
  position: absolute;
  top: 8px;
  right: 8px;
  z-index: 3;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #c62828;
  box-shadow:
    0 0 0 2px rgba(247, 241, 227, 0.95),
    0 1px 3px rgba(120, 20, 16, 0.45);
  pointer-events: none;
}
.paper-poster:hover {
  box-shadow:
    0 1px 0 rgba(255, 255, 255, 0.5) inset,
    0 14px 24px rgba(20, 14, 8, 0.32),
    0 3px 6px rgba(20, 14, 8, 0.2);
}
.nail {
  position: absolute;
  top: 8px;
  left: 50%;
  width: 18px;
  height: 18px;
  margin-left: -9px;
  z-index: 2;
  pointer-events: none;
}
.nail-hole {
  position: absolute;
  left: 50%;
  top: 50%;
  width: 8px;
  height: 8px;
  margin: -3px 0 0 -4px;
  border-radius: 50%;
  background: rgba(40, 28, 16, 0.28);
  box-shadow: 0 0 0 1px rgba(40, 28, 16, 0.12);
}
.nail-head {
  position: absolute;
  left: 50%;
  top: 50%;
  width: 14px;
  height: 14px;
  margin: -8px 0 0 -7px;
  border-radius: 50%;
  background: radial-gradient(circle at 32% 28%, #f0e6d0 0%, #b8a070 42%, #6a5840 78%, #3a3020 100%);
  box-shadow:
    0 1px 2px rgba(0, 0, 0, 0.45),
    inset 0 1px 1px rgba(255, 255, 255, 0.35);
}
.poster-top {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
  margin-bottom: 8px;
  min-height: 22px;
}
.poster-title {
  margin: 0 0 8px;
  font-family: var(--jh-font-display);
  font-size: 17px;
  line-height: 1.4;
  letter-spacing: 0.04em;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 2.8em;
  font-weight: normal;
  color: inherit;
}
.poster-body {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
  /* 宣纸浅底：正文用茶墨，勿继承告示板浅色 muted */
  color: rgba(42, 33, 24, 0.78);
}
.poster-body :deep(p) {
  margin: 0;
  font-size: 12px;
  color: rgba(42, 33, 24, 0.78);
}
.poster-bottom {
  margin-top: auto;
  padding-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.poster-seal {
  position: absolute;
  right: 14px;
  bottom: 16px;
  width: 34px;
  height: 34px;
  display: grid;
  place-items: center;
  border: 2px solid rgba(178, 58, 45, 0.65);
  color: rgba(178, 58, 45, 0.75);
  font-family: var(--jh-font-display);
  font-size: 15px;
  transform: rotate(-10deg);
  opacity: 0.72;
  pointer-events: none;
}
.poster-actions {
  margin-top: 8px;
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .paper-wrap {
    width: 100%;
    max-width: 100%;
    transform: none;
  }
  .paper-wrap:hover {
    transform: none;
  }
  .paper-poster {
    padding: 22px 10px 10px;
  }
  .poster-title {
    font-size: 15px;
    min-height: 2.6em;
    -webkit-line-clamp: 2;
  }
  .poster-seal {
    width: 28px;
    height: 28px;
    font-size: 13px;
    right: 10px;
    bottom: 12px;
  }
}
</style>
