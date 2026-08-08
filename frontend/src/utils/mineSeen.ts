/**
 * 「我的悬赏」关注水位：对比本地已读快照，判断新会话 / 新成果 / 状态变更。
 * 打开某令详情后更新该令快照；协作未读仍以接口 unreadCollabCount 为准（进会话清零）。
 */
import type { BountyListItem } from '@/types/models'

const STORAGE_KEY = 'jh_mine_bounty_seen_v1'

export type MineSeenSnap = {
  status: string
  submissionCount: number
}

function readMap(): Record<string, MineSeenSnap> {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return {}
    const parsed = JSON.parse(raw) as Record<string, MineSeenSnap>
    return parsed && typeof parsed === 'object' ? parsed : {}
  } catch {
    return {}
  }
}

function writeMap(map: Record<string, MineSeenSnap>) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(map))
  } catch {
    /* ignore quota */
  }
}

/** 首次见到的令写入基线，不立刻亮红点 */
export function ensureMineSeenBaseline(items: BountyListItem[]) {
  const map = readMap()
  let dirty = false
  for (const item of items) {
    const key = String(item.id)
    if (!map[key]) {
      map[key] = {
        status: String(item.status || ''),
        submissionCount: Number(item.submissionCount ?? 0),
      }
      dirty = true
    }
  }
  if (dirty) writeMap(map)
}

export function markMineBountySeen(item: Pick<BountyListItem, 'id' | 'status' | 'submissionCount'>) {
  const map = readMap()
  map[String(item.id)] = {
    status: String(item.status || ''),
    submissionCount: Number(item.submissionCount ?? 0),
  }
  writeMap(map)
}

export function markMineBountySeenByFields(
  id: number | string,
  status: string,
  submissionCount: number,
) {
  markMineBountySeen({ id: Number(id), status: status as BountyListItem['status'], submissionCount })
}

/** 宣纸是否应亮红点 */
export function hasMinePosterAttention(item: BountyListItem): boolean {
  if (Number(item.unreadCollabCount ?? 0) > 0) return true
  const snap = readMap()[String(item.id)]
  if (!snap) return false
  if (Number(item.submissionCount ?? 0) > Number(snap.submissionCount || 0)) return true
  if (String(item.status || '') !== String(snap.status || '')) return true
  return false
}

export function clearMineSeenStorage() {
  try {
    localStorage.removeItem(STORAGE_KEY)
  } catch {
    /* ignore */
  }
}
