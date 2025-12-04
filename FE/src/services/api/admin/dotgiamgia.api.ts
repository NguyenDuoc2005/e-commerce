import type { AxiosResponse } from "axios";
import request from "@/services/request";
import { PREFIX_API_DOT_GIAM_GIA_ADMIN } from "@/constants/url";
import type {
  PaginationParams,
  DefaultResponse,
  ResponseList,
  PaginationResponse,
} from "@/types/api.common";

export interface ParamsGetDotGiamGia extends PaginationParams {
  ma?: string | "";
  ten?: string | "";
  phanTramGiam?: string | "";
  ngayBatDau?: number | null;
  ngayKetThuc?: number | null;
  trangThai?: number | null;
}

export type DotGiamGiaResponse = ResponseList & {
  ma: string;
  ten: string;
  status: string;
};

export interface IdProductDetail {
  id: string;
}

export interface DotGiamGiaRequest {
  id?: string;
  code?: string;
  name: string;
  value: number;
  startDate: number;
  endDate: number;
  idProductDetails: IdProductDetail[];
}

export const GetDotGiamGia = async (params: ParamsGetDotGiamGia) => {
  const res = (await request({
    url: `${PREFIX_API_DOT_GIAM_GIA_ADMIN}`,
    method: "GET",
    params: params,
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<DotGiamGiaResponse>>>>;

  return res.data;
};

export const GetSanPham = async () => {
  const res = (await request({
    url: `${PREFIX_API_DOT_GIAM_GIA_ADMIN}/san-pham`,
    method: "GET",
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<DotGiamGiaResponse>>>>;

  return res.data;
};

export const getColorsFromAPI = async () => {
  const res = (await request({
    url: `${PREFIX_API_DOT_GIAM_GIA_ADMIN}/mau-sac`,
    method: "GET",
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<DotGiamGiaResponse>>>>;

  return res.data;
};

export const getSizesFromAPI = async () => {
  const res = (await request({
    url: `${PREFIX_API_DOT_GIAM_GIA_ADMIN}/size`,
    method: "GET",
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<DotGiamGiaResponse>>>>;

  return res.data;
};

export const DetailDotGiamGia = async (id: string) => {
  const res = (await request({
    url: `${PREFIX_API_DOT_GIAM_GIA_ADMIN}/${id}`,
    method: "GET",
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<DotGiamGiaResponse>>>>;

  return res.data;
};

export const getSanPhamChiTiets = async (id: string) => {
  const res = (await request({
    url: `${PREFIX_API_DOT_GIAM_GIA_ADMIN}/san-pham-chi-tiet/${id}`,
    method: "GET",
  })) as AxiosResponse<DefaultResponse<DotGiamGiaResponse>>;

  return res.data;
};

export const getSanPhamChiTietsByDot = async (id: string) => {
  const res = (await request({
    url: `${PREFIX_API_DOT_GIAM_GIA_ADMIN}/san-pham-chi-tiet-by-dot/${id}`,
    method: "GET",
  })) as AxiosResponse<DefaultResponse<DotGiamGiaResponse>>;

  return res.data;
};

export const addDotGiamGia = async (data: DotGiamGiaRequest) => {
  const res = (await request({
    url: `${PREFIX_API_DOT_GIAM_GIA_ADMIN}`,
    method: "POST",
    data: data,
  })) as AxiosResponse<DefaultResponse<DotGiamGiaResponse>>;

  return res.data;
};

export const updateDotGiamGia = async (data: DotGiamGiaRequest) => {
  const res = (await request({
    url: `${PREFIX_API_DOT_GIAM_GIA_ADMIN}/${data.id}`,
    method: "PUT",
    data: data,
  })) as AxiosResponse<DefaultResponse<DotGiamGiaResponse>>;

  return res.data;
};
