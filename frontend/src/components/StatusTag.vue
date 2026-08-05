<script setup lang="ts">
import type { BountyStatus } from '@/types/api'
import { bountyStatusLabel } from '@/utils/labels'

const props = defineProps<{ status: BountyStatus | string }>()

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
    {{ bountyStatusLabel[props.status as BountyStatus] || props.status }}
  </el-tag>
</template>
