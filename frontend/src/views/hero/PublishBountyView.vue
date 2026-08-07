<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createBounty, getRepublishDraft, republishBounty } from '@/api/bounty'
import { getChecklistTemplates, getRewardSuggest, getWarrantTemplates } from '@/api/meta'
import { getTopNotices } from '@/api/notice'
import type { BountyType, Difficulty } from '@/types/api'
import type { ChecklistTemplate, Notice, RewardSuggest, WarrantTemplate } from '@/types/models'
import { difficultyLabel } from '@/utils/labels'
import JhPageHeader from '@/components/JhPageHeader.vue'
import PageBreadcrumb from '@/components/PageBreadcrumb.vue'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const draftLoading = ref(false)
const suggest = ref<RewardSuggest | null>(null)
const templates = ref<WarrantTemplate[]>([])
const checklists = ref<ChecklistTemplate[]>([])
const tops = ref<Notice[]>([])

const republishFromId = computed(() => {
  const raw = route.query.republishFrom
  const v = Array.isArray(raw) ? raw[0] : raw
  if (!v) return null
  const n = Number(v)
  return Number.isFinite(n) && n > 0 ? n : null
})
const isRepublish = computed(() => republishFromId.value != null)

const crumbs = computed(() => {
  if (isRepublish.value && republishFromId.value) {
    return [
      { label: '悬赏榜', to: '/plaza' },
      { label: '原令详情', to: `/bounties/${republishFromId.value}` },
      { label: '再发一令' },
    ]
  }
  return [
    { label: '悬赏榜', to: '/plaza' },
    { label: '张贴悬赏' },
  ]
})

const form = reactive({
  type: 'RENT_SEEK' as BountyType,
  title: '',
  difficulty: 'NORMAL' as Difficulty,
  rewardAmount: 350,
  deadlineAt: '',
  taskTags: [] as string[],
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  warrantFields: {} as Record<string, any>,
  checklistItemCodes: [] as string[],
  confirmLowReward: false,
})

const currentTemplate = computed(() =>
  templates.value.find((t) => t.type === form.type || (t as { code?: string }).code === form.type),
)
const currentSuggest = computed(() =>
  suggest.value?.difficulties.find((d) => d.code === form.difficulty),
)

const tagOptions = ['帮寻房', '帮带看', '帮验房', '帮谈价', '帮核验真伪']

onMounted(async () => {
  ;[suggest.value, templates.value, tops.value] = await Promise.all([
    getRewardSuggest(),
    getWarrantTemplates(),
    getTopNotices('ANTI_FRAUD', 3).catch(() => []),
  ])
  if (suggest.value) form.rewardAmount = Math.max(suggest.value.minReward, form.rewardAmount)

  if (isRepublish.value) {
    await loadRepublishDraft()
  } else {
    await reloadChecklists()
    initWarrantDefaults()
  }
})

async function loadRepublishDraft() {
  const id = republishFromId.value
  if (!id) return
  draftLoading.value = true
  try {
    const draft = await getRepublishDraft(id)
    form.type = draft.type
    form.title = draft.title || ''
    form.difficulty = draft.difficulty
    form.rewardAmount = Number(draft.rewardAmount) || form.rewardAmount
    form.taskTags = [...(draft.taskTags || [])]
    form.warrantFields = { ...(draft.warrantFields || {}) }
    form.checklistItemCodes = [...(draft.checklistItemCodes || [])]
    form.deadlineAt = ''
    initWarrantDefaults()
    await reloadChecklists()
    // 保留草稿勾选（reload 会合并必选项）
    const fromDraft = draft.checklistItemCodes || []
    form.checklistItemCodes = Array.from(
      new Set([...form.checklistItemCodes, ...fromDraft]),
    )
  } finally {
    draftLoading.value = false
  }
}

async function reloadChecklists() {
  const tags = form.taskTags.length ? form.taskTags.join(',') : undefined
  checklists.value = await getChecklistTemplates(tags)
  const required = checklists.value.filter((c) => c.required).map((c) => c.itemCode)
  const keep = form.checklistItemCodes.filter((code) =>
    checklists.value.some((c) => c.itemCode === code),
  )
  form.checklistItemCodes = Array.from(new Set([...required, ...keep]))
}

watch(
  () => [...form.taskTags],
  () => {
    reloadChecklists()
  },
)

function initWarrantDefaults() {
  const fields = currentTemplate.value?.fields || []
  const next: Record<string, any> = {}
  fields.forEach((f) => {
    next[f.key] = form.warrantFields[f.key] ?? (f.type === 'boolean' ? false : '')
  })
  // 保留模板外已有值（草稿可能含旧 key）
  Object.keys(form.warrantFields || {}).forEach((k) => {
    if (!(k in next)) next[k] = form.warrantFields[k]
  })
  form.warrantFields = next
}

async function onTypeChange() {
  if (isRepublish.value) return
  initWarrantDefaults()
}

async function onSubmit() {
  if (!form.title.trim()) return ElMessage.warning('请填写标题')
  if (!form.deadlineAt) return ElMessage.warning('请选择截止时间')
  const min = suggest.value?.minReward ?? 200
  if (form.rewardAmount < min) return ElMessage.error(`赏银不得低于 ${min} 两`)

  const s = currentSuggest.value
  let confirmLowReward = false
  if (s && form.rewardAmount < s.suggestMin) {
    await ElMessageBox.confirm(
      `当前赏银低于「${difficultyLabel[form.difficulty]}」建议下限 ${s.suggestMin} 两，确认仍要发令？`,
      '赏银偏低提示',
    )
    confirmLowReward = true
  }

  loading.value = true
  try {
    const deadlineAt = new Date(form.deadlineAt).toISOString()
    if (isRepublish.value && republishFromId.value) {
      const created = await republishBounty(republishFromId.value, {
        title: form.title,
        difficulty: form.difficulty,
        rewardAmount: form.rewardAmount,
        confirmLowReward,
        deadlineAt,
        taskTags: form.taskTags,
        warrantFields: form.warrantFields,
        checklistItemCodes: form.checklistItemCodes,
      })
      ElMessage.success('再发一令已提交，赏银已重新冻结，等待审核')
      router.replace(`/bounties/${created.id}`)
    } else {
      const created = await createBounty({
        ...form,
        confirmLowReward,
        deadlineAt,
      })
      ElMessage.success('已提交发令，赏银已冻结，等待审核')
      router.replace(`/bounties/${created.id}`)
    }
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <section class="jh-section" v-loading="draftLoading">
    <div class="jh-container narrow">
      <PageBreadcrumb :items="crumbs" />
      <JhPageHeader
        :title="isRepublish ? '再发一令' : '张贴悬赏令'"
        :subtitle="
          isRepublish
            ? `基于原令 #${republishFromId} 复制新建 · 须重新托管赏银并审核 · 原单不变`
            : `结构化租房令状 · 最低赏银 ${suggest?.minReward ?? 200} 两 · 模拟银两托管`
        "
      />

      <div v-if="tops.length" class="tips jh-panel">
        <strong>发令前必读</strong>
        <RouterLink v-for="n in tops" :key="n.id" :to="`/notices/${n.id}`">{{ n.title }}</RouterLink>
      </div>

      <el-form class="jh-panel form" label-position="top" @submit.prevent="onSubmit">
        <el-form-item label="令种">
          <el-radio-group v-model="form.type" :disabled="isRepublish" @change="onTypeChange">
            <el-radio-button value="RENT_SEEK">求租</el-radio-button>
            <el-radio-button value="RENT_OUT">出租/转租</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="标题" required>
          <el-input v-model="form.title" maxlength="60" show-word-limit />
        </el-form-item>
        <el-form-item label="难易度">
          <el-select v-model="form.difficulty" style="width: 100%">
            <el-option
              v-for="d in suggest?.difficulties || []"
              :key="d.code"
              :label="`${d.name}（建议 ${d.suggestMin}-${d.suggestMax} 两）`"
              :value="d.code"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="赏银（两）" required>
          <el-input-number v-model="form.rewardAmount" :min="0" :step="50" style="width: 100%" />
        </el-form-item>
        <el-form-item label="截止时间" required>
          <el-date-picker
            v-model="form.deadlineAt"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%"
            placeholder="再发须重新选择截止时间"
          />
        </el-form-item>
        <el-form-item label="任务标签">
          <el-select v-model="form.taskTags" multiple style="width: 100%" placeholder="选择后预勾探子清单">
            <el-option v-for="t in tagOptions" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>

        <h3>令状字段</h3>
        <template v-if="currentTemplate">
          <el-form-item
            v-for="field in currentTemplate.fields"
            :key="field.key"
            :label="field.label"
            :required="field.required"
          >
            <el-switch
              v-if="field.type === 'boolean'"
              v-model="form.warrantFields[field.key]"
            />
            <el-input
              v-else-if="field.type === 'textarea'"
              v-model="form.warrantFields[field.key]"
              type="textarea"
              :rows="3"
            />
            <el-select
              v-else-if="field.type === 'select'"
              v-model="form.warrantFields[field.key]"
              style="width: 100%"
            >
              <el-option
                v-for="opt in field.options || []"
                :key="String(opt.value)"
                :label="opt.label"
                :value="opt.value"
              />
            </el-select>
            <el-input
              v-else
              v-model="form.warrantFields[field.key]"
              :type="field.type === 'number' ? 'number' : 'text'"
              :placeholder="field.placeholder || field.label"
            />
          </el-form-item>
        </template>
        <el-empty v-else description="令状模板加载中或暂无配置" />

        <h3>探子清单</h3>
        <el-checkbox-group v-model="form.checklistItemCodes">
          <el-checkbox
            v-for="c in checklists"
            :key="c.itemCode"
            :value="c.itemCode"
            :disabled="c.required"
          >
            {{ c.itemName }}
            <el-tag v-if="c.required" size="small" type="danger">必选</el-tag>
          </el-checkbox>
        </el-checkbox-group>

        <el-button
          class="jh-btn-seal submit"
          type="primary"
          native-type="submit"
          :loading="loading"
        >
          {{ isRepublish ? '再发并冻结赏银提交审核' : '冻结赏银并提交审核' }}
        </el-button>
      </el-form>
    </div>
  </section>
</template>

<style scoped>
.narrow {
  max-width: 760px;
}
h1 {
  margin: 0 0 6px;
  font-size: 36px;
}
.tips {
  margin: 16px 0;
  padding: 12px 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px 16px;
}
.tips a {
  color: var(--jh-seal);
}
.form {
  padding: 20px;
  margin-top: 12px;
}
h3 {
  margin: 18px 0 10px;
  font-family: var(--jh-font-display);
}
.submit {
  width: 100%;
  margin-top: 20px;
}
@media (max-width: 768px) {
  h1 {
    font-size: 28px;
  }
  .form {
    padding: 14px;
  }
  .form :deep(.el-checkbox-group) {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
  .form :deep(.el-date-editor) {
    width: 100% !important;
  }
  .form :deep(.el-input-number) {
    width: 100%;
  }
}
</style>
