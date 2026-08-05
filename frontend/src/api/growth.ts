import { http } from './request'
import type { PageQuery, PageResult } from '@/types/api'
import type { LevelProgress, Product } from '@/types/models'

export function exchangeStamina(staminaPoints: number) {
  return http<null>({
    url: '/growth/stamina/exchange',
    method: 'POST',
    data: { staminaPoints },
  })
}

export function listProducts(params: PageQuery) {
  return http<PageResult<Product>>({ url: '/growth/products', method: 'GET', params })
}

export function redeemProduct(productId: number | string, quantity = 1) {
  return http<null>({
    url: `/growth/products/${productId}/redeem`,
    method: 'POST',
    data: { quantity },
  })
}

export function listRedeemOrders(params: PageQuery) {
  return http<PageResult<Record<string, unknown>>>({
    url: '/growth/redeem-orders',
    method: 'GET',
    params,
  })
}

export function getLevelProgress() {
  return http<LevelProgress>({ url: '/growth/level', method: 'GET' })
}
