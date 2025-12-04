import type { AxiosResponse } from 'axios'
import request from '@/services/request'
import { PREFIX_API_SAN_PHAM_CHI_TIET_ADMIN} from '@/constants/url'
import type {
  PaginationParams,
  DefaultResponse,
  ResponseList,
  PaginationResponse
} from '@/types/api.common'
import type { List } from 'lodash'

export interface ParamsGetSanPham extends PaginationParams {
  q?: string | ''
  idSP?: string | undefined
  status?: number | null
}



export type SanPhamResponse = ResponseList & {
  id: string
  ma: string,
  ten: string,
  moTa: string,
  mau: string,
  idSP: string,
  idCL: string,
  idLG: string,
  idLD: string,
  idXX: string,
  idMau: string,
  idSize: string,
  status: string,
}

export type ThuocTinhResponse = ResponseList & {
  
  ma: string,
  ten: string,
  moTa: string,
  mau: string,
  status: string,
}

export interface ADSanPhamRequest {
  id?: string,
  code: string,
  name: string,
  soLuong: number,
  gia: string,
  idSP: string,
  idCL: string,
  idLG: string,
  idLD: string,
  idXX: string,
  idMau: string,
  idSize: string,
  status: string,
}

export const GetSanPhams = async (params: ParamsGetSanPham) => {
  const res = (await request({
    url: `${PREFIX_API_SAN_PHAM_CHI_TIET_ADMIN}`,
    method: 'GET',
    params: params
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<SanPhamResponse>>>>

  return res.data
}

export const getDetailSanPham = async (id: string) => {
  const res = (await request({
    url: `${PREFIX_API_SAN_PHAM_CHI_TIET_ADMIN}/detail/${id}`,
    method: 'GET'
  })) as AxiosResponse<DefaultResponse<SanPhamResponse>>

  return res.data
}

export const getSanPham = async (id: string) => {
  const res = (await request({
    url: `${PREFIX_API_SAN_PHAM_CHI_TIET_ADMIN}/${id}`,
    method: 'GET'
  })) as AxiosResponse<DefaultResponse<SanPhamResponse>>

  return res.data
}

export const modifySanPham = async (data: ADSanPhamRequest) => {
  const res = (await request({
    url: `${PREFIX_API_SAN_PHAM_CHI_TIET_ADMIN}`,
    method: 'POST',
    data: data,
  })) as AxiosResponse<DefaultResponse<SanPhamResponse>>;

  return res.data;
};

export const modifyStatusSanPham = async (id: string) => {
  const res = (await request({
    url: `${PREFIX_API_SAN_PHAM_CHI_TIET_ADMIN}/${id}/change-status`,
    method: 'PUT'
  })) as AxiosResponse<DefaultResponse<SanPhamResponse>>

  return res.data;
}

// export const GetListXuatXu = async () => {
//   const res = (await request({
//     url: `${PREFIX_API_SAN_PHAM_CHI_TIET_ADMIN}/list-xuat-xu`,
//     method: 'GET',
//   })) as AxiosResponse<DefaultResponse<Array<SanPhamResponse>>>

//   return res.data
// }


// export const GetListChatLieu = async () => {
//   const res = (await request({
//     url: `${PREFIX_API_SAN_PHAM_CHI_TIET_ADMIN}/list-chat-lieu`,
//     method: 'GET',
//   })) as AxiosResponse<DefaultResponse<Array<SanPhamResponse>>>

//   return res.data
// }


// export const GetListLoaiDe= async () => {
//   const res = (await request({
//     url: `${PREFIX_API_SAN_PHAM_CHI_TIET_ADMIN}/list-loai-de`,
//     method: 'GET',
//   })) as AxiosResponse<DefaultResponse<Array<SanPhamResponse>>>

//   return res.data
// }


// export const GetListLoaiGiay = async () => {
//   const res = (await request({
//     url: `${PREFIX_API_SAN_PHAM_CHI_TIET_ADMIN}/list-loai-giay`,
//     method: 'GET',
//   })) as AxiosResponse<DefaultResponse<Array<SanPhamResponse>>>

//   return res.data
// }

export const GetListSize = async () => {
  const res = (await request({
    url: `${PREFIX_API_SAN_PHAM_CHI_TIET_ADMIN}/list-size`,
    method: 'GET',
  })) as AxiosResponse<DefaultResponse<Array<SanPhamResponse>>>

  return res.data
}

export const GetListColor = async () => {
  const res = (await request({
    url: `${PREFIX_API_SAN_PHAM_CHI_TIET_ADMIN}/list-mau`,
    method: 'GET',
  })) as AxiosResponse<DefaultResponse<Array<SanPhamResponse>>>

  return res.data
}


export const GetListSP = async () => {
  const res = (await request({
    url: `${PREFIX_API_SAN_PHAM_CHI_TIET_ADMIN}/list-sp`,
    method: 'GET',
  })) as AxiosResponse<DefaultResponse<Array<SanPhamResponse>>>

  return res.data
}
