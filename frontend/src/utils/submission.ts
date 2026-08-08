/**
 * 成果详情/列表字段归一化（对齐 api.md §8.0 / §8.2）。
 * 兼容旧键 id/userId，并在缺 itemName 时回退探子清单常用中文名。
 */
import type {
  BountySubmissionListItem,
  SubmissionDetail,
  SubmissionItemDetail,
} from '@/types/models'
import type { SubmissionStatus } from '@/types/api'

/** 与 data.sql checklist_template 默认种子对齐；仅作展示回退，正式以接口 itemName 为准 */
const CHECKLIST_NAME_FALLBACK: Record<string, string> = {
  VERIFY_AUTHENTIC: '核验房源真实性',
  SITE_VISIT_RECORD: '现场带看记录',
  PHOTO_EVIDENCE: '现场照片/视频',
  NEIGHBORHOOD_NOTE: '周边配套备注',
  CONTRACT_HINT: '合同/中介风险提示',
  LANDLORD_CONTACT: '房东沟通记录',
}

function asRecord(raw: unknown): Record<string, unknown> {
  return raw && typeof raw === 'object' ? (raw as Record<string, unknown>) : {}
}

function asNum(v: unknown): number {
  const n = Number(v)
  return Number.isFinite(n) ? n : 0
}

function asStr(v: unknown, fallback = ''): string {
  if (v == null) return fallback
  return String(v)
}

export function checklistItemDisplayName(itemCode: string, itemName?: string | null): string {
  const name = (itemName || '').trim()
  if (name) return name
  return CHECKLIST_NAME_FALLBACK[itemCode] || itemCode || '清单项'
}

export function normalizeSubmissionItem(raw: unknown): SubmissionItemDetail {
  const r = asRecord(raw)
  const itemCode = asStr(r.itemCode || r.checklistItemCode)
  const media = r.mediaUrls
  return {
    itemCode,
    itemName: checklistItemDisplayName(itemCode, r.itemName as string | null | undefined),
    done: Boolean(r.done),
    text: (r.text as string | null | undefined) ?? null,
    mediaUrls: Array.isArray(media) ? media.map(String) : [],
  }
}

export function normalizeSubmissionDetail(raw: unknown): SubmissionDetail {
  const r = asRecord(raw)
  const submissionId = asNum(r.submissionId ?? r.id)
  const claimerUserId = asNum(r.claimerUserId ?? r.userId)
  const itemsRaw = Array.isArray(r.items) ? r.items : []
  return {
    submissionId,
    bountyId: asNum(r.bountyId),
    bountyTitle: (r.bountyTitle as string | null | undefined) ?? null,
    claimId: asNum(r.claimId),
    claimerUserId,
    claimerNickname: asStr(r.claimerNickname),
    versionNo: asNum(r.versionNo) || 1,
    status: asStr(r.status, 'PENDING') as SubmissionStatus,
    summary: asStr(r.summary ?? r.contentSummary),
    items: itemsRaw.map(normalizeSubmissionItem),
    reviewReason: (r.reviewReason ?? r.rejectReason) as string | null | undefined,
    reviewedAt: (r.reviewedAt as string | null | undefined) ?? null,
    createdAt: asStr(r.createdAt),
    updatedAt: r.updatedAt != null ? asStr(r.updatedAt) : undefined,
  }
}

export function normalizeSubmissionListItem(raw: unknown): BountySubmissionListItem {
  const r = asRecord(raw)
  return {
    submissionId: asNum(r.submissionId ?? r.id),
    bountyId: asNum(r.bountyId),
    claimId: asNum(r.claimId),
    claimerUserId: asNum(r.claimerUserId ?? r.userId),
    claimerNickname: asStr(r.claimerNickname),
    versionNo: asNum(r.versionNo) || 1,
    status: asStr(r.status, 'PENDING') as SubmissionStatus,
    summary: asStr(r.summary ?? r.contentSummary),
    createdAt: asStr(r.createdAt),
    reviewedAt: (r.reviewedAt as string | null | undefined) ?? null,
    reviewReason: (r.reviewReason ?? r.rejectReason) as string | null | undefined,
  }
}

export function submissionClaimerLabel(d: {
  claimerNickname?: string | null
  claimerUserId?: number | null
}): string {
  const nick = (d.claimerNickname || '').trim()
  if (nick) return nick
  const id = d.claimerUserId
  return id ? `侠士#${id}` : '佚名侠士'
}
