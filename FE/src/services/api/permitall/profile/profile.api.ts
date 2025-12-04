import type { AxiosResponse } from 'axios'
import request from '@/services/request'
import { PREFIX_API_PROFILE_PERMITALL} from '@/constants/url'
import type {
  PaginationParams,
  DefaultResponse,
  ResponseList,
  PaginationResponse
} from '@/types/api.common'

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

export const getKhachHang = async (id: string) => {
  const res = (await request({
    url: `${PREFIX_API_PROFILE_PERMITALL}/${id}`,
    method: 'GET'
  })) as AxiosResponse<DefaultResponse<KhachHangResponse>>

  return res.data
}

export const getKhachHangs = async (id: string) => {
  const res = (await request({
    url: `${PREFIX_API_PROFILE_PERMITALL}/hd/${id}`,
    method: 'GET'
  })) as AxiosResponse<DefaultResponse<KhachHangResponse>>

  return res.data
}

export const modifyKhachHang = async (data: ADKhachHangRequest) => {
  const res = (await request({
    url: `${PREFIX_API_PROFILE_PERMITALL}`,
    method: 'POST',
    data: data
  })) as AxiosResponse<DefaultResponse<KhachHangResponse>>

  return res.data
}
