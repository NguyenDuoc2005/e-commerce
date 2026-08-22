import type { AxiosResponse } from 'axios'
import request from '@/services/request'
import { PREFIX_API_SANPHAM_PERMITALL } from '@/constants/url'
import type { DefaultResponse, PaginationParams, PaginationResponse, ResponseList } from '@/types/api.common'

export interface MauSacDTO {
  ten: string
  maMau: string
}

export interface KichCoDTO {
  ten: string
  soLuong: number
}

export interface DotGiamGiaInfo {
  tenDotGiamGia: string
  phanTramGiam: number
  giaTruoc: number
  giaSau: number
  ngayBatDau: number
  ngayKetThuc: number
}

export type SanPhamResponse = ResponseList & {
  ma: string,
  ten: string,
  moTa: string,
  status: string,
}


export interface SanPhamMoiResponse {
  id: string
  sellerId?: string
  sellerName?: string
  shopName?: string
  sellerSlug?: string
  sellerLogoUrl?: string
  sellerRating?: number
  soldCount?: number
  tenSanPham: string
  hinhAnhDaiDien: string
  thuongHieu: string
  danhMuc: string
  chatLieu: string
  xuatXu: string | null
  moTa: string
  giaBan: number
  ngayTao: number
  giaSauGiam: number | null
  kichCo: KichCoDTO[]
  mauSac: MauSacDTO[]
  dotGiamGia: DotGiamGiaInfo | null
  dsAnh: string[]
  attributes?: Array<{
    attributeId: string
    name: string
    dataType: 'TEXT' | 'NUMBER' | 'SINGLE_SELECT' | 'MULTI_SELECT'
    options: Array<{ id: string; value: string }>
    textValue?: string
    numberValue?: number
    unit?: string
    selectedOptionIds: string[]
  }>
}

export interface ParamsGetSanPhamMoi extends PaginationParams {}

export interface DynamicCategoryFilter {
  attributeId: string
  name: string
  dataType: 'TEXT' | 'NUMBER' | 'SINGLE_SELECT' | 'MULTI_SELECT'
  options: Array<{ id: string; value: string }>
  min?: number
  max?: number
  displayOrder: number
}

const emptyProductPage = (message = 'Không thể tải danh sách sản phẩm') =>
  ({
    data: {
      data: [],
      totalPages: 0,
      totalElements: 0,
      currentPage: 0
    },
    message,
    status: 'ERROR',
    success: false
  }) as DefaultResponse<PaginationResponse<SanPhamMoiResponse[]>>

export const GetSanPhamMoiTrangChu = async (
  params: ParamsGetSanPhamMoi
) => {
  try {
    const res = (await request({
      url: `${PREFIX_API_SANPHAM_PERMITALL}/get-all/san-pham-moi`,
      method: 'GET',
      params
    })) as AxiosResponse<DefaultResponse<PaginationResponse<SanPhamMoiResponse[]>>>
    return res.data
  } catch (error) {
    console.error('Không thể tải sản phẩm mới:', error)
    return emptyProductPage()
  }
}

export const GetDanhSachSanPhamTrangSanPham = async (
  params: ParamsGetSanPhamMoi
) => {
  try {
    const res = (await request({
      url: `${PREFIX_API_SANPHAM_PERMITALL}/get-all/danh-sach-san-pham`,
      method: 'GET',
      params
    })) as AxiosResponse<DefaultResponse<PaginationResponse<SanPhamMoiResponse[]>>>
    return res.data
  } catch (error) {
    console.error('Không thể tải danh sách sản phẩm:', error)
    return emptyProductPage()
  }
}

export const GetSanPhamGiamGiaTrangChu = async (
  params: ParamsGetSanPhamMoi
) => { 
  try {
    const res = (await request({
      url: `${PREFIX_API_SANPHAM_PERMITALL}/get-all/san-pham-giam-gia`,
      method: 'GET',
      params
    })) as AxiosResponse<DefaultResponse<PaginationResponse<SanPhamMoiResponse[]>>>
    return res.data
  } catch (error) {
    console.error('Không thể tải sản phẩm giảm giá:', error)
    return emptyProductPage()
  }
}


export const GetListThuongHieu = async () => {
  const res = (await request({
    url: `${PREFIX_API_SANPHAM_PERMITALL}/list-thuong-hieu`,
    method: 'GET',
  })) as AxiosResponse<DefaultResponse<Array<SanPhamResponse>>>

  return res.data
}

export const GetListXuatXu = async () => {
  const res = (await request({
    url: `${PREFIX_API_SANPHAM_PERMITALL}/list-xuat-xu`,
    method: 'GET',
  })) as AxiosResponse<DefaultResponse<Array<SanPhamResponse>>>

  return res.data
}

export const GetListChatLieu= async () => {
  const res = (await request({
    url: `${PREFIX_API_SANPHAM_PERMITALL}/list-chat-lieu`,
    method: 'GET',
  })) as AxiosResponse<DefaultResponse<Array<SanPhamResponse>>>

  return res.data
}

export const GetListSize = async () => {
  const res = (await request({
    url: `${PREFIX_API_SANPHAM_PERMITALL}/list-size`,
    method: 'GET',
  })) as AxiosResponse<DefaultResponse<Array<SanPhamResponse>>>

  return res.data
}

export const GetListColor = async () => {
  const res = (await request({
    url: `${PREFIX_API_SANPHAM_PERMITALL}/list-mau`,
    method: 'GET',
  })) as AxiosResponse<DefaultResponse<Array<SanPhamResponse>>>

  return res.data
}

export const GetListLoaiDe = async () => {
  const res = (await request({
    url: `${PREFIX_API_SANPHAM_PERMITALL}/list-loai-de`,
    method: 'GET',
  })) as AxiosResponse<DefaultResponse<Array<SanPhamResponse>>>

  return res.data
}

export const GetListDanhMuc = async () => {
  const res = (await request({
    url: `${PREFIX_API_SANPHAM_PERMITALL}/list-danh-muc`,
    method: 'GET',
  })) as AxiosResponse<DefaultResponse<Array<SanPhamResponse>>>

  return res.data
}

export const GetCategoryFilters = async (categoryId: string) => {
  const res = (await request({
    url: `${PREFIX_API_SANPHAM_PERMITALL}/category-filters`,
    method: 'GET',
    params: { categoryId }
  })) as AxiosResponse<DynamicCategoryFilter[]>

  return res.data || []
}
