import type { AxiosResponse } from 'axios'
import request from '@/services/request'
import { PREFIX_API_SELLER_PAYOUT } from '@/constants/url'

export interface SellerWallet {
  id: string
  sellerId: string
  pendingAmount: number
  availableAmount: number
  paidAmount: number
}

export interface SellerReceivable {
  id: string
  orderSellerId: string
  orderId: string
  sellerId: string
  grossAmount: number
  commissionRate: number
  commissionAmount: number
  netAmount: number
  releasedAmount: number
  status: string
  availableAt?: string
  paidAt?: string
  payoutBatchId?: string
  createdAt: string
}

export const getSellerWallet = async () => {
  const res = (await request({
    url: `${PREFIX_API_SELLER_PAYOUT}/wallet`,
    method: 'GET'
  })) as AxiosResponse<SellerWallet>

  return res.data
}

export const getSellerReceivables = async () => {
  const res = (await request({
    url: `${PREFIX_API_SELLER_PAYOUT}/receivables`,
    method: 'GET'
  })) as AxiosResponse<SellerReceivable[]>

  return res.data
}
