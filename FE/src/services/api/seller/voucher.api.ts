import type { AxiosResponse } from 'axios'
import request from '@/services/request'
import { PREFIX_API_SELLER_VOUCHERS } from '@/constants/url'
import type { DefaultResponse, PaginationResponse } from '@/types/api.common'

export interface SellerVoucher {
  id: string
  code: string
  name: string
  discountValue: number
  quantity: number
  conditionAmount: number
  maxDiscountAmount: number
  discountType: boolean
  discountMethod: boolean
  startDate: string
  endDate: string
  status: string
  sellerId: string
}

export interface SellerVoucherRequest {
  id?: string
  name: string
  LoiPhanNay?: number
  quantity: number
  startDate: string
  endDate: string
  conditionAmount: number
  maxDiscountAmount: number
  discountType: boolean
  discountMethod: boolean
}

export const getSellerVouchers = async (params?: { q?: string; status?: number | null; page?: number; size?: number }) => {
  const res = (await request({
    url: PREFIX_API_SELLER_VOUCHERS,
    method: 'GET',
    params
  })) as AxiosResponse<DefaultResponse<PaginationResponse<SellerVoucher[]>>>

  return res.data
}

export const modifySellerVoucher = async (data: SellerVoucherRequest) => {
  const formData = new FormData()
  Object.entries(data).forEach(([key, value]) => {
    if (value !== undefined && value !== null) {
      formData.append(key, String(value))
    }
  })
  const res = (await request({
    url: PREFIX_API_SELLER_VOUCHERS,
    method: 'POST',
    data: formData
  })) as AxiosResponse<DefaultResponse<SellerVoucher>>

  return res.data
}

export const changeSellerVoucherStatus = async (id: string) => {
  const res = (await request({
    url: `${PREFIX_API_SELLER_VOUCHERS}/${id}/change-status`,
    method: 'PUT'
  })) as AxiosResponse<DefaultResponse<SellerVoucher>>

  return res.data
}
