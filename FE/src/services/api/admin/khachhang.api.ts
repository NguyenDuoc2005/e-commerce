import type { AxiosResponse } from 'axios'
import request from '@/services/request'
import { PREFIX_API_KHACH_HANG_ADMIN} from '@/constants/url'
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
  status: string,
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

  return res.data
}

export const getKhachHang = async (id: string) => {
  const res = (await request({
    url: `${PREFIX_API_KHACH_HANG_ADMIN}/${id}`,
    method: 'GET'
  })) as AxiosResponse<DefaultResponse<KhachHangResponse>>

  return res.data
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