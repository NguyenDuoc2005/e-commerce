import type { AxiosResponse } from 'axios'
import request from '@/services/request'
import { PREFIX_API_NHAN_VIEN_ADMIN } from '@/constants/url'
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



export type NhanVienResponse = ResponseList & {
  ma: string,
  ten: string,
  sdt: string,
  diaChi: string,
  email: string,
  status: string,
}

export interface ADNhanVienRequest  {
  id?: string,
  code: string,
  name: string,

  
}

export const getMembers = async (params: ParamsGetMember) => {
  const res = (await request({
    url: `${PREFIX_API_NHAN_VIEN_ADMIN}`,
    method: 'GET',
    params: params
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<NhanVienResponse>>>>

  return res.data
}

export const getMember = async (id: string) => {
  const res = (await request({
    url: `${PREFIX_API_NHAN_VIEN_ADMIN}/${id}`,
    method: 'GET'
  })) as AxiosResponse<DefaultResponse<NhanVienResponse>>

  return res.data
}

export const modifyMember = async (data: ADNhanVienRequest) => {
  console.log('data', data)
  const res = (await request({
    url: `${PREFIX_API_NHAN_VIEN_ADMIN}`,
    method: 'POST',
    data: data
  })) as AxiosResponse<DefaultResponse<ADNhanVienRequest>>

  return res.data
}

export const modifyStatusMember = async (id: string) => {
  const res = (await request({
    url: `${PREFIX_API_NHAN_VIEN_ADMIN}/${id}/change-status`,
    method: 'PUT'
  })) as AxiosResponse<DefaultResponse<NhanVienResponse>>

  return res.data;
}

export const changeStatusMember = async (id: string) => {
  const res = (await request({
    url: `${PREFIX_API_NHAN_VIEN_ADMIN}/${id}/change-role`,
    method: 'PUT'
  })) as AxiosResponse<DefaultResponse<NhanVienResponse>>

  return res.data;
}

export const getTinh = async () => {
  const res = (await request({
    url: `https://provinces.open-api.vn/api/p`,
    method: 'GET',
  })) as AxiosResponse<DefaultResponse<Array<NhanVienResponse>>>

  return res.data;
}

export const getHuyen = async (provinceCode: string) => {
  const res = (await request({
    url: `https://provinces.open-api.vn/api/p/${provinceCode}?depth=2`,
    method: 'GET',
  })) as AxiosResponse<DefaultResponse<Array<NhanVienResponse>>>

  return res.data;
}

export const getXa = async (districtCode: string) => {
  const res = (await request({
    url: `https://provinces.open-api.vn/api/d/${districtCode}?depth=2`,
    method: 'GET',
  })) as AxiosResponse<DefaultResponse<Array<NhanVienResponse>>>

  return res.data;
}