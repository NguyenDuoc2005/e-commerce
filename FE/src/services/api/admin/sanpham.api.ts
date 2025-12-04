import type { AxiosResponse } from 'axios'
import request from '@/services/request'
import { PREFIX_API_SAN_PHAM_ADMIN, PREFIX_API_SAN_PHAM_CHI_TIET_ADMIN} from '@/constants/url'
import type {
  PaginationParams,
  DefaultResponse,
  ResponseList,
  PaginationResponse
} from '@/types/api.common'

export interface ParamsGetSanPham extends PaginationParams {
  q?: string | ''
  status?: number | null
  idSP?: string | null
}



export type SanPhamResponse = ResponseList & {
  ma: string,
  ten: string,
  moTa: string,
  status: string,
}

export interface ADSanPhamRequest {
  id?: string,
  code: string,
  name: string,
}

export const GetSanPhams = async (params: ParamsGetSanPham) => {
  const res = (await request({
    url: `${PREFIX_API_SAN_PHAM_ADMIN}`,
    method: 'GET',
    params: params
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<SanPhamResponse>>>>

  return res.data
}

export const GetListThuongHieu = async () => {
  const res = (await request({
    url: `${PREFIX_API_SAN_PHAM_ADMIN}/list-thuong-hieu`,
    method: 'GET',
  })) as AxiosResponse<DefaultResponse<Array<SanPhamResponse>>>

  return res.data
}

export const GetListXuatXu = async () => {
  const res = (await request({
    url: `${PREFIX_API_SAN_PHAM_ADMIN}/list-xuat-xu`,
    method: 'GET',
  })) as AxiosResponse<DefaultResponse<Array<SanPhamResponse>>>

  return res.data
}

export const GetListChatLieu= async () => {
  const res = (await request({
    url: `${PREFIX_API_SAN_PHAM_ADMIN}/list-chat-lieu`,
    method: 'GET',
  })) as AxiosResponse<DefaultResponse<Array<SanPhamResponse>>>

  return res.data
}


export const GetListLoaiDe = async () => {
  const res = (await request({
    url: `${PREFIX_API_SAN_PHAM_ADMIN}/list-loai-de`,
    method: 'GET',
  })) as AxiosResponse<DefaultResponse<Array<SanPhamResponse>>>

  return res.data
}

export const GetListDanhMuc = async () => {
  const res = (await request({
    url: `${PREFIX_API_SAN_PHAM_ADMIN}/list-danh-muc`,
    method: 'GET',
  })) as AxiosResponse<DefaultResponse<Array<SanPhamResponse>>>

  return res.data
}

export const getSanPham = async (id: string) => {
  const res = (await request({
    url: `${PREFIX_API_SAN_PHAM_ADMIN}/${id}`,
    method: 'GET'
  })) as AxiosResponse<DefaultResponse<SanPhamResponse>>

  return res.data
}

export const modifySanPham = async (data: ADSanPhamRequest) => {
  const res = (await request({
    url: `${PREFIX_API_SAN_PHAM_ADMIN}`,
    method: 'POST',
    data: data
  })) as AxiosResponse<DefaultResponse<SanPhamResponse>>

  return res.data
}

export const updateSanPham = async (data: ADSanPhamRequest) => {
  const res = (await request({
    url: `${PREFIX_API_SAN_PHAM_CHI_TIET_ADMIN}/update`,
    method: 'POST',
    data: data
  })) as AxiosResponse<DefaultResponse<SanPhamResponse>>

  return res.data
}

export const modifyStatusSanPham = async (id: string) => {
  const res = (await request({
    url: `${PREFIX_API_SAN_PHAM_ADMIN}/${id}/change-status`,
    method: 'PUT'
  })) as AxiosResponse<DefaultResponse<SanPhamResponse>>

  return res.data;
}