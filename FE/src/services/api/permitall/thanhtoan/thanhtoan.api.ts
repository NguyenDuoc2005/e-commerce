import type { AxiosResponse } from "axios";
import type { ParamsPhieuGiamGia, PhieuGiamGiaResponse } from "../../admin/banhang.api";
import request from "@/services/request";
import type { DefaultResponse, PaginationResponse } from "@/types/api.common";

interface ParamsThanhToan {
  hoTen: string;
  soDienThoai: string;
  diaChi: string;
  ghiChu: string;
  maGiamGia: string;
  hinhThucThanhToan: string;
  tongTien: number;
  phiShip: number;
  giamGia: number;
  tongCong: number;
  items: Array<{ id: string; quantity: number }>;
  KhachHang: string;
}

export const ThanhToan = async (data: ParamsThanhToan) => {
  const res = (await request({
    url: `http://localhost:8386/api/orders/create`,
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
    url: `http://localhost:8386/api/orders/create-vnpay`,
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
    url: `http://localhost:8386/api/orders/pgg`,
    method: "POST",
    params: data,
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<PhieuGiamGiaResponse>>>>;

  return res.data;
};

export const getKhachHangDetail = async (id: string) => {
  const res = (await request({
    url: `http://localhost:8386/api/orders/khach-hang/${id}`,
    method: "POST",
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<PhieuGiamGiaResponse>>>>;

  return res.data;
};

export const getListPGG = async (data: ParamsPhieuGiamGia) => {
  const res = (await request({
    url: `http://localhost:8386/api/orders/pgg/list`,
    method: "POST",
    params: data, 
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<PhieuGiamGiaResponse>>>>;

  return res.data;
};
