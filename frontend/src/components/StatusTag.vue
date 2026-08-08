<script setup lang="ts">
import type { BountyStatus } from '@/types/api'
import { resolveBountyStatusLabel, type BountyStatusScene } from '@/utils/labels'

const props = withDefaults(
  defineProps<{
    status: BountyStatus | string
    /** plaza=悬赏中；mine=进行中；default=协作中 */
    scene?: BountyStatusScene
  }>(),
  { scene: 'default' },
)

const typeMap: Record<string, '' | 'success' | 'warning' | 'info' | 'danger'> = {
  PENDING_REVIEW: 'warning',
  OPEN: 'success',
  IN_COLLAB: '',
  PENDING_SETTLE: 'warning',
  COMPLETED: 'info',
  REJECTED: 'danger',
  CANCELLED: 'info',
  IN_DISPUTE: 'danger',
}
</script>

<template>
  <el-tag :type="typeMap[props.status] || 'info'" effect="plain" size="small">
    {{ resolveBountyStatusLabel(props.status, props.scene) }}
  </el-tag>
</template>
