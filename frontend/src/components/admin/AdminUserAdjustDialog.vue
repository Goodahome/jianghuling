<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { adminAdjustAssets } from '@/api/admin'

const props = defineProps<{
  modelValue: boolean
  userId: number | string | null
}>()

const emit = defineEmits<{
  'update:modelValue': [boolean]
  success: []
}>()

const submitting = ref(false)
const form = reactive({
  assetType: 'BALANCE',
  delta: 0 as number | undefined,
  reason: '',
})

watch(
  () => props.modelValue,
  (open) => {
    if (open) {
      form.assetType = 'BALANCE'
      form.delta = 0
      form.reason = ''
    }
  },
)

function close() {
  emit('update:modelValue', false)
}

async function submit() {
  if (props.userId == null || props.userId === '') {
    ElMessage.error('缺少侠士 ID')
    return
  }
  const delta = Number(form.delta)
  if (!Number.isFinite(delta)) {
    ElMessage.error('请填写有效调整量')
    return
  }
  const reason = form.reason.trim()
  if (!reason) {
    ElMessage.error('请填写调账原因')
    return
  }
  submitting.value = true
  try {
    await adminAdjustAssets(props.userId, {
      assetType: form.assetType,
      delta,
      reason,
    })
    ElMessage.success('已调账')
    emit('success')
    close()
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    title="资产调账"
    width="420px"
    destroy-on-close
    @update:model-value="emit('update:modelValue', $event)"
  >
    <el-form label-width="88px" @submit.prevent>
      <el-form-item label="资产类型" required>
        <el-select v-model="form.assetType" style="width: 100%">
          <el-option label="银两余额 BALANCE" value="BALANCE" />
          <el-option label="侠义 CHIVALRY" value="CHIVALRY" />
          <el-option label="体力 STAMINA" value="STAMINA" />
        </el-select>
      </el-form-item>
      <el-form-item label="调整量" required>
        <el-input-number
          v-model="form.delta"
          :step="form.assetType === 'BALANCE' ? 10 : 1"
          :precision="form.assetType === 'BALANCE' ? 2 : 0"
          controls-position="right"
          style="width: 100%"
        />
        <div class="hint">正数增加，负数扣减</div>
      </el-form-item>
      <el-form-item label="原因" required>
        <el-input v-model="form.reason" type="textarea" :rows="2" maxlength="200" show-word-limit placeholder="必填，写入审计" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="close">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submit">确认调账</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.hint {
  margin-top: 4px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>
