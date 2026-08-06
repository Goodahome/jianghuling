<template>
  <section class="hero-band">
    <div class="band-inner">
      <div class="copy">
        <h1 class="brand-title">江湖令</h1>
        <p class="slogan">天下有悬赏，江湖有侠士。</p>
        <p class="belief">江湖不让善意吃亏。</p>
        <div class="cta-row">
          <RouterLink class="btn primary" to="/plaza">进入悬赏广场</RouterLink>
          <RouterLink class="btn ghost" to="/bounties/publish">张贴悬赏</RouterLink>
          <RouterLink class="btn ghost" to="/notices">先看告示</RouterLink>
        </div>
      </div>
    </div>

    <!-- 挂到告示板木面，贴底且不挤占页脚文档流 -->
    <Teleport to=".hero-app .board-face" defer>
      <aside class="home-swordsman" aria-hidden="true">
        <img
          class="swordsman-img"
          src="/textures/home-hero-swordsman.png?v=2"
          alt=""
          width="480"
          height="640"
          decoding="async"
        />
      </aside>
    </Teleport>
  </section>
</template>

<style scoped>
.hero-band {
  min-height: auto;
  display: flex;
  align-items: flex-start;
  padding: 28px clamp(12px, 4vw, 32px) 12px;
}
.band-inner {
  width: 100%;
  max-width: 1120px;
  margin: 0 auto;
  min-width: 0;
}
.copy {
  max-width: min(36em, 58%);
  padding: 8px 0 12px;
  background: none;
  border: none;
  position: relative;
  z-index: 1;
}
/* 金榜色 + 轻阴刻；字号按内容区缩放反补，避免嵌套 zoom 叠影 */
.brand-title,
.slogan,
.belief {
  font-family: var(--jh-font-display);
  color: var(--jh-gold-bright);
  text-shadow: var(--jh-gold-text-shine);
}
.brand-title {
  margin: 0;
  font-size: clamp(
    calc(36px / var(--jh-content-scale)),
    calc(6vw / var(--jh-content-scale)),
    calc(56px / var(--jh-content-scale))
  );
  line-height: 1.1;
  letter-spacing: 0.12em;
  font-weight: normal;
}
.slogan {
  margin: 12px 0 6px;
  font-size: clamp(
    calc(18px / var(--jh-content-scale)),
    calc(2.8vw / var(--jh-content-scale)),
    calc(24px / var(--jh-content-scale))
  );
  letter-spacing: 0.06em;
  line-height: 1.4;
}
.belief {
  margin: 0 0 20px;
  padding-left: 1ch;
  font-size: calc(15px / var(--jh-content-scale));
  letter-spacing: 0.06em;
  line-height: 1.4;
}
.cta-row {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 10px 16px;
  min-height: 40px;
  border-radius: 2px;
  border: 1px solid transparent;
  font-family: var(--jh-font-display);
  letter-spacing: 0.12em;
}
.btn.primary {
  background: linear-gradient(180deg, #c9a24a, var(--jh-gold-deep));
  color: #1c1408;
  border-color: var(--jh-gold-bright);
}
.btn.ghost {
  border-color: rgba(228, 200, 120, 0.55);
  color: var(--jh-gold-bright);
}

@media (max-width: 768px) {
  .hero-band {
    padding: 12px 4px 8px;
    min-width: 0;
  }
  .band-inner {
    max-width: 100%;
  }
  .copy {
    max-width: 100%;
    padding: 4px 0 0;
    box-sizing: border-box;
  }
  .brand-title {
    font-size: clamp(
      calc(30px / var(--jh-content-scale)),
      calc(11vw / var(--jh-content-scale)),
      calc(42px / var(--jh-content-scale))
    );
    letter-spacing: 0.08em;
    word-break: keep-all;
  }
  .slogan {
    font-size: clamp(
      calc(16px / var(--jh-content-scale)),
      calc(4.5vw / var(--jh-content-scale)),
      calc(20px / var(--jh-content-scale))
    );
    letter-spacing: 0.04em;
  }
  .belief {
    margin-bottom: 16px;
    font-size: calc(14px / var(--jh-content-scale));
    padding-left: 0;
  }
  .cta-row {
    flex-direction: column;
    gap: 8px;
  }
  .btn {
    width: 100%;
    box-sizing: border-box;
  }
}
</style>

<!-- Teleport 内容在 board-face 下，需非 scoped 才能稳贴木面底 -->
<style>
.hero-app .board-face {
  display: flex;
  flex-direction: column;
}
.hero-app:has(.hero-band) .page-main {
  flex: 1 1 auto;
  min-height: 0;
}
.hero-app:has(.hero-band) .footer {
  position: relative;
  z-index: 3;
  flex: 0 0 auto;
  /* 右侧留给侠士站位，避免页脚被视觉挤压 */
  padding-right: min(42%, 420px);
}
.home-swordsman {
  position: absolute;
  right: clamp(8px, 2vw, 28px);
  bottom: 0;
  /* 顶部留出顶栏菜单高度，避免头部被木牌按钮挡住 */
  top: 4.75rem;
  z-index: 1;
  width: min(38%, 360px);
  margin: 0;
  pointer-events: none;
  line-height: 0;
  display: flex;
  align-items: flex-end;
  justify-content: flex-end;
}
.home-swordsman .swordsman-img {
  display: block;
  width: auto;
  max-width: 100%;
  height: auto;
  max-height: 100%;
  object-fit: contain;
  object-position: right bottom;
  filter: saturate(0.92) contrast(1.04);
}
@media (max-width: 768px) {
  .hero-app:has(.hero-band) .footer {
    padding-right: 0;
  }
  .home-swordsman {
    right: 4px;
    top: 3.75rem;
    width: min(46%, 200px);
    opacity: 0.92;
  }
}
</style>
