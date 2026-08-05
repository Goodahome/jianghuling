<script setup lang="ts">
import { useRouter } from 'vue-router'

const props = withDefaults(
  defineProps<{
    /** 明确回退目标（优先于浏览器历史，保证回待审列表） */
    to?: string
    label?: string
  }>(),
  { label: '返回上一页' },
)

const router = useRouter()

function goBack() {
  if (props.to) {
    router.push(props.to)
    return
  }
  if (window.history.length > 1) {
    router.back()
    return
  }
  router.push('/hall')
}
</script>

<template>
  <div class="hall-back">
    <button type="button" class="back-btn" @click="goBack">
      <span class="arrow" aria-hidden="true">←</span>
      {{ label }}
    </button>
    <div v-if="$slots.default" class="hall-back-actions">
      <slot />
    </div>
  </div>
</template>

<style scoped>
.hall-back {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 14px;
}
.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-height: 40px;
  padding: 8px 14px;
  border: 1px solid var(--jh-line);
  border-radius: var(--jh-radius);
  background: #fff;
  color: var(--jh-ink);
  font: inherit;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
}
.back-btn:hover {
  border-color: var(--jh-seal);
  color: var(--jh-seal);
}
.arrow {
  font-size: 16px;
  line-height: 1;
}
.hall-back-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}
</style>
