import type { AxiosResponse } from 'axios'
import request from '@/services/request'
import { PREFIX_API_SAN_PHAM_ADMIN } from '@/constants/url'
import type { DefaultResponse, PaginationParams, PaginationResponse } from '@/types/api.common'

export interface ParamsGetSanPham extends PaginationParams {
  status?: number | null
  idSP?: string | null
}

export interface SanPhamResponse {
  id: string
  code: string
  name: string
  description?: string
  status: string
}

export const GetSanPhams = async (params: ParamsGetSanPham) => {
  const res = (await request.get(PREFIX_API_SAN_PHAM_ADMIN, { params })) as AxiosResponse<DefaultResponse<PaginationResponse<SanPhamResponse[]>>>
  return res.data
}
