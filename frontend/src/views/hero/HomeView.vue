<template>
  <section class="hero-band">
    <div class="band-inner">
      <div class="copy">
        <p class="eyebrow">遵义城 · 告示牌前</p>
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
  </section>
</template>

<style scoped>
.hero-band {
  min-height: auto;
  display: flex;
  align-items: center;
  padding: 28px clamp(12px, 4vw, 32px) 12px;
  color: #f7f0dd;
}
.band-inner {
  width: 100%;
  max-width: 1120px;
  margin: 0 auto;
}
.copy {
  max-width: 36em;
  padding: 18px 20px;
  background: rgba(28, 22, 16, 0.45);
  border: 1px solid rgba(196, 163, 90, 0.35);
}
.eyebrow {
  margin: 0 0 8px;
  font-size: 13px;
  letter-spacing: 0.28em;
  color: var(--jh-gold-bright);
}
.brand-title {
  margin: 0;
  font-size: clamp(36px, 6vw, 56px);
  line-height: 1.1;
  letter-spacing: 0.12em;
}
.slogan {
  margin: 12px 0 6px;
  font-family: var(--jh-font-display);
  font-size: clamp(18px, 2.8vw, 24px);
  letter-spacing: 0.06em;
  line-height: 1.4;
  color: var(--jh-gold-bright);
}
.belief {
  margin: 0 0 20px;
  font-size: 15px;
  line-height: 1.6;
  color: rgba(247, 240, 221, 0.8);
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
    padding: 16px 8px 8px;
  }
  .cta-row {
    flex-direction: column;
  }
  .btn {
    width: 100%;
  }
}
</style>
