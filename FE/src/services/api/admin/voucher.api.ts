import type { AxiosResponse } from 'axios'
import request from '@/services/request'
import { PREFIX_API_VOUCHER_ADMIN } from '@/constants/url'
import type {
  PaginationParams,
  DefaultResponse,
  ResponseList,
  PaginationResponse
} from '@/types/api.common'

export interface ParamsGetSize extends PaginationParams {
  q?: string | ''
  status?: number | null
  startDate?: string | null
  endDate?: string | null
  kieuGiam?: number | null
}



export type SizeResponse = ResponseList & {
  ma?: string,
  ten?: string,
  dieuKien?: number,
  giaGiam?: number,
  phanTramGiam?: number,
  kieuGiam?: boolean,
  loaiGiam?: boolean,
  soLuongPhieu?: number,
  ngayBatDau?: string,
  ngayKetThuc?: string,
  status?: string,
  sdt?: string,
  diaChi?: string,
  email?: string,
  gioiTinh?: boolean,
}

const normalizeVoucher = (row: any): SizeResponse => ({
  ...row,
  ma: row?.ma ?? row?.code ?? '',
  ten: row?.ten ?? row?.name ?? '',
  dieuKien: row?.dieuKien ?? row?.conditionAmount,
  giaGiam: row?.giaGiam ?? row?.maxDiscountAmount,
  phanTramGiam: row?.phanTramGiam ?? row?.discountValue,
  kieuGiam: row?.kieuGiam ?? row?.discountMethod,
  loaiGiam: row?.loaiGiam ?? row?.discountType,
  soLuongPhieu: row?.soLuongPhieu ?? row?.quantity,
  ngayBatDau: row?.ngayBatDau ?? row?.startDate,
  ngayKetThuc: row?.ngayKetThuc ?? row?.endDate,
})

const normalizeVoucherPage = (response: DefaultResponse<PaginationResponse<Array<SizeResponse>>>) => ({
  ...response,
  data: response.data
    ? { ...response.data, data: (response.data.data ?? []).map(normalizeVoucher) }
    : response.data,
})

export interface ADSizeRequest {
  id?: string,
  code: string,
  name: string,
}

export const GetSizes = async (params: ParamsGetSize) => {
  const { kieuGiam, ...rest } = params
  const res = (await request({
    url: `${PREFIX_API_VOUCHER_ADMIN}`,
    method: 'GET',
    params: {
      ...rest,
      discountMethod: kieuGiam,
    }
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<SizeResponse>>>>

  return normalizeVoucherPage(res.data)
}

export const getSize = async (id: string) => {
  const res = (await request({
    url: `${PREFIX_API_VOUCHER_ADMIN}/${id}`,
    method: 'GET'
  })) as AxiosResponse<DefaultResponse<SizeResponse>>

  return {
    ...res.data,
    data: res.data?.data ? normalizeVoucher(res.data.data) : res.data?.data,
  }
}

export const getListKH = async (id: string, search: string = "", page: number = 1, size: number = 10) => {
  const res = await request({
    url: `${PREFIX_API_VOUCHER_ADMIN}/listkh/${id}`,
    method: "GET",
    params: {
      search,
      page: page - 1, // Spring Boot sử dụng page bắt đầu từ 0
      size,
    },
  }) as AxiosResponse<DefaultResponse<PaginationResponse<Array<SizeResponse>>>>

  return res.data;
};
export const modifySize = async (data: ADSizeRequest) => {
  const res = (await request({
    url: `${PREFIX_API_VOUCHER_ADMIN}`,
    method: 'POST',
    data: data
  })) as AxiosResponse<DefaultResponse<SizeResponse>>

  return res.data
}

export const modifyStatusSize = async (id: string) => {
  const res = (await request({
    url: `${PREFIX_API_VOUCHER_ADMIN}/${id}/change-status`,
    method: 'PUT'
  })) as AxiosResponse<DefaultResponse<SizeResponse>>

  return res.data;
}
