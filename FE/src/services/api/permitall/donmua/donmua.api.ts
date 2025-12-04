import type { AxiosResponse } from 'axios'
import request from '@/services/request'
import { PREFIX_API_PROFILE_ORDER_HISTORY} from '@/constants/url'
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
}

export const getDonMua = async (params: DonMuaRequest) => {
  const res = (await request({
    url: `${PREFIX_API_PROFILE_ORDER_HISTORY}`,
    method: 'GET',
    params: params
  })) as AxiosResponse<DefaultResponse<DonMuaResponse>>

  return res.data
}

export const GetSanPhams = async (params: ParamsGetSanPham) => {
  const res = (await request({
    url: `${PREFIX_API_PROFILE_ORDER_HISTORY}/spct`,
    method: 'GET',
    params: params
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<SanPhamResponse>>>>

  return res.data
}

export const GetLSTT = async (id: string) => {
  const res = (await request({
    url: `${PREFIX_API_PROFILE_ORDER_HISTORY}/lich_su_thanh_toan/${id}`,
    method: 'GET',
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<DonMuaResponse>>>>

  return res.data
}

export const getDonMuaByCode = async (code: string) => {
  const res = (await request({
    url: `${PREFIX_API_PROFILE_ORDER_HISTORY}/all/${code}`,
    method: 'GET',
  })) as AxiosResponse<DefaultResponse<DonMuaResponse>>

  return res.data
}

export const themSanPhamOnl = async (data: ParamsGetSanPham) => {
  const res = (await request({
    url: `${PREFIX_API_PROFILE_ORDER_HISTORY}/them-san-pham`,
    method: "POST",
    data: data,
  })) as AxiosResponse<DefaultResponse<DonMuaResponse>>;

  return res.data;
};


export const getHoaDonChiTiets = async (params: ParamsGetHoaDonCT) => {
  const res = (await request({
    url: `${PREFIX_API_PROFILE_ORDER_HISTORY}/all`,
    method: 'GET',
    params: params
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<DonMuaResponse>>>>

  return res.data
}

export const getSuaThongTin = async (params: ParamsGetHoaDonCT) => {
  const res = (await request({
    url: `${PREFIX_API_PROFILE_ORDER_HISTORY}/sua-thong-tin`,
    method: 'POST',
    params: params
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<DonMuaResponse>>>>

  return res.data
}

export const changeStatus = async (params: ParamsGetHoaDonCT) => {
  const res = (await request({
    url: `${PREFIX_API_PROFILE_ORDER_HISTORY}/change-status`,
    method: 'PUT',
    params: params
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<DonMuaResponse>>>>

  return res.data
}


export const GetLSTTHD = async (id: string) => {
  const res = (await request({
    url: `${PREFIX_API_PROFILE_ORDER_HISTORY}/${id}`,
    method: 'GET',
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<DonMuaResponse>>>>

  return res.data
}