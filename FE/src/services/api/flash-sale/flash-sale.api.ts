import type { AxiosResponse } from 'axios'
import request from '@/services/request'
import { API_URL } from '@/constants/url'

export type FlashSaleStatus = 'CHUA_KICH_HOAT' | 'DANG_KICH_HOAT' | 'HET_HAN_KICH_HOAT'
export type RegistrationStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'WITHDRAWN'

export interface FlashSaleProduct {
  id: string
  campaignId: string
  campaignName: string
  sellerId: string
  productId?: string
  productVariantId: string
  sku?: string
  productName?: string
  variantLabel?: string
  imageUrl?: string
  quantity?: number
  salePrice?: number
  priceBeforeDiscount: number
  flashPrice: number
  discountPercent: number
  registrationStatus: RegistrationStatus
  rejectionReason?: string
  reviewedAt?: number
  createdDate?: number
}

export interface FlashSaleCampaign {
  id: string
  code: string
  name: string
  description?: string
  registrationStartDate: number
  registrationEndDate: number
  startDate: number
  endDate: number
  status: FlashSaleStatus
  registrationOpen: boolean
  pendingCount: number
  approvedCount: number
  sellerRegistrationCount?: number
  products?: FlashSaleProduct[]
}

export interface FlashSaleCampaignInput {
  name: string
  description?: string
  registrationStartDate: number
  registrationEndDate: number
  startDate: number
  endDate: number
}

export interface SellerVariant {
  productId: string
  productVariantId: string
  sellerId: string
  sku: string
  productName: string
  variantLabel: string
  salePrice: number
  quantity: number
  imageUrl?: string
}

const call = async <T>(url: string, method: 'GET' | 'POST' | 'PUT' = 'GET', data?: unknown) => {
  const response = await request({ url, method, data }) as AxiosResponse<T>
  return response.data
}

const adminBase = `${API_URL}/admin/flash-sales`
const sellerBase = `${API_URL}/seller/flash-sales`

export const getAdminFlashSales = () => call<FlashSaleCampaign[]>(adminBase)
export const createFlashSale = (data: FlashSaleCampaignInput) => call<FlashSaleCampaign>(adminBase, 'POST', data)
export const updateFlashSale = (id: string, data: FlashSaleCampaignInput) => call<FlashSaleCampaign>(`${adminBase}/${id}`, 'PUT', data)
export const getFlashSaleRegistrations = (campaignId: string) => call<FlashSaleProduct[]>(`${adminBase}/${campaignId}/registrations`)
export const reviewFlashSaleRegistration = (campaignId: string, registrationId: string, decision: 'APPROVE' | 'REJECT', reason?: string) =>
  call<FlashSaleProduct>(`${adminBase}/${campaignId}/registrations/${registrationId}/review`, 'POST', { decision, reason })

export const getSellerFlashSales = () => call<FlashSaleCampaign[]>(sellerBase)
export const getSellerFlashRegistrations = () => call<FlashSaleProduct[]>(`${sellerBase}/registrations`)
export const getSellerFlashVariants = () => call<SellerVariant[]>(`${sellerBase}/variants`)
export const registerFlashSaleProduct = (campaignId: string, productVariantId: string, flashPrice: number) =>
  call<FlashSaleProduct>(`${sellerBase}/${campaignId}/registrations`, 'POST', { productVariantId, flashPrice })
export const withdrawFlashSaleRegistration = (campaignId: string, registrationId: string) =>
  call<FlashSaleProduct>(`${sellerBase}/${campaignId}/registrations/${registrationId}/withdraw`, 'POST')

export const getPublicFlashSales = () => call<FlashSaleCampaign[]>(`${API_URL}/permitall/flash-sales`)
