<template>
  <section class="hero-band">
    <div class="band-inner">
      <div class="copy">
        <p class="entry-mark">义帖既出 · 四方有应</p>
        <h1 class="brand-title">江湖令</h1>
        <p class="slogan">天下有悬赏，江湖有侠士。</p>
        <div class="ink-divider" aria-hidden="true" />
        <p class="belief">以告示立信，以悬赏成事，让每一份托付都有回响。</p>
        <div class="cta-row">
          <RouterLink class="btn primary" to="/plaza">进入悬赏榜</RouterLink>
          <RouterLink class="btn ghost" to="/bounties/publish">张贴悬赏</RouterLink>
          <RouterLink class="btn ghost" to="/notices">先看告示</RouterLink>
        </div>
        <div class="hero-stats" aria-label="江湖令特点">
          <article class="hero-stat">
            <span class="stat-label">告示立信</span>
            <strong>规则公开</strong>
            <p>悬赏、验功、结算皆可循迹，不让托付落空。</p>
          </article>
          <article class="hero-stat">
            <span class="stat-label">侠士应令</span>
            <strong>聚人办事</strong>
            <p>寻人、护送、查物、协作，都能在江湖中找到回应。</p>
          </article>
          <article class="hero-stat">
            <span class="stat-label">信义留名</span>
            <strong>榜上可见</strong>
            <p>从告示栏到英雄榜，让声望、履历与成果沉淀下来。</p>
          </article>
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
  max-width: min(40rem, 58%);
  padding: 18px 0 16px;
  position: relative;
  position: relative;
  z-index: 1;
}
.copy::before {
  content: '';
  position: absolute;
  inset: 0 -12px 0 -26px;
  border-radius: 14px;
  background:
    linear-gradient(90deg, rgba(16, 11, 7, 0.5) 0%, rgba(16, 11, 7, 0.22) 62%, rgba(16, 11, 7, 0) 100%);
  border: 1px solid rgba(228, 200, 120, 0.12);
  box-shadow:
    inset 0 1px 0 rgba(255, 244, 210, 0.06),
    0 12px 28px rgba(10, 7, 4, 0.16);
  pointer-events: none;
}
/* 金榜色 + 轻阴刻；字号按内容区缩放反补，避免嵌套 zoom 叠影 */
.entry-mark,
.brand-title,
.slogan,
.belief {
  font-family: var(--jh-font-display);
  color: var(--jh-gold-bright);
  text-shadow: var(--jh-gold-text-shine);
}
.entry-mark {
  margin: 0 0 12px;
  font-size: calc(12px / var(--jh-content-scale));
  letter-spacing: 0.34em;
  color: rgba(228, 200, 120, 0.82);
  text-transform: uppercase;
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
  margin: 14px 0 8px;
  font-size: clamp(
    calc(18px / var(--jh-content-scale)),
    calc(2.8vw / var(--jh-content-scale)),
    calc(24px / var(--jh-content-scale))
  );
  letter-spacing: 0.06em;
  line-height: 1.4;
}
.ink-divider {
  width: min(160px, 38%);
  height: 1px;
  margin: 12px 0 14px;
  background: linear-gradient(90deg, rgba(228, 200, 120, 0.85), rgba(228, 200, 120, 0.15));
}
.belief {
  margin: 0 0 22px;
  max-width: 30em;
  padding-left: 0;
  font-size: calc(16px / var(--jh-content-scale));
  letter-spacing: 0.05em;
  line-height: 1.8;
}
.cta-row {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 18px;
}
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 11px 18px;
  min-height: 42px;
  border-radius: 2px;
  border: 1px solid transparent;
  font-family: var(--jh-font-display);
  letter-spacing: 0.12em;
  transition:
    transform 0.18s ease,
    filter 0.18s ease,
    border-color 0.18s ease;
}
.btn:hover {
  transform: translateY(-1px);
  filter: brightness(1.03);
}
.btn.primary {
  background: linear-gradient(180deg, #c9a24a, var(--jh-gold-deep));
  color: #1c1408;
  border-color: var(--jh-gold-bright);
  box-shadow:
    inset 0 1px 0 rgba(255, 244, 210, 0.45),
    0 6px 14px rgba(18, 12, 6, 0.22);
}
.btn.ghost {
  border-color: rgba(228, 200, 120, 0.55);
  color: var(--jh-gold-bright);
  background: linear-gradient(180deg, rgba(68, 49, 28, 0.46), rgba(38, 28, 18, 0.28));
}
.hero-stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  max-width: min(44rem, 100%);
}
.hero-stat {
  position: relative;
  min-width: 0;
  padding: 14px 14px 15px;
  background:
    linear-gradient(180deg, rgba(248, 238, 214, 0.15), rgba(102, 78, 45, 0.12)),
    rgba(24, 18, 12, 0.46);
  border: 1px solid rgba(228, 200, 120, 0.2);
  box-shadow:
    inset 0 1px 0 rgba(255, 244, 210, 0.08),
    0 10px 24px rgba(8, 6, 4, 0.14);
}
.hero-stat::before {
  content: '';
  position: absolute;
  inset: 0 auto 0 0;
  width: 2px;
  background: linear-gradient(180deg, rgba(228, 200, 120, 0.95), rgba(228, 200, 120, 0));
}
.stat-label {
  display: inline-block;
  margin-bottom: 6px;
  font-family: var(--jh-font-display);
  font-size: calc(11px / var(--jh-content-scale));
  letter-spacing: 0.18em;
  color: rgba(228, 200, 120, 0.76);
}
.hero-stat strong {
  display: block;
  margin-bottom: 6px;
  font-family: var(--jh-font-display);
  font-size: calc(18px / var(--jh-content-scale));
  font-weight: normal;
  letter-spacing: 0.1em;
  color: #f6e7bd;
}
.hero-stat p {
  margin: 0;
  color: rgba(247, 240, 221, 0.8);
  font-size: calc(13px / var(--jh-content-scale));
  line-height: 1.7;
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
    padding: 10px 0 0;
    box-sizing: border-box;
  }
  .copy::before {
    inset: 0;
    border-radius: 10px;
  }
  .entry-mark {
    margin-bottom: 8px;
    letter-spacing: 0.22em;
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
  .ink-divider {
    width: 132px;
    margin: 10px 0 12px;
  }
  .belief {
    margin-bottom: 16px;
    font-size: calc(14px / var(--jh-content-scale));
  }
  .cta-row {
    flex-direction: column;
    gap: 8px;
  }
  .btn {
    width: 100%;
    box-sizing: border-box;
  }
  .hero-stats {
    grid-template-columns: 1fr;
    gap: 8px;
  }
  .hero-stat {
    padding: 12px 12px 13px;
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
