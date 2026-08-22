import type { AxiosResponse } from 'axios'
import request from '@/services/request'
import { PREFIX_API_THONG_KE_ADMIN } from '@/constants/url'

export interface MarketplaceDashboard {
  gmv: number
  totalSubOrders: number
  completedSubOrders: number
  activeSellers: number
  topSellers: Array<{ sellerId: string; shopName: string; orderCount: number; revenue: number }>
  topProducts: Array<{ productVariantId: string; productName: string; soldCount: number; revenue: number }>
}

export const getMarketplaceDashboard = async () => {
  const res = (await request({ url: `${PREFIX_API_THONG_KE_ADMIN}/marketplace-dashboard`, method: 'GET' })) as AxiosResponse<MarketplaceDashboard>
  return res.data
}
