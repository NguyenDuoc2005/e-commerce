import type { AxiosResponse } from "axios";
import request from "@/services/request";
import type { DefaultResponse } from "@/types/api.common";
import { API_URL_1 } from "@/constants/url";

export interface ParamsPhieuGiamGia {
  idKH?: string;
  idHD?: string | number | null;
  tongTien?: number | string | null;
}

export interface PhieuGiamGiaResponse {
  id: string;
  code?: string;
  name?: string;
  discount_value?: number;
  condition_amount?: number;
  max_discount_amount?: number;
  discount_method?: boolean;
  end_date?: string;
  actualDiscountValue?: number;
  discountValue?: number;
  maxDiscountAmount?: number;
  discountMethod?: boolean;
}

export interface CheckoutResponse {
  id?: string;
  code?: string;
  orderId?: string;
  paymentUrl?: string;
  [key: string]: unknown;
}

export interface CheckoutCustomerResponse {
  id: string;
  code?: string;
  name?: string;
  ten?: string;
  email?: string;
  phoneNumber?: string;
  sdt?: string;
  address?: string;
  diaChi?: string;
  tinh?: number | string;
  huyen?: number | string;
  xa?: string;
}

interface ParamsThanhToan {
  hoTen: string;
  soDienThoai: string;
  email?: string;
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

export const ThanhToan = async (data: ParamsThanhToan, idempotencyKey?: string) => {
  const res = (await request({
    url: `${API_URL_1}/orders/create`,
    method: "POST",
    data: data, // Sử dụng 'data' thay vì 'params' để gửi body JSON
    headers: {
      "Content-Type": "application/json", // Đảm bảo header đúng
      ...(idempotencyKey ? { "Idempotency-Key": idempotencyKey } : {}),
    },
  })) as AxiosResponse<CheckoutResponse>;

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
  })) as AxiosResponse<CheckoutResponse>;

  return res.data;
};

export const getPGG = async (data: ParamsPhieuGiamGia) => {
  const res = (await request({
    url: `${API_URL_1}/orders/pgg`,
    method: "POST",
    params: data,
  })) as AxiosResponse<DefaultResponse<PhieuGiamGiaResponse>>;

  return res.data;
};

export const getKhachHangDetail = async (id: string) => {
  const res = (await request({
    url: `${API_URL_1}/orders/khach-hang/${id}`,
    method: "POST",
  })) as AxiosResponse<DefaultResponse<CheckoutCustomerResponse>>;

  return res.data;
};

export const getListPGG = async (data: ParamsPhieuGiamGia) => {
  const res = (await request({
    url: `${API_URL_1}/orders/pgg/list`,
    method: "POST",
    params: data, 
  })) as AxiosResponse<DefaultResponse<Array<PhieuGiamGiaResponse>>>;

  return res.data;
};
