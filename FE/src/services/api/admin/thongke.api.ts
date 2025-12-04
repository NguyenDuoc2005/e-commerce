import type { AxiosResponse } from 'axios'
import request from '@/services/request'
import { PREFIX_API_THONG_KE_ADMIN} from '@/constants/url'
import type {
  PaginationParams,
  DefaultResponse,
  ResponseList,
  PaginationResponse
} from '@/types/api.common'

export interface DoanhThuResponse {
  doanhSoThangNay: number;
  soHoaDonThangNay: number;
  doanhSoHomNay: number;
  soHoaDonHomNay: number;
  hangBanDuocThangNay: number;
}

export interface DonHangHoanThanhResponse {
  date: string;
  soLuongDonHang: number;
}

export interface ParamsGetDonHang {
  ngayBatDau: string;
  ngayKetThuc: string;
}

export const getDoanhThu = async () => {
  const res = (await request({
    url: `${PREFIX_API_THONG_KE_ADMIN}/doanh-thu`,
    method: 'GET',
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<DoanhThuResponse>>>>

  return res.data
}

export const getDonHangHoanThanh = async (params: ParamsGetDonHang) => {
  const res = (await request({
    url: `${PREFIX_API_THONG_KE_ADMIN}/don-hang-hoan-thanh`,
    method: 'GET',
    params: params
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<DonHangHoanThanhResponse>>>>

  return res.data
}

export const getTop3SP = async (params: ParamsGetDonHang) => {
  const res = (await request({
    url: `${PREFIX_API_THONG_KE_ADMIN}/top-san-pham-ban-chay`,
    method: 'GET',
    params: params
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<DonHangHoanThanhResponse>>>>

  return res.data
}

export const getTTHD = async (params: ParamsGetDonHang) => {
  const res = (await request({
    url: `${PREFIX_API_THONG_KE_ADMIN}/ti-le-trang-thai`,
    method: 'GET',
    params: params
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<DonHangHoanThanhResponse>>>>

  return res.data
}
