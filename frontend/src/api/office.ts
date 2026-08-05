import { http } from './request'
import type { OfficeCode } from '@/types/api'
import type { OfficeBrief, OfficeDef } from '@/types/models'

export function listOfficeDefs() {
  return http<OfficeDef[]>({ url: '/offices/defs', method: 'GET' })
}

export function applyOffice(officeCode: OfficeCode | string, statement: string) {
  return http<null>({
    url: '/offices/applications',
    method: 'POST',
    data: { officeCode, statement },
  })
}

export function getMyOffices() {
  return http<OfficeBrief[]>({ url: '/offices/mine', method: 'GET' })
}

export function getMyOfficeApplications() {
  return http<Record<string, unknown>[]>({ url: '/offices/applications/mine', method: 'GET' })
}
