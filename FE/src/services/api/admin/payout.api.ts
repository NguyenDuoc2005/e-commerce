import type { AxiosResponse } from 'axios'
import request from '@/services/request'
import { API_URL } from '@/constants/url'

export interface CommissionConfig {
  id: string
  categoryId?: string
  ratePercent: number
  active: boolean
  createdAt: string
}

export interface AdminReceivable {
  id: string
  orderSellerId: string
  orderId?: string
  sellerId: string
  grossAmount: number
  commissionRate: number
  commissionAmount: number
  netAmount: number
  status: string
  availableAt?: string
  releasedAmount: number
  paidAt?: string
  payoutBatchId?: string
  createdAt: string
}

export interface PayoutBatch {
  id: string
  referenceCode: string
  status: string
  itemCount: number
  totalAmount: number
  createdByStaffId?: string
  note?: string
  createdAt: string
  paidAt: string
}

const baseUrl = `${API_URL}/admin/payout`

export const getCommissionConfigs = async () => {
  const response = (await request({ url: `${baseUrl}/commission-configs`, method: 'GET' })) as AxiosResponse<CommissionConfig[]>
  return response.data
}

export const createCommissionConfig = async (data: { categoryId?: string; ratePercent: number }) => {
  const response = (await request({ url: `${baseUrl}/commission-configs`, method: 'POST', data })) as AxiosResponse<CommissionConfig>
  return response.data
}

export const getAdminReceivables = async () => {
  const response = (await request({ url: `${baseUrl}/receivables`, method: 'GET' })) as AxiosResponse<AdminReceivable[]>
  return response.data
}

export const payAdminReceivable = async (id: string) => {
  const response = (await request({ url: `${baseUrl}/receivables/${id}/pay`, method: 'POST' })) as AxiosResponse<AdminReceivable>
  return response.data
}

export const releaseEligibleReceivables = async () => {
  const response = (await request({ url: `${baseUrl}/receivables/release-eligible`, method: 'POST' })) as AxiosResponse<AdminReceivable[]>
  return response.data
}

export const getPayoutBatches = async () => {
  const response = (await request({ url: `${baseUrl}/batches`, method: 'GET' })) as AxiosResponse<PayoutBatch[]>
  return response.data
}

export const createPayoutBatch = async (data: { receivableIds: string[]; note?: string }) => {
  const response = (await request({ url: `${baseUrl}/batches`, method: 'POST', data })) as AxiosResponse<PayoutBatch>
  return response.data
}
