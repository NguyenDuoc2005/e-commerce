import type { AxiosResponse } from 'axios'
import request from '@/services/request'
import { PREFIX_API_SELLER_PRODUCTS } from '@/constants/url'
import type { DefaultResponse } from '@/types/api.common'

export interface LowStockProduct {
  id: string
  code: string
  productName: string
  quantity: number
  imageUrl?: string
}

export const getSellerLowStock = async (threshold = 5) => {
  const res = (await request({
    url: `${PREFIX_API_SELLER_PRODUCTS}/low-stock-variants`,
    method: 'GET',
    params: { threshold }
  })) as AxiosResponse<DefaultResponse<LowStockProduct[]>>
  return res.data
}
