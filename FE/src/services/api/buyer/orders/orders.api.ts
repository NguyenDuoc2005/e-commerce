import type { AxiosResponse } from 'axios'
import request from '@/services/request'
import { PREFIX_API_BUYER_ORDERS } from '@/constants/url'
import type {
  PaginationParams,
  DefaultResponse,
  ResponseList,
  PaginationResponse
} from '@/types/api.common'

export type DonMuaResponse = ResponseList & {
  q: string,
  ten: string,
  status: string,
}

export type SanPhamResponse = ResponseList & {
  id: string
  ma: string,
  ten: string,
  moTa: string,
  mau: string,
  idSP: string,
  idCL: string,
  idLG: string,
  idLD: string,
  idXX: string,
  idMau: string,
  idSize: string,
  status: string,
}

export interface ParamsGetHoaDonCT extends PaginationParams {
  maHoaDon?: string | ''
}

export interface ParamsGetSanPham extends PaginationParams {
  q?: string | ''
  idSP?: string | undefined
  status?: number | null
}

export interface DonMuaRequest {
  q?: string
  status?: string
  search?: string
}

export interface GroupedOrderItem {
  id: string
  product_variant_id: string
  productId?: string
  productName: string
  imageUrl?: string
  color?: string
  size?: string
  quantity: number
  sale_price: number
}

export interface GroupedSubOrder {
  id: string
  seller_id: string
  shop_name: string
  seller_slug: string
  total_after_discount: number
  order_status: number
  created_date: number
  items: GroupedOrderItem[]
}

export interface GroupedOrder {
  id: string
  code: string
  total_after_discount: number
  order_status: number
  created_date: number
  subOrders: GroupedSubOrder[]
}

export const getGroupedOrders = async () => {
  const res = (await request({
    url: `${PREFIX_API_BUYER_ORDERS}/grouped`,
    method: 'GET'
  })) as AxiosResponse<{ data: GroupedOrder[] }>
  return res.data
}

export const getDonMua = async (params: DonMuaRequest) => {
  const res = (await request({
    url: PREFIX_API_BUYER_ORDERS,
    method: 'GET',
    params: params
  })) as AxiosResponse<DefaultResponse<DonMuaResponse>>

  return res.data
}

export const GetSanPhams = async (params: ParamsGetSanPham) => {
  const res = (await request({
    url: `${PREFIX_API_BUYER_ORDERS}/spct`,
    method: 'GET',
    params: params
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<SanPhamResponse>>>>

  return res.data
}

export const GetLSTT = async (id: string) => {
  const res = (await request({
    url: `${PREFIX_API_BUYER_ORDERS}/payment_history/${id}`,
    method: 'GET',
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<DonMuaResponse>>>>

  return res.data
}

export const getDonMuaByCode = async (code: string) => {
  const res = (await request({
    url: `${PREFIX_API_BUYER_ORDERS}/all/${code}`,
    method: 'GET',
  })) as AxiosResponse<DefaultResponse<DonMuaResponse>>

  return res.data
}

export const themSanPhamOnl = async (data: ParamsGetSanPham) => {
  const res = (await request({
    url: `${PREFIX_API_BUYER_ORDERS}/them-san-pham`,
    method: "POST",
    data: data,
  })) as AxiosResponse<DefaultResponse<DonMuaResponse>>;

  return res.data;
};


export const getHoaDonChiTiets = async (params: ParamsGetHoaDonCT) => {
  const res = (await request({
    url: `${PREFIX_API_BUYER_ORDERS}/all`,
    method: 'GET',
    params: params
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<DonMuaResponse>>>>

  return res.data
}

export const getSuaThongTin = async (params: ParamsGetHoaDonCT) => {
  const res = (await request({
    url: `${PREFIX_API_BUYER_ORDERS}/sua-thong-tin`,
    method: 'POST',
    params: params
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<DonMuaResponse>>>>

  return res.data
}

export const changeStatus = async (params: ParamsGetHoaDonCT) => {
  const res = (await request({
    url: `${PREFIX_API_BUYER_ORDERS}/change-status`,
    method: 'PUT',
    params: params
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<DonMuaResponse>>>>

  return res.data
}


export const GetLSTTHD = async (id: string) => {
  const res = (await request({
    url: `${PREFIX_API_BUYER_ORDERS}/${id}`,
    method: 'GET',
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<DonMuaResponse>>>>

  return res.data
}
