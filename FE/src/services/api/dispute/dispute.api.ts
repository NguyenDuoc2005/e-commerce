import type { AxiosResponse } from 'axios'
import request from '@/services/request'
import { API_URL } from '@/constants/url'

export type DisputeStatus = 'OPEN' | 'SELLER_RESPONDED' | 'UNDER_ADMIN_REVIEW' | 'RESOLVED_REFUND_BUYER' | 'RESOLVED_REJECT_BUYER' | 'RESOLVED_PARTIAL_REFUND' | 'CLOSED'
export interface DisputeMessage { id: string; senderType: 'BUYER' | 'SELLER' | 'ADMIN'; senderId: string; message: string; attachmentUrls: string[]; createdAt: string }
export interface Dispute {
  id: string; orderSellerId: string; orderId: string; sellerId: string; customerId: string
  raisedBy: string; disputeType: string; reason: string; description?: string; evidenceUrls?: string[]
  status: DisputeStatus; requestedAmount?: number; resolvedAmount?: number; resolutionNote?: string
  resolvedByStaffId?: string; resolvedAt?: string; createdAt: string; updatedAt: string
  order?: Record<string, any>; items?: Array<Record<string, any>>; messages?: DisputeMessage[]
}

const call = async <T>(url: string, method: 'GET' | 'POST' = 'GET', data?: unknown, params?: unknown) => {
  const response = await request({ url, method, data, params }) as AxiosResponse<T>
  return response.data
}

export const buyerDisputes = (status?: string) => call<Dispute[]>(`${API_URL}/buyer/disputes`, 'GET', undefined, { status })
export const buyerDisputeDetail = (id: string) => call<Dispute>(`${API_URL}/buyer/disputes/${id}`)
export const createBuyerDispute = (data: { orderSellerId: string; disputeType: string; reason: string; description?: string; evidenceUrls?: string[]; requestedAmount?: number }) => call<Dispute>(`${API_URL}/buyer/disputes`, 'POST', data)
export const sendBuyerDisputeMessage = (id: string, data: { message: string; attachmentUrls?: string[] }) => call<Dispute>(`${API_URL}/buyer/disputes/${id}/messages`, 'POST', data)

export const sellerDisputes = (status?: string) => call<Dispute[]>(`${API_URL}/seller/disputes`, 'GET', undefined, { status })
export const sellerDisputeDetail = (id: string) => call<Dispute>(`${API_URL}/seller/disputes/${id}`)
export const respondSellerDispute = (id: string, data: { message: string; attachmentUrls?: string[] }) => call<Dispute>(`${API_URL}/seller/disputes/${id}/respond`, 'POST', data)

export const adminDisputes = (params?: { status?: string; sellerId?: string; dateFrom?: string; dateTo?: string }) => call<Dispute[]>(`${API_URL}/admin/disputes`, 'GET', undefined, params)
export const adminDisputeDetail = (id: string) => call<Dispute>(`${API_URL}/admin/disputes/${id}`)
export const takeDisputeReview = (id: string) => call<Dispute>(`${API_URL}/admin/disputes/${id}/take-review`, 'POST')
export const resolveDispute = (id: string, data: { decision: string; resolvedAmount?: number; note: string }) => call<Dispute>(`${API_URL}/admin/disputes/${id}/resolve`, 'POST', data)
export const closeDispute = (id: string) => call<Dispute>(`${API_URL}/admin/disputes/${id}/close`, 'POST')
