import type { AxiosResponse } from 'axios'
import request from '@/services/request'
import { PREFIX_API_THUONG_HIEU_ADMIN } from '@/constants/url'
import type {
  PaginationParams,
  DefaultResponse,
  ResponseList,
  PaginationResponse
} from '@/types/api.common'

export interface ParamsGetThuongHieu extends PaginationParams {
  q?: string | ''
  status?: number | null
}

export type ThuongHieuResponse = ResponseList & {
  maSize: string,
  tenSize: string,
  status: string,
}

export interface ADThuongHieuRequest {
  id?: string,
  code: string,
  name: string,
}

export const GetThuongHieus = async (params: ParamsGetThuongHieu) => {
  const res = (await request({
    url: `${PREFIX_API_THUONG_HIEU_ADMIN}`,
    method: 'GET',
    params: params
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<ThuongHieuResponse>>>>

  return res.data
}

export const getThuongHieu = async (id: string) => {
  const res = (await request({
    url: `${PREFIX_API_THUONG_HIEU_ADMIN}/${id}`,
    method: 'GET'
  })) as AxiosResponse<DefaultResponse<ThuongHieuResponse>>

  return res.data
}

export const modifyThuongHieu = async (data: ADThuongHieuRequest) => {
  const res = (await request({
    url: `${PREFIX_API_THUONG_HIEU_ADMIN}`,
    method: 'POST',
    data: data
  })) as AxiosResponse<DefaultResponse<ThuongHieuResponse>>

  return res.data
}

export const modifyStatusThuongHieu = async (id: string) => {
  const res = (await request({
    url: `${PREFIX_API_THUONG_HIEU_ADMIN}/${id}/change-status`,
    method: 'PUT'
  })) as AxiosResponse<DefaultResponse<ThuongHieuResponse>>

  return res.data;
}