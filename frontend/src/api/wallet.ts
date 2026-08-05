import { http } from './request'
import type { PageQuery, PageResult } from '@/types/api'
import type { WalletAccount, WalletLedger } from '@/types/models'

export function getWalletAccount() {
  return http<WalletAccount>({ url: '/wallet/account', method: 'GET' })
}

export function recharge(amount: number, clientRequestId: string) {
  return http<WalletAccount & { bizNo?: string }>({
    url: '/wallet/recharge',
    method: 'POST',
    data: { amount, clientRequestId },
  })
}

export function withdraw(amount: number, clientRequestId: string) {
  return http<WalletAccount & { bizNo?: string }>({
    url: '/wallet/withdraw',
    method: 'POST',
    data: { amount, clientRequestId },
  })
}

export function listLedgers(params: PageQuery & { type?: string }) {
  return http<PageResult<WalletLedger>>({
    url: '/wallet/ledgers',
    method: 'GET',
    params,
  })
}
