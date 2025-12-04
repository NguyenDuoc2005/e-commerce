import type { AxiosResponse } from 'axios'
import request from '@/services/request'
import { PREFIX_API_THUONGHIEU_PERMITALL } from '@/constants/url'
import type { PaginationParams, DefaultResponse, PaginationResponse } from '@/types/api.common'

export interface ThuongHieuResponse {
  idThuongHieu: string
  maThuongHieu: string
  tenThuongHieu: string
}

export interface ParamsGetThuongHieu extends PaginationParams {
}

export const GetAllThuongHieusTrangChu = async (params: ParamsGetThuongHieu) => {
  const res = (await request({
    url: `${PREFIX_API_THUONGHIEU_PERMITALL}/get-all/thuong-hieu-trang-chu`,
    method: 'GET',
    params: params
  })) as AxiosResponse<DefaultResponse<PaginationResponse<ThuongHieuResponse[]>>>
  return res.data
}
