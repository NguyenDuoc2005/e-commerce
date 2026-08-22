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
  createdAt: string
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
