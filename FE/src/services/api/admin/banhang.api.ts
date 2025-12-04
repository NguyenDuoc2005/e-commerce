import type { AxiosResponse } from "axios";
import request from "@/services/request";
import { PREFIX_API_BAN_HANG_ADMIN } from "@/constants/url";
import type {
  PaginationParams,
  DefaultResponse,
  ResponseList,
  PaginationResponse,
} from "@/types/api.common";

export interface ParamsGetSanPham extends PaginationParams {
  q?: string | "";
  idSP?: string | undefined;
  status?: number | null;
}

export interface ParamsPhieuGiamGia extends PaginationParams {
  idKH?: string | undefined;
  idHD?: number | null;
}

export interface ParamsGetHoaDon extends PaginationParams {
  q?: string | "";
  status?: number | null;
}

export interface ParamsXoaSP {
  idHD?: string;
  idSP?: string;
}

export interface ParamsPTTT {
  idHD?: string;
  pttt: string;
}

export interface ParamsThanhCong {
  idHD?: string;
  tongTien: string;
}

// NEW: Type for delivery information
export type ThongTinGiaoHangResponse = ResponseList & {
  id: string;
  hoTenNguoiNhan: string;
  sdtNguoiNhan: string;
  diaChiGiaoHang: string;
  ghiChu?: string;
  // Add other relevant fields for delivery information
  // Example:
  // maHoaDon?: string;
  // trangThaiGiaoHang?: string;
};

// NEW: Request payload for adding/updating delivery information
export interface ThemThongTinGiaoHangRequest {
  idHD: string; // Assuming delivery info is tied to a specific invoice
  hoTenNguoiNhan: string;
  sdtNguoiNhan: string;
  diaChiGiaoHang: string;
  ghiChu?: string;
}

export type KhachHangResponse = ResponseList & {
  id: string;
  ten: string;
  sdt: string;
};

export type thanhToanResponse = ResponseList & {
  id: string;
  ten: string;
  sdt: string;
};

export type PhuongThucThanhToanResponse = ResponseList & {
  id: string;
  soTien: number;
  phuongThucThanhToan: string;
};

export type tongTienResponse = ResponseList & {
  tongTien: number;
};

export interface ADThemSanPhamRequest {
  id?: string;
  code: string;
  name: string;
}

export type PhieuGiamGiaResponse = ResponseList & {
  id: string;
  ma: string;
  giaTriGiam: number; // Giá trị giảm (tiền hoặc %)
  laPhanTram: boolean; // true nếu là %, false nếu là tiền
  giaTriGiamThucTe: number; // Giá trị giảm thực tế (đã tính dựa trên tongTien)
};

export interface PhieuGiamGiaRequest {
  idHD: number;
  idKH: string;
}

export type BanHangResponse = ResponseList & {
  ma: string;
  ten: string;
  status: string;
};

export type themKHResponse = {
  idHD: string;
  idKH: string;
};

export type XoaSPResponse = {
  idHD: string;
  idSP: string;
};

export type SanPhamResponse = ResponseList & {
  id: string;
  ma: string;
  ten: string;
  moTa: string;
  mau: string;
  idSP: string;
  idCL: string;
  idLG: string;
  idLD: string;
  idXX: string;
  idMau: string;
  idSize: string;
  status: string;
};

export const GetHoaDons = async (params: ParamsGetHoaDon) => {
  const res = (await request({
    url: `${PREFIX_API_BAN_HANG_ADMIN}/list-hoa-don`,
    method: "GET",
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<BanHangResponse>>>>;

  return res.data;
};

export const getCreateHoaDon = async (maHoaDon: ADThemSanPhamRequest) => {
  const res = (await request({
    url: `${PREFIX_API_BAN_HANG_ADMIN}/create-hoa-don`,
    method: "POST",
    data: maHoaDon,
  })) as AxiosResponse<DefaultResponse<BanHangResponse>>;
  return res.data;
};

export const huyHoaDon = async (maHoaDon: ADThemSanPhamRequest) => {
  const res = (await request({
    url: `${PREFIX_API_BAN_HANG_ADMIN}/huy`,
    method: "POST",
    data: maHoaDon,
  })) as AxiosResponse<DefaultResponse<BanHangResponse>>;
  return res.data;
};

export const themSanPham = async (data: ADThemSanPhamRequest) => {
  const res = (await request({
    url: `${PREFIX_API_BAN_HANG_ADMIN}/them-san-pham`,
    method: "POST",
    data: data,
  })) as AxiosResponse<DefaultResponse<BanHangResponse>>;

  return res.data;
};

export const xoaSP = async (data: ParamsXoaSP) => {
  const res = (await request({
    url: `${PREFIX_API_BAN_HANG_ADMIN}/xoa-san-pham `,
    method: "POST",
    data: data,
  })) as AxiosResponse<DefaultResponse<XoaSPResponse>>;
  return res.data; // Added return statement
};

export const GetGioHang = async (id: string) => {
  const res = (await request({
    url: `${PREFIX_API_BAN_HANG_ADMIN}/list-gio-hang/${id}`,
    method: "GET",
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<BanHangResponse>>>>;

  return res.data;
};

export const themSL = async (data: ParamsXoaSP) => {
  const res = (await request({
    url: `${PREFIX_API_BAN_HANG_ADMIN}/them-so-luong `,
    method: "POST",
    data: data,
  })) as AxiosResponse<DefaultResponse<XoaSPResponse>>;

  return res.data;
};

export const xoaSL = async (data: ParamsXoaSP) => {
  const res = (await request({
    url: `${PREFIX_API_BAN_HANG_ADMIN}/xoa-so-luong `,
    method: "POST",
    data: data,
  })) as AxiosResponse<DefaultResponse<XoaSPResponse>>;
  return res.data; // Added return statement
};

export const GeOneKhachHang = async (id: string) => {
  const res = (await request({
    url: `${PREFIX_API_BAN_HANG_ADMIN}/khach-hang/${id}`,
    method: "GET",
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<KhachHangResponse>>>>;

  return res.data;
};

export const getThanhToan = async (id: string) => {
  const res = (await request({
    url: `${PREFIX_API_BAN_HANG_ADMIN}/thanh-toan/${id}`,
    method: "GET",
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<thanhToanResponse>>>>;

  return res.data;
};

export const getPhuongThucThanhToan = async (id: string) => {
  const res = (await request({
    url: `${PREFIX_API_BAN_HANG_ADMIN}/phuong-thuc-thanh-toan/${id}`,
    method: "GET",
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<thanhToanResponse>>>>;

  return res.data;
};

export const themPTTT = async (data: ParamsPTTT) => {
  const res = (await request({
    url: `${PREFIX_API_BAN_HANG_ADMIN}/cap-nhat-phuong-thuc-thanh-toan`,
    method: "POST",
    data: data,
  })) as AxiosResponse<DefaultResponse<XoaSPResponse>>;
  return res.data; // Added return statement
};

export const thanhToanThanhCong = async (data: ParamsThanhCong) => {
  const res = (await request({
    url: `${PREFIX_API_BAN_HANG_ADMIN}/thanh-toan-thanh-cong`,
    method: "POST",
    data: data,
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<KhachHangResponse>>>>;

  return res.data;
};

export const themMoiKhachHang = async (data: themKHResponse) => {
  const res = (await request({
    url: `${PREFIX_API_BAN_HANG_ADMIN}/them-moi-khach-hang`,
    method: "POST",
    data: data,
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<KhachHangResponse>>>>;

  return res.data;
};

export const themKhachHang = async (data: themKHResponse) => {
  const res = (await request({
    url: `${PREFIX_API_BAN_HANG_ADMIN}/them-khach-hang`,
    method: "POST",
    data: data,
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<KhachHangResponse>>>>;

  return res.data;
};

export const GetSanPhams = async (params: ParamsGetSanPham) => {
  const res = (await request({
    url: `${PREFIX_API_BAN_HANG_ADMIN}/list-san-pham`,
    method: "GET",
    params: params,
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<SanPhamResponse>>>>;

  return res.data;
};

export const GetKhachHang = async (data: ParamsXoaSP) => {
  const res = (await request({
    url: `${PREFIX_API_BAN_HANG_ADMIN}/list-khach-hang`,
    method: "GET",
    params: data,
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<KhachHangResponse>>>>;

  return res.data;
};

export const getMaGiamGia = async (data: ParamsPhieuGiamGia) => {
  const res = (await request({
    url: `${PREFIX_API_BAN_HANG_ADMIN}/danh-sach-phieu-giam-gia`,
    method: "GET",
    params: data,
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<PhieuGiamGiaResponse>>>>;

  return res.data;
};

export const getMaGiamGiaKoDu = async (data: ParamsPhieuGiamGia) => {
  const res = (await request({
    url: `${PREFIX_API_BAN_HANG_ADMIN}/danh-sach-phieu-giam-gia-ko_du`,
    method: "GET",
    params: data,
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<PhieuGiamGiaResponse>>>>;

  return res.data;
};

export const suaGiaoHang = async (id: string) => {
  const res = (await request({
    url: `${PREFIX_API_BAN_HANG_ADMIN}/giao-hang/${id}`,
    method: "POST",
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<PhieuGiamGiaResponse>>>>;

  return res.data;
};
// NEW: API call to add/update delivery information
