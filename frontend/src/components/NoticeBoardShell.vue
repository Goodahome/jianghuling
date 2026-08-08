<script setup lang="ts">
/** 古代遮雨告示板外壳（视觉试验，可整组件拆除回滚） */
defineProps<{
  title?: string
}>()
</script>

<template>
  <div class="notice-stage">
    <div class="notice-board" role="presentation">
      <div class="board-roof" aria-hidden="true">
        <div class="roof-tiles-stack">
          <div class="roof-tiles-thickness" />
          <div class="roof-tiles" />
        </div>
        <div class="roof-eave" />
      </div>

      <div class="board-beam" aria-hidden="true">
        <span class="beam-notch" />
        <span class="beam-notch right" />
      </div>

      <div class="board-face">
        <slot />
      </div>

      <div class="board-legs" aria-hidden="true">
        <span class="leg" />
        <span class="leg" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.notice-stage {
  width: 100%;
  max-width: 100%;
  margin: 0;
  padding: 0 90px;
  /* 扣除 html zoom，避免一屏内容被放大后撑出滚动条 */
  height: var(--jh-viewport-min);
  min-height: var(--jh-viewport-min);
  max-height: var(--jh-viewport-min);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-sizing: border-box;
}

.notice-board {
  position: relative;
  width: min(1220px, 100%);
  max-width: 100%;
  margin: 0 auto;
  flex: 1 1 auto;
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  max-height: 100%;
  min-width: 0;
  box-shadow: none;
  box-sizing: border-box;
  overflow: visible;
}

.board-roof {
  position: relative;
  z-index: 5;
  flex-shrink: 0;
  width: calc(100% + 180px);
  margin: 0 -90px;
}

.roof-tiles-stack {
  position: relative;
  z-index: 2;
  width: 100%;
  /* clip-path 会裁掉 box-shadow，用 drop-shadow 做外轮廓立体 */
  filter:
    drop-shadow(0 8px 12px rgba(0, 0, 0, 0.4))
    drop-shadow(0 2px 0 rgba(20, 18, 14, 0.32));
}

/* 厚度层：略下错，形成瓦檐侧面厚度；左右斜度须对称 */
.roof-tiles-thickness {
  position: absolute;
  left: 0;
  right: 0;
  top: 4px;
  height: clamp(52px, 6.8vw, 74px);
  background: linear-gradient(180deg, #3a424c 0%, #1e242c 100%);
  clip-path: polygon(12% 0, 88% 0, 100% 100%, 0 100%);
  z-index: 0;
}

.roof-tiles {
  position: relative;
  z-index: 1;
  width: 100%;
  height: clamp(52px, 6.8vw, 74px);
  background-color: #5a6570;
  background-image: url('/textures/roof-tiles.png');
  background-size: 140px 100%;
  background-repeat: repeat-x;
  background-position: center top;
  /* 左右各收 6%，与厚度层同轴对称（勿写成 6%/96% 导致右陡左缓） */
  clip-path: polygon(6% 0, 94% 0, 100% 100%, 0 100%);
}

/* 瓦面内缘立体：顶亮、底暗、两侧收口 */
.roof-tiles::before {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.18) 0%, transparent 28%),
    linear-gradient(180deg, transparent 55%, rgba(0, 0, 0, 0.38) 100%),
    linear-gradient(90deg, rgba(0, 0, 0, 0.32) 0%, transparent 10%, transparent 90%, rgba(0, 0, 0, 0.32) 100%);
}

.roof-tiles::after {
  content: '';
  position: absolute;
  left: 2%;
  right: 2%;
  bottom: 0;
  height: 7px;
  pointer-events: none;
  background: linear-gradient(180deg, rgba(80, 90, 100, 0.15), rgba(15, 18, 22, 0.55));
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.08);
}

.roof-eave {
  position: relative;
  z-index: 1;
  width: calc(100% - 96px);
  height: 22px;
  margin: -8px auto 0;
  background: linear-gradient(180deg, #6a4a2a 0%, #3a2816 55%, #24180e 100%);
  border: 1px solid rgba(196, 163, 90, 0.4);
  border-top: none;
  box-shadow:
    0 6px 12px rgba(0, 0, 0, 0.28),
    inset 0 1px 0 rgba(228, 200, 120, 0.22);
}

.board-beam {
  position: relative;
  z-index: 2;
  flex-shrink: 0;
  height: 14px;
  margin: 0 12px;
  background: linear-gradient(180deg, #5a4228, #2a1e12);
  border: 1px solid rgba(196, 163, 90, 0.35);
  box-shadow: inset 0 1px 0 rgba(228, 200, 120, 0.18);
}

.beam-notch {
  position: absolute;
  top: 2px;
  left: 12px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: radial-gradient(circle at 35% 35%, #6a5234, #1a120a);
  box-shadow: inset 0 1px 2px rgba(0, 0, 0, 0.5);
}
.beam-notch.right {
  left: auto;
  right: 12px;
}

.board-face {
  position: relative;
  z-index: 1;
  /* 木面吃满剩余高度，避免立柱被拉长、牌面显矮 */
  flex: 1 1 auto;
  display: flex;
  flex-direction: column;
  margin: 0 12px;
  /* 左右内边距刚好让出竖向木条，内容区不被滚动条再挤 */
  padding: 0 var(--jh-board-face-inline) 8px;
  min-height: 0;
  max-height: 100%;
  /* 侧栏 absolute 滑出时由舞台 overflow-x 裁切，木面保持定位上下文 */
  isolation: isolate;
  background-color: #3d3224;
  background-image:
    linear-gradient(180deg, rgba(62, 48, 32, 0.55), rgba(42, 34, 24, 0.35)),
    url('/textures/notice-wood.png');
  background-size: auto, 280px 280px;
  border: 3px solid #2a1e12;
  border-top: none;
  box-shadow:
    inset 0 0 0 1px rgba(196, 163, 90, 0.22),
    inset 0 24px 48px rgba(20, 14, 8, 0.18),
    0 18px 36px rgba(12, 10, 6, 0.35);
  overflow: hidden;
}

/* 侧立柱：随木板面通高；右侧木条同时作为滚动条承载面 */
.board-face::before,
.board-face::after {
  content: '';
  position: absolute;
  top: 10px;
  bottom: 10px;
  width: var(--jh-board-rail-width);
  box-sizing: border-box;
  background:
    linear-gradient(90deg, rgba(20, 12, 6, 0.35) 0%, transparent 28%, transparent 72%, rgba(20, 12, 6, 0.28) 100%),
    linear-gradient(180deg, #6a4a2a 0%, #4a3420 48%, #2e2014 100%);
  border: 1px solid rgba(196, 163, 90, 0.28);
  box-shadow:
    inset 0 1px 0 rgba(228, 200, 120, 0.16),
    inset 0 0 6px rgba(20, 12, 6, 0.25);
  pointer-events: none;
  /* 低于 page-main，便于透明滚动槽透出木纹 */
  z-index: 1;
}
.board-face::before {
  left: var(--jh-board-rail-inset);
}
.board-face::after {
  right: var(--jh-board-rail-inset);
}

/* 底脚立柱：固定短脚，贴齐浏览器底边 */
.board-legs {
  display: flex;
  justify-content: space-between;
  flex: 0 0 auto;
  flex-shrink: 0;
  margin: 0 32px;
  height: 28px;
}

.leg {
  width: 20px;
  height: 100%;
  background: linear-gradient(90deg, #2a1e12, #5a4228 40%, #2a1e12);
  border-radius: 0;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.25);
}

@media (max-width: 768px) {
  .notice-stage {
    /* 给瓦檐左右留出挑出量，避免相对牌面过短 */
    padding: 0 32px;
    overflow-x: hidden;
  }

  .notice-board {
    width: 100%;
    max-width: 100%;
  }

  .board-roof {
    width: calc(100% + 64px);
    max-width: none;
    margin: 0 -32px;
  }

  .roof-tiles,
  .roof-tiles-thickness {
    height: clamp(48px, 12vw, 64px);
  }

  .roof-tiles {
    background-size: 110px 100%;
    /* 略缓斜度，左右挑檐更长 */
    clip-path: polygon(7% 0, 93% 0, 100% 100%, 0 100%);
  }

  .roof-tiles-thickness {
    top: 3px;
    height: clamp(48px, 12vw, 64px);
    clip-path: polygon(12% 0, 88% 0, 100% 100%, 0 100%);
  }

  .roof-eave {
    width: calc(100% - 36px);
    height: 20px;
    margin-top: -6px;
  }

  .board-beam {
    margin: 0 2px;
    height: 12px;
  }

  .board-face {
    margin: 0 2px;
    border-width: 2px;
    padding: 0 10px 8px;
    --jh-board-rail-width: 0px;
    --jh-board-rail-inset: 0px;
    --jh-board-face-inline: 10px;
  }

  .board-face::before,
  .board-face::after {
    display: none;
  }

  .board-legs {
    margin: 0 14px;
    height: 18px;
  }

  .leg {
    width: 12px;
  }
}

@media (max-width: 400px) {
  .notice-stage {
    padding: 0 22px;
  }

  .notice-board {
    width: 100%;
    max-width: 100%;
  }

  .board-roof {
    width: calc(100% + 44px);
    margin: 0 -22px;
  }

  .roof-tiles,
  .roof-tiles-thickness {
    height: clamp(44px, 13vw, 58px);
  }

  .roof-eave {
    width: calc(100% - 28px);
    height: 18px;
  }

  .board-face {
    padding: 0 8px 6px;
  }
}
</style>
