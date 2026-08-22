import type { AxiosResponse } from "axios";
import request from "@/services/request";
import type { DefaultResponse, PaginationParams, PaginationResponse, ResponseList } from "@/types/api.common";
import { API_URL_1 } from "@/constants/url";

export interface ParamsPhieuGiamGia extends PaginationParams {
  idKH?: string;
  idHD?: string | number | null;
  tongTien?: number | string | null;
}

export type PhieuGiamGiaResponse = ResponseList & {
  id: string;
  ma: string;
  giaTriGiam: number;
  laPhanTram: boolean;
  giaTriGiamThucTe: number;
};

interface ParamsThanhToan {
  hoTen: string;
  soDienThoai: string;
  address?: string;
  diaChi: string;
  ghiChu: string;
  maGiamGia: string;
  hinhThucThanhToan: string;
  tongTien: number;
  phiShip: number;
  giamGia: number;
  tongCong: number;
  items?: Array<{ id: string; quantity: number }>;
  product?: Array<{ id: string; quantity: number }>;
  sanPham?: Array<{ id: string; quantity: number }>;
  Customer?: string;
  KhachHang: string;
}

export const ThanhToan = async (data: ParamsThanhToan) => {
  const res = (await request({
    url: `${API_URL_1}/orders/create`,
    method: "POST",
    data: data, // Sử dụng 'data' thay vì 'params' để gửi body JSON
    headers: {
      "Content-Type": "application/json", // Đảm bảo header đúng
    },
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<PhieuGiamGiaResponse>>>>;

  return res.data;
};

export const ThanhToanVnPay = async (data: ParamsThanhToan) => {
  const res = (await request({
    url: `${API_URL_1}/orders/create`,
    method: "POST",
    data: data, // Sử dụng 'data' thay vì 'params' để gửi body JSON
    headers: {
      "Content-Type": "application/json", // Đảm bảo header đúng
    },
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<PhieuGiamGiaResponse>>>>;

  return res.data;
};

export const getPGG = async (data: ParamsPhieuGiamGia) => {
  const res = (await request({
    url: `${API_URL_1}/orders/pgg`,
    method: "POST",
    params: data,
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<PhieuGiamGiaResponse>>>>;

  return res.data;
};

export const getKhachHangDetail = async (id: string) => {
  const res = (await request({
    url: `${API_URL_1}/orders/khach-hang/${id}`,
    method: "POST",
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<PhieuGiamGiaResponse>>>>;

  return res.data;
};

export const getListPGG = async (data: ParamsPhieuGiamGia) => {
  const res = (await request({
    url: `${API_URL_1}/orders/pgg/list`,
    method: "POST",
    params: data, 
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<PhieuGiamGiaResponse>>>>;

  return res.data;
};
