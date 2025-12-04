import type { AxiosResponse } from 'axios'
import request from '@/services/request'
import { PREFIX_API_XUAT_XU_ADMIN} from '@/constants/url'
import type {
  PaginationParams,
  DefaultResponse,
  ResponseList,
  PaginationResponse
} from '@/types/api.common'

export interface ParamsGetXuatXu extends PaginationParams {
  q?: string | ''
  status?: number | null
}



export type XuatXuResponse = ResponseList & {
  maSize: string,
  tenSize: string,
  status: string,
}

export interface ADXuatXuRequest {
  id?: string,
  code: string,
  name: string,
}

export const GetXuatXus = async (params: ParamsGetXuatXu) => {
  const res = (await request({
    url: `${PREFIX_API_XUAT_XU_ADMIN}`,
    method: 'GET',
    params: params
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<XuatXuResponse>>>>

  return res.data
}

export const getXuatXu = async (id: string) => {
  const res = (await request({
    url: `${PREFIX_API_XUAT_XU_ADMIN}/${id}`,
    method: 'GET'
  })) as AxiosResponse<DefaultResponse<XuatXuResponse>>

  return res.data
}

export const modifyXuatXu = async (data: ADXuatXuRequest) => {
  const res = (await request({
    url: `${PREFIX_API_XUAT_XU_ADMIN}`,
    method: 'POST',
    data: data
  })) as AxiosResponse<DefaultResponse<XuatXuResponse>>

  return res.data
}

export const modifyStatusXuatXu = async (id: string) => {
  const res = (await request({
    url: `${PREFIX_API_XUAT_XU_ADMIN}/${id}/change-status`,
    method: 'PUT'
  })) as AxiosResponse<DefaultResponse<XuatXuResponse>>

  return res.data;
}