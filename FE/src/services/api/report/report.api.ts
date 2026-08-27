import type { AxiosResponse } from 'axios'
import request from '@/services/request'
import { API_URL } from '@/constants/url'

export type ReportTargetType = 'PRODUCT' | 'SHOP' | 'REVIEW' | 'USER'
export interface ReportItem {
  id: string; reporterId: string; reporterType: 'BUYER' | 'SELLER'; targetType: ReportTargetType; targetId: string
  reasonCode: string; description?: string; evidenceUrls?: string[]; status: 'PENDING' | 'REVIEWING' | 'ACTION_TAKEN' | 'DISMISSED'
  actionTaken?: string; resolutionNote?: string; reviewedByStaffId?: string; reviewedAt?: string; createdAt: string
  target?: Record<string, unknown> & {
    unavailable?: boolean; lookupError?: string; name?: string; shopName?: string; comment?: string; email?: string
  }
}
export interface CreateReportPayload { targetType: ReportTargetType; targetId: string; reasonCode: string; description?: string; evidenceUrls?: string[] }

const call = async <T>(url: string, method: 'GET' | 'POST' = 'GET', data?: unknown, params?: unknown) => {
  const response = await request({ url, method, data, params }) as AxiosResponse<T>
  return response.data
}
export const createBuyerReport = (data: CreateReportPayload) => call<ReportItem>(`${API_URL}/buyer/reports`, 'POST', data)
export const createSellerReport = (data: CreateReportPayload) => call<ReportItem>(`${API_URL}/seller/reports`, 'POST', data)
export const adminReports = (params?: { status?: string; targetType?: string; dateFrom?: string; dateTo?: string }) => call<ReportItem[]>(`${API_URL}/admin/reports`, 'GET', undefined, params)
export const adminReportDetail = (id: string) => call<ReportItem>(`${API_URL}/admin/reports/${id}`)
export const reviewReport = (id: string) => call<ReportItem>(`${API_URL}/admin/reports/${id}/review`, 'POST')
export const resolveReport = (id: string, data: { actionTaken: string; note: string }) => call<ReportItem>(`${API_URL}/admin/reports/${id}/resolve`, 'POST', data)
