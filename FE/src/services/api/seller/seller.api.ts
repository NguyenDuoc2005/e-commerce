import type { AxiosResponse } from 'axios'
import request from '@/services/request'
import {
  PREFIX_API_SELLER,
  PREFIX_API_SELLER_ADMIN,
  PREFIX_API_SELLER_PROFILE,
  PREFIX_API_SHOP_PERMITALL
} from '@/constants/url'
import type { DefaultResponse } from '@/types/api.common'

export type SellerStatus = 'DRAFT' | 'PENDING_APPROVAL' | 'APPROVED' | 'REJECTED' | 'SUSPENDED' | 'CLOSED'

export interface SellerRegistrationRequest {
  shopName: string
  sellerSlug?: string
  description: string
  logoUrl?: string
  coverImageUrl?: string
  pickupAddress: string
  contactPhone: string
  identityType: string
  identityNumber: string
  bankName: string
  bankAccountNo: string
  bankAccountHolder: string
  mainCategoryId?: string
}

export interface SellerResponse {
  id: string
  ownerCustomerId?: string
  shopName: string
  sellerSlug: string
  description: string
  logoUrl?: string
  coverImageUrl?: string
  pickupAddress?: string
  contactPhone?: string
  identityType?: string
  identityNumber?: string
  bankName?: string
  bankAccountNo?: string
  bankAccountHolder?: string
  mainCategoryId?: string
  status: SellerStatus
  rejectionReason?: string
  approvedByStaffId?: string
  approvedAt?: string
  createdAt?: string
  updatedAt?: string
  followerCount?: number
}

export const registerShop = async (data: SellerRegistrationRequest) => {
  const res = (await request({
    url: `${PREFIX_API_SELLER}/register-shop`,
    method: 'POST',
    data
  })) as AxiosResponse<DefaultResponse<SellerResponse>>

  return res.data
}

export const getMyShop = async () => {
  const res = (await request({
    url: `${PREFIX_API_SELLER}/my-shop`,
    method: 'GET'
  })) as AxiosResponse<DefaultResponse<SellerResponse>>

  return res.data
}

export const getSellerProfile = async () => {
  const res = (await request({
    url: PREFIX_API_SELLER_PROFILE,
    method: 'GET'
  })) as AxiosResponse<DefaultResponse<SellerResponse>>

  return res.data
}

export const getPublicShop = async (sellerSlug: string) => {
  const res = (await request({
    url: `${PREFIX_API_SHOP_PERMITALL}/${sellerSlug}`,
    method: 'GET'
  })) as AxiosResponse<DefaultResponse<SellerResponse>>

  return res.data
}

export const getPublicShops = async () => {
  const res = (await request({
    url: PREFIX_API_SHOP_PERMITALL,
    method: 'GET'
  })) as AxiosResponse<DefaultResponse<SellerResponse[]>>

  return res.data
}

export interface ShopFollowState {
  following: boolean
  followerCount: number
}

export const getShopFollowState = async (sellerId: string) => {
  const res = (await request({
    url: `${PREFIX_API_SHOP_PERMITALL}/${sellerId}/follow`,
    method: 'GET'
  })) as AxiosResponse<{ data: ShopFollowState }>
  return res.data
}

export const followShop = async (sellerId: string) => {
  const res = (await request({
    url: `${PREFIX_API_SHOP_PERMITALL}/${sellerId}/follow`,
    method: 'POST'
  })) as AxiosResponse<{ data: ShopFollowState }>
  return res.data
}

export const unfollowShop = async (sellerId: string) => {
  const res = (await request({
    url: `${PREFIX_API_SHOP_PERMITALL}/${sellerId}/follow`,
    method: 'DELETE'
  })) as AxiosResponse<{ data: ShopFollowState }>
  return res.data
}

export const getAdminSellers = async (status?: SellerStatus | '') => {
  const res = (await request({
    url: PREFIX_API_SELLER_ADMIN,
    method: 'GET',
    params: status ? { status } : undefined
  })) as AxiosResponse<DefaultResponse<SellerResponse[]>>

  return res.data
}

export const approveSeller = async (id: string) => {
  const res = (await request({
    url: `${PREFIX_API_SELLER_ADMIN}/${id}/approve`,
    method: 'POST'
  })) as AxiosResponse<DefaultResponse<SellerResponse>>

  return res.data
}

export const rejectSeller = async (id: string, reason: string) => {
  const res = (await request({
    url: `${PREFIX_API_SELLER_ADMIN}/${id}/reject`,
    method: 'POST',
    data: { reason }
  })) as AxiosResponse<DefaultResponse<SellerResponse>>

  return res.data
}

export const suspendSeller = async (id: string, reason: string) => {
  const res = (await request({
    url: `${PREFIX_API_SELLER_ADMIN}/${id}/suspend`,
    method: 'POST',
    data: { reason }
  })) as AxiosResponse<DefaultResponse<SellerResponse>>

  return res.data
}

export const reopenSeller = async (id: string) => {
  const res = (await request({
    url: `${PREFIX_API_SELLER_ADMIN}/${id}/reopen`,
    method: 'POST'
  })) as AxiosResponse<DefaultResponse<SellerResponse>>

  return res.data
}
