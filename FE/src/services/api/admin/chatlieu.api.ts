import type { AxiosResponse } from 'axios'
import request from '@/services/request'
import { PREFIX_API_CHAT_LIEU_ADMIN } from '@/constants/url'
import type {
  PaginationParams,
  DefaultResponse,
  ResponseList,
  PaginationResponse
} from '@/types/api.common'

export interface ParamsGetSize extends PaginationParams {
  q?: string | ''
  status?: number | null
}



export type SizeResponse = ResponseList & {
  maSize: string,
  tenSize: string,
  status: string,
}

export interface ADSizeRequest {
  id?: string,
  code: string,
  name: string,
}

export const GetSizes = async (params: ParamsGetSize) => {
  const res = (await request({
    url: `${PREFIX_API_CHAT_LIEU_ADMIN}`,
    method: 'GET',
    params: params
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<SizeResponse>>>>

  return res.data
}

export const getSize = async (id: string) => {
  const res = (await request({
    url: `${PREFIX_API_CHAT_LIEU_ADMIN}/${id}`,
    method: 'GET'
  })) as AxiosResponse<DefaultResponse<SizeResponse>>

  return res.data
}

export const modifySize = async (data: ADSizeRequest) => {
  const res = (await request({
    url: `${PREFIX_API_CHAT_LIEU_ADMIN}`,
    method: 'POST',
    data: data
  })) as AxiosResponse<DefaultResponse<SizeResponse>>

  return res.data
}

export const modifyStatusSize = async (id: string) => {
  const res = (await request({
    url: `${PREFIX_API_CHAT_LIEU_ADMIN}/${id}/change-status`,
    method: 'PUT'
  })) as AxiosResponse<DefaultResponse<SizeResponse>>

  return res.data;
}