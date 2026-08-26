import type { AxiosResponse } from 'axios'
import request from '@/services/request'
import { PREFIX_API_KHACH_HANG_ADMIN, PREFIX_API_SELLER_ADMIN } from '@/constants/url'
import type {
  PaginationParams,
  DefaultResponse,
  ResponseList,
  PaginationResponse
} from '@/types/api.common'

export interface ParamsGetKhachHang extends PaginationParams {
  q?: string | ''
  status?: number | null
}



export type KhachHangResponse = ResponseList & {
  ma: string,
  ten: string,
  sdt: string,
  diaChi?: string,
  tinh?: string,
  huyen?: string,
  xa?: string,
  cccd?: string,
  ngaySinh?: string | number,
  gioiTinh?: boolean,
  email?: string,
  avatar?: string,
  createdDate?: number,
  status: string,
  sellerStatus?: string | null,
  sellerShopName?: string | null,
}

const normalizeKhachHang = (row: any): KhachHangResponse => ({
  ...row,
  ma: row?.ma ?? row?.code ?? '',
  ten: row?.ten ?? row?.name ?? '',
  sdt: row?.sdt ?? row?.phoneNumber ?? '',
  diaChi: row?.diaChi ?? row?.address ?? '',
  tinh: row?.tinh ?? row?.province ?? '',
  huyen: row?.huyen ?? row?.district ?? '',
  xa: row?.xa ?? row?.ward ?? '',
  cccd: row?.cccd ?? row?.identityNumber ?? '',
  ngaySinh: row?.ngaySinh ?? row?.dateOfBirth,
  gioiTinh: row?.gioiTinh ?? row?.gender,
})

const normalizeKhachHangPage = (response: DefaultResponse<PaginationResponse<Array<KhachHangResponse>>>) => ({
  ...response,
  data: response.data
    ? { ...response.data, data: (response.data.data ?? []).map(normalizeKhachHang) }
    : response.data,
})

export type SellerOwnerStatus = {
  sellerId: string,
  shopName: string,
  sellerSlug?: string,
  status: string,
  approvedAt?: string,
  createdAt?: string,
}

export interface ADKhachHangRequest {
  id?: string,
  code: string,
  name: string,
}

export const GetKhachHangs = async (params: ParamsGetKhachHang) => {
  const res = (await request({
    url: `${PREFIX_API_KHACH_HANG_ADMIN}`,
    method: 'GET',
    params: params
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<KhachHangResponse>>>>

  return normalizeKhachHangPage(res.data)
}

export const getKhachHang = async (id: string) => {
  const res = (await request({
    url: `${PREFIX_API_KHACH_HANG_ADMIN}/${id}`,
    method: 'GET'
  })) as AxiosResponse<DefaultResponse<KhachHangResponse>>

  return {
    ...res.data,
    data: res.data?.data ? normalizeKhachHang(res.data.data) : res.data?.data,
  }
}

export const modifyKhachHang = async (data: ADKhachHangRequest) => {
  const res = (await request({
    url: `${PREFIX_API_KHACH_HANG_ADMIN}`,
    method: 'POST',
    data: data
  })) as AxiosResponse<DefaultResponse<KhachHangResponse>>

  return res.data
}

export const modifyStatusKhachHang = async (id: string) => {
  const res = (await request({
    url: `${PREFIX_API_KHACH_HANG_ADMIN}/${id}/change-status`,
    method: 'PUT'
  })) as AxiosResponse<DefaultResponse<KhachHangResponse>>

  return res.data;
}

export const getSellerStatusesByOwnerIds = async (ids: string[]) => {
  const ownerIds = Array.from(new Set(ids.filter(Boolean)))
  if (!ownerIds.length) return {}

  const res = (await request({
    url: `${PREFIX_API_SELLER_ADMIN}/by-owner-ids`,
    method: 'GET',
    params: { ids: ownerIds.join(',') }
  })) as AxiosResponse<DefaultResponse<Record<string, SellerOwnerStatus>>>

  return res.data?.data || {}
}
