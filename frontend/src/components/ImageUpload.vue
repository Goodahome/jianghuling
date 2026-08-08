<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { uploadFile } from '@/api/file'

const props = withDefaults(
  defineProps<{
    modelValue?: string[]
    limit?: number
    tip?: string
  }>(),
  {
    modelValue: () => [],
    limit: 3,
    tip: '支持图片上传',
  },
)

const emit = defineEmits<{ 'update:modelValue': [string[]] }>()
const uploading = ref(false)

async function onChange(file: { raw?: File }) {
  const raw = file.raw
  if (!raw) return
  if ((props.modelValue?.length || 0) >= props.limit) {
    ElMessage.warning(`最多上传 ${props.limit} 个文件`)
    return
  }
  uploading.value = true
  try {
    const res = await uploadFile(raw)
    emit('update:modelValue', [...(props.modelValue || []), res.url])
    ElMessage.success('上传成功')
  } catch {
    /* request 层已提示 */
  } finally {
    uploading.value = false
  }
}

function remove(url: string) {
  emit(
    'update:modelValue',
    (props.modelValue || []).filter((u) => u !== url),
  )
}
</script>

<template>
  <div class="upload">
    <div v-if="modelValue?.length" class="list">
      <div v-for="url in modelValue" :key="url" class="thumb">
        <img :src="url" alt="" />
        <button type="button" class="rm" @click="remove(url)">移除</button>
      </div>
    </div>
    <el-upload
      v-if="(modelValue?.length || 0) < limit"
      :show-file-list="false"
      :auto-upload="false"
      accept="image/*"
      :disabled="uploading"
      :on-change="onChange"
    >
      <el-button :loading="uploading" size="small">上传图片</el-button>
    </el-upload>
    <p class="tip">{{ tip }}（最多 {{ limit }} 张）</p>
  </div>
</template>

<style scoped>
.upload {
  width: 100%;
}
.list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 8px;
}
.thumb {
  width: 88px;
  border: 1px solid var(--jh-line);
  border-radius: var(--jh-radius);
  overflow: hidden;
  background: transparent;
}
.thumb img {
  display: block;
  width: 88px;
  height: 66px;
  object-fit: cover;
}
.rm {
  display: block;
  width: 100%;
  border: none;
  background: rgba(196, 163, 90, 0.16);
  color: var(--jh-ink);
  font-size: 12px;
  padding: 4px 0;
  cursor: pointer;
}
.tip {
  margin: 6px 0 0;
  font-size: 12px;
  color: var(--jh-muted);
}
</style>
