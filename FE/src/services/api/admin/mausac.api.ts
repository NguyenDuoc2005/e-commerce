import type { AxiosResponse } from 'axios'
import request from '@/services/request'
import { PREFIX_API_MAU_SAC_ADMIN } from '@/constants/url'
import type {
  PaginationParams,
  DefaultResponse,
  ResponseList,
  PaginationResponse
} from '@/types/api.common'

export interface ParamsGetMember extends PaginationParams {
  q?: string | ''
  status?: number | null
}



export type MauSacResponse = ResponseList & {
  code: string,
  name: string,
}

export interface ADMauSacRequest {
  id?: string,
  code: string,
  name: string,
}

export const getMembers = async (params: ParamsGetMember) => {
  const res = (await request({
    url: `${PREFIX_API_MAU_SAC_ADMIN}`,
    method: 'GET',
    params: params
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<MauSacResponse>>>>

  return res.data
}

export const getMember = async (id: string) => {
  const res = (await request({
    url: `${PREFIX_API_MAU_SAC_ADMIN}/${id}`,
    method: 'GET'
  })) as AxiosResponse<DefaultResponse<MauSacResponse>>

  return res.data
}

export const modifyMember = async (data: ADMauSacRequest) => {
  const res = (await request({
    url: `${PREFIX_API_MAU_SAC_ADMIN}`,
    method: 'POST',
    data: data
  })) as AxiosResponse<DefaultResponse<MauSacResponse>>

  return res.data
}

export const modifyStatusMember = async (id: string) => {
  const res = (await request({
    url: `${PREFIX_API_MAU_SAC_ADMIN}/${id}/change-status`,
    method: 'PUT'
  })) as AxiosResponse<DefaultResponse<MauSacResponse>>

  return res.data;
}