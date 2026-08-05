import { http } from './request'
import type {
  ChecklistTemplate,
  GrowthConfig,
  RewardSuggest,
  WarrantTemplate,
} from '@/types/models'

export function getRewardSuggest() {
  return http<RewardSuggest>({ url: '/meta/reward-suggest', method: 'GET' })
}

export function getWarrantTemplates() {
  return http<WarrantTemplate[]>({ url: '/meta/warrant-templates', method: 'GET' })
}

export function getChecklistTemplates(tags?: string) {
  return http<ChecklistTemplate[]>({
    url: '/meta/checklist-templates',
    method: 'GET',
    params: { tags },
  })
}

export function getGrowthConfig() {
  return http<GrowthConfig>({ url: '/meta/growth-config', method: 'GET' })
}
