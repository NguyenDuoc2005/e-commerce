import type { AxiosResponse } from 'axios'
import request from '@/services/request'
import { PREFIX_API_HOA_DON_ADMIN, PREFIX_API_KHACH_HANG_ADMIN } from '@/constants/url'
import type {
  PaginationParams,
  DefaultResponse,
  ResponseList,
  PaginationResponse
} from '@/types/api.common'

export interface ParamsGetHoaDon extends PaginationParams {
  q?: string | ''
  status?: number | null
  startDate?: number | null
  endDate?: number | null
}

export interface ParamsGetHoaDonCT extends PaginationParams {
  maHoaDon?: string | ''
}

export interface ParamsChangeStatus {
  maHoaDon?: string | ''
  status?: number | null
}

export interface ParamsThanhToan {
  hoaDonId?: string | ''
  soTienKhachDua?: number | null
  soTienTraLai?: number | null
  ghiChu?: string | ''
  loaiGiaoDich?: string | ''
  nhanVienId?: string | ''
  status?: number | null
}

export type HoaDonResponse = ResponseList & {
  ma: string,
  ten: string,
  status: string,
}

export const GetHoaDons = async (params: ParamsGetHoaDon) => {
  const res = (await request({
    url: `${PREFIX_API_HOA_DON_ADMIN}`,
    method: 'GET',
    params: params
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<HoaDonResponse>>>>

  return res.data
}

export const GetLSTTHD = async (id: string) => {
  const res = (await request({
    url: `${PREFIX_API_HOA_DON_ADMIN}/${id}`,
    method: 'GET',
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<HoaDonResponse>>>>

  return res.data
}

export const GetLSTT = async (id: string) => {
  const res = (await request({
    url: `${PREFIX_API_HOA_DON_ADMIN}/lich_su_thanh_toan/${id}`,
    method: 'GET',
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<HoaDonResponse>>>>

  return res.data
}

export const thanhToan = async (params: ParamsChangeStatus) => {
  const res = (await request({
    url: `${PREFIX_API_HOA_DON_ADMIN}/thanh_toan`,
    method: 'POST',
    params: params
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<HoaDonResponse>>>>

  return res.data
}

export const updateOrderStatusInDatabase = async (params: ParamsChangeStatus) => {
  const res = (await request({
    url: `${PREFIX_API_HOA_DON_ADMIN}/change-status`,
    method: 'PUT',
    params: params
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<HoaDonResponse>>>>

  return res.data
}

export const getHoaDonChiTiets = async (params: ParamsGetHoaDonCT) => {
  const res = (await request({
    url: `${PREFIX_API_HOA_DON_ADMIN}/all`,
    method: 'GET',
    params: params
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<HoaDonResponse>>>>

  return res.data
}

// 5. Cập nhật API functions để trả về response đúng định dạng
// Thay thế 2 function inPDFOFFLINE và inPDFONLINE trong file hoadon.api.ts:

// 3. Cải tiến API functions với better error handling
export const inPDFOFFLINE = async (maHoaDon: string): Promise<Blob> => {
  try {
    const res = (await request({
      url: `${PREFIX_API_HOA_DON_ADMIN}/pdf/${maHoaDon}`,
      method: 'GET',
      responseType: 'blob', // Quan trọng để nhận về file PDF
      headers: {
        Accept: 'application/pdf',
      },
    })) as AxiosResponse<Blob>;

    if (!res.data.type.includes('pdf')) {
      throw new Error('Response không phải là file PDF');
    }

    return res.data;
  } catch (error) {
    console.error('Lỗi inPDFOFFLINE:', error);
    throw error;
  }
}

export const inPDFONLINE = async (maHoaDon: string): Promise<Blob> => {
  try {
    const res = (await request({
      url: `${PREFIX_API_HOA_DON_ADMIN}/delivery/${maHoaDon}/pdf`,
      method: 'GET',
      responseType: 'blob',
      headers: {
        Accept: 'application/pdf',
      },
    })) as AxiosResponse<Blob>;

    if (!res.data.type.includes('pdf')) {
      throw new Error('Response không phải là file PDF');
    }

    return res.data;
  } catch (error) {
    console.error('Lỗi inPDFONLINE:', error);
    throw error;
  }
}