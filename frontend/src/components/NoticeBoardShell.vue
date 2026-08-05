<script setup lang="ts">
/** 古代遮雨告示板外壳（视觉试验，可整组件拆除回滚） */
defineProps<{
  title?: string
}>()
</script>

<template>
  <div class="notice-stage">
    <div class="notice-board" role="presentation">
      <!-- 加大瓦顶屋檐 -->
      <div class="board-roof" aria-hidden="true">
        <div class="roof-ridge" />
        <div class="roof-tiles" />
        <div class="roof-tiles-shadow" />
        <div class="roof-eave" />
      </div>

      <!-- 遮雨帘 -->
      <div class="rain-curtain" aria-hidden="true">
        <span
          v-for="i in 18"
          :key="i"
          class="curtain-strip"
          :style="{ animationDelay: `${(i % 6) * 0.16}s`, height: `${88 + (i % 3) * 8}%` }"
        />
      </div>

      <!-- 木梁（匾额字交给导航刻字） -->
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
  width: min(1220px, calc(100% - 20px));
  margin: 0 auto;
  padding: 20px 0 36px;
}

.notice-board {
  position: relative;
  filter: drop-shadow(0 22px 40px rgba(12, 10, 6, 0.45));
}

/* —— 加大小青瓦顶 —— */
.board-roof {
  position: relative;
  z-index: 3;
  margin: 0 -28px;
}

.roof-ridge {
  height: 14px;
  margin: 0 10%;
  background: linear-gradient(180deg, #4a5560, #1a2028);
  border-radius: 8px 8px 0 0;
  box-shadow: 0 2px 0 rgba(0, 0, 0, 0.4);
}

.roof-tiles {
  height: clamp(96px, 16vw, 148px);
  background-color: #2f3842;
  background-image: url('/textures/roof-tiles.png');
  background-size: 180px auto;
  background-repeat: repeat-x;
  background-position: center bottom;
  border-radius: 6px 6px 0 0;
  clip-path: polygon(1.5% 0, 98.5% 0, 100% 100%, 0 100%);
  box-shadow: inset 0 -18px 28px rgba(0, 0, 0, 0.4);
}

.roof-tiles-shadow {
  height: 18px;
  margin: -1px 0 0;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0.35), transparent);
}

.roof-eave {
  height: 22px;
  margin: -4px 4px 0;
  background:
    linear-gradient(180deg, #6a4a2a, #3a2816 60%, #24180e);
  border: 1px solid rgba(196, 163, 90, 0.3);
  border-top: none;
  box-shadow:
    0 6px 12px rgba(0, 0, 0, 0.35),
    inset 0 1px 0 rgba(228, 200, 120, 0.2);
}

/* —— 遮雨帘 —— */
.rain-curtain {
  position: relative;
  z-index: 2;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 3px;
  height: 56px;
  margin: 0 14px;
  padding: 0 8px;
  overflow: hidden;
  pointer-events: none;
}

.curtain-strip {
  flex: 1;
  min-width: 0;
  max-width: 26px;
  border-radius: 0 0 12px 12px;
  background:
    linear-gradient(180deg, rgba(90, 24, 20, 0.97), rgba(140, 48, 38, 0.9) 45%, rgba(70, 20, 16, 0.95));
  border: 1px solid rgba(60, 16, 12, 0.45);
  border-top: none;
  transform-origin: top center;
  animation: curtain-sway 3.8s ease-in-out infinite;
  box-shadow: inset 0 -8px 10px rgba(0, 0, 0, 0.22);
}

@keyframes curtain-sway {
  0%,
  100% {
    transform: rotate(-1.4deg) translateY(0);
  }
  50% {
    transform: rotate(1.6deg) translateY(3px);
  }
}

@media (prefers-reduced-motion: reduce) {
  .curtain-strip {
    animation: none;
  }
}

.board-beam {
  position: relative;
  z-index: 2;
  height: 18px;
  margin: -8px 16px 0;
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
  margin: 0 12px;
  min-height: 60vh;
  padding: 0 14px 10px;
  background-color: #3d3224;
  background-image:
    linear-gradient(180deg, rgba(62, 48, 32, 0.55), rgba(42, 34, 24, 0.35)),
    url('/textures/notice-wood.png');
  background-size: auto, 280px 280px;
  border: 3px solid #2a1e12;
  border-top: none;
  box-shadow:
    inset 0 0 0 1px rgba(196, 163, 90, 0.22),
    inset 0 24px 48px rgba(20, 14, 8, 0.18);
}

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

.board-legs {
  display: flex;
  justify-content: space-between;
  margin: 0 32px;
  height: 32px;
}

.leg {
  width: 20px;
  height: 100%;
  background: linear-gradient(90deg, #2a1e12, #5a4228 40%, #2a1e12);
  border-radius: 0 0 3px 3px;
  box-shadow: 0 6px 8px rgba(0, 0, 0, 0.28);
}

@media (max-width: 768px) {
  .notice-stage {
    width: 100%;
    padding: 0 0 16px;
  }

  .board-roof {
    margin: 0 -4px;
  }

  .roof-tiles {
    height: clamp(72px, 22vw, 110px);
    clip-path: polygon(0.5% 0, 99.5% 0, 100% 100%, 0 100%);
  }

  .rain-curtain {
    height: 40px;
    margin: 0 6px;
  }

  .board-beam {
    margin: -6px 6px 0;
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
    height: 20px;
  }

  .leg {
    width: 14px;
  }
}
</style>
