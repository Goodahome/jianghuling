import { http } from './request'
import type { LoginType, SmsScene } from '@/types/api'
import type { AuthResult, MeProfile } from '@/types/models'

export function sendSms(phone: string, scene: SmsScene) {
  return http<{ expireIn: number }>({
    url: '/auth/sms/send',
    method: 'POST',
    data: { phone, scene },
  })
}

export function validateInvite(inviteCode: string) {
  return http<{ valid: boolean; inviterNickname?: string }>({
    url: '/auth/invite/validate',
    method: 'POST',
    data: { inviteCode },
  })
}

export function register(data: {
  inviteCode: string
  phone: string
  smsCode?: string
  username?: string
  password?: string
  nickname: string
}) {
  return http<AuthResult>({ url: '/auth/register', method: 'POST', data })
}

export function login(data: {
  loginType: LoginType
  username?: string
  password?: string
  phone?: string
  smsCode?: string
}) {
  return http<AuthResult>({ url: '/auth/login', method: 'POST', data })
}

export function logout() {
  return http<null>({ url: '/auth/logout', method: 'POST' })
}

export function fetchMe() {
  return http<MeProfile>({ url: '/auth/me', method: 'GET' })
}
