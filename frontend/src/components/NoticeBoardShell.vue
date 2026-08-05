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
  margin: 0;
  padding: 0;
  min-height: 100vh;
  min-height: 100dvh;
  display: flex;
  flex-direction: column;
}

.notice-board {
  position: relative;
  width: min(1220px, calc(100% - 20px));
  margin: 0 auto;
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 100%;
  box-shadow: none;
}

.board-roof {
  position: relative;
  z-index: 5;
  flex-shrink: 0;
  width: calc(100% + 240px);
  margin: 0 -120px;
}

.roof-tiles-stack {
  position: relative;
  z-index: 2;
  width: 100%;
  /* clip-path 会裁掉 box-shadow，用 drop-shadow 做外轮廓立体 */
  filter:
    drop-shadow(0 10px 14px rgba(0, 0, 0, 0.42))
    drop-shadow(0 3px 0 rgba(20, 18, 14, 0.35));
}

/* 厚度层：略下错，形成瓦檐侧面厚度 */
.roof-tiles-thickness {
  position: absolute;
  left: 0;
  right: 0;
  top: 6px;
  height: clamp(66px, 9vw, 94px);
  background: linear-gradient(180deg, #3a424c 0%, #1e242c 100%);
  clip-path: polygon(4% 0, 96% 0, 100% 100%, 0 100%);
  z-index: 0;
}

.roof-tiles {
  position: relative;
  z-index: 1;
  width: 100%;
  height: clamp(66px, 9vw, 94px);
  background-color: #5a6570;
  background-image: url('/textures/roof-tiles.png');
  background-size: 160px 100%;
  background-repeat: repeat-x;
  background-position: center top;
  clip-path: polygon(4% 0, 96% 0, 100% 100%, 0 100%);
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
  height: 9px;
  pointer-events: none;
  background: linear-gradient(180deg, rgba(80, 90, 100, 0.15), rgba(15, 18, 22, 0.55));
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.08);
}

.roof-eave {
  position: relative;
  z-index: 1;
  width: calc(100% - 56px);
  height: 30px;
  margin: -8px auto 0;
  background: linear-gradient(180deg, #6a4a2a 0%, #3a2816 55%, #24180e 100%);
  border: 1px solid rgba(196, 163, 90, 0.4);
  border-top: none;
  box-shadow:
    0 8px 14px rgba(0, 0, 0, 0.3),
    inset 0 1px 0 rgba(228, 200, 120, 0.22);
}

.board-beam {
  position: relative;
  z-index: 2;
  flex-shrink: 0;
  height: 18px;
  margin: 0 12px;
  background: linear-gradient(180deg, #5a4228, #2a1e12);
  border: 1px solid rgba(196, 163, 90, 0.35);
  box-shadow: inset 0 1px 0 rgba(228, 200, 120, 0.18);
}

.beam-notch {
  position: absolute;
  top: 3px;
  left: 12px;
  width: 10px;
  height: 10px;
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
  flex: 1;
  margin: 0 12px;
  padding: 0 14px 10px;
  min-height: 0;
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
}

/* 侧立柱：随木板面通高 */
.board-face::before,
.board-face::after {
  content: '';
  position: absolute;
  top: 10px;
  bottom: 10px;
  width: 12px;
  background: linear-gradient(180deg, #5a4228, #3a2a18);
  border: 1px solid rgba(196, 163, 90, 0.25);
  pointer-events: none;
  z-index: 2;
}
.board-face::before {
  left: 4px;
}
.board-face::after {
  right: 4px;
}

/* 底脚立柱：贴齐页面底边 */
.board-legs {
  display: flex;
  justify-content: space-between;
  flex-shrink: 0;
  margin: 0 32px;
  height: 36px;
}

.leg {
  width: 20px;
  height: 100%;
  background: linear-gradient(90deg, #2a1e12, #5a4228 40%, #2a1e12);
  border-radius: 0;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.25);
}

@media (max-width: 768px) {
  .notice-board {
    width: 100%;
  }

  .board-roof {
    width: calc(100% + 80px);
    margin: 0 -40px;
  }

  .roof-tiles,
  .roof-tiles-thickness {
    height: clamp(50px, 13vw, 70px);
  }

  .roof-tiles {
    background-size: 120px 100%;
  }

  .roof-tiles-thickness {
    top: 4px;
  }

  .roof-eave {
    width: calc(100% - 28px);
    height: 24px;
    margin-top: -6px;
  }

  .board-beam {
    margin: 0 4px;
  }

  .board-face {
    margin: 0 4px;
    border-width: 2px;
    padding: 0 8px 8px;
  }

  .board-face::before,
  .board-face::after {
    display: none;
  }

  .board-legs {
    margin: 0 18px;
    height: 28px;
  }

  .leg {
    width: 14px;
  }
}
</style>
