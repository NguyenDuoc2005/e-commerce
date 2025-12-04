import type { AxiosResponse } from 'axios'
import request from '@/services/request'
import { PREFIX_API_SANPHAM_PERMITALL, PREFIX_API_SANPHAMCHITIET_PERMITALL } from '@/constants/url'
import type { DefaultResponse, PaginationParams, PaginationResponse } from '@/types/api.common'


export interface ThuongHieuDTO {
  id: string
  tenThuongHieu: string
  maThuongHieu: string
}

export interface XuatXuDTO {
  id: string
  tenNuoc: string
  maNuoc: string
}

export interface ChatLieuDTO {
  id: string
  tenChatLieu: string
  maChatLieu: string
}

export interface DanhMucDTO {
  id: string
  tenDanhMuc: string
  maDanhMuc: string
}

export interface LoaiDeDTO {
  id: string
  tenLoaiDe: string
  maLoaiDe: string
}

export interface MauSacDTO {
  id: string
  tenMauSac: string
  maMau: string
}

export interface KichCoDTO {
  id: string
  tenKichCo: string
  maKichCo: string
}

export interface DotGiamGiaDTO {
  tenDotGiamGia: string
  phanTramGiam: number
  giaTruoc: number | null
  giaSau: number | null
  ngayBatDau: number
  ngayKetThuc: number
}

export interface ChiTietSanPham {
  id: string
  mauSac: MauSacDTO
  kichCo: KichCoDTO
  giaBan: number
  soLuong: number
  hinhAnh: string
  dotGiamGia: DotGiamGiaDTO | null
}

export interface SanPhamChiTietResponse {
  tenSanPham: string
  moTa: string
  thuongHieu: ThuongHieuDTO
  xuatXu: XuatXuDTO | null
  chatLieu: ChatLieuDTO
  danhMuc: DanhMucDTO
  loaiDe: LoaiDeDTO
  chiTietSanPham: ChiTietSanPham[]
}
export interface ParamsGetSanPhamChiTiet {
  idSanPham: string
}

export const GetSanPhamChiTietById = async (
  params: ParamsGetSanPhamChiTiet
) => {
  const res = (await request({
    url: `${PREFIX_API_SANPHAMCHITIET_PERMITALL}/get-all/san-pham-chi-tiet`,
    method: 'GET',
    params, 
  })) as AxiosResponse<DefaultResponse<SanPhamChiTietResponse>>
  return res.data
}
