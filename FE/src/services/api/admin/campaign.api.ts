import type { AxiosResponse } from "axios";
import request from "@/services/request";
import { PREFIX_API_CAMPAIGNS_ADMIN } from "@/constants/url";
import type {
  PaginationParams,
  DefaultResponse,
  ResponseList,
  PaginationResponse,
} from "@/types/api.common";

export interface AdminCampaignParams extends PaginationParams {
  ma?: string | "";
  ten?: string | "";
  phanTramGiam?: string | "";
  ngayBatDau?: number | null;
  ngayKetThuc?: number | null;
  trangThai?: number | null;
}

export type AdminCampaignResponse = ResponseList & {
  ma: string;
  ten: string;
  status: string;
};

export interface IdProductDetail {
  id: string;
}

export interface AdminCampaignRequest {
  id?: string;
  code?: string;
  name: string;
  value: number;
  startDate: number;
  endDate: number;
  idProductDetails: IdProductDetail[];
}

export const getAdminCampaigns = async (params: AdminCampaignParams) => {
  const res = (await request({
    url: `${PREFIX_API_CAMPAIGNS_ADMIN}`,
    method: "GET",
    params: params,
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<AdminCampaignResponse>>>>;

  return res.data;
};

export const getCampaignProducts = async () => {
  const res = (await request({
    url: `${PREFIX_API_CAMPAIGNS_ADMIN}/products`,
    method: "GET",
  })) as AxiosResponse<any[]>;

  return res.data;
};

export const getAdminCampaign = async (id: string) => {
  const res = (await request({
    url: `${PREFIX_API_CAMPAIGNS_ADMIN}/${id}`,
    method: "GET",
  })) as AxiosResponse<any>;

  return res.data;
};

export const getCampaignProductVariants = async (id: string) => {
  const res = (await request({
    url: `${PREFIX_API_CAMPAIGNS_ADMIN}/products/${id}/variants`,
    method: "GET",
  })) as AxiosResponse<any[]>;

  return res.data;
};

export const getCampaignVariants = async (id: string) => {
  const res = (await request({
    url: `${PREFIX_API_CAMPAIGNS_ADMIN}/${id}/product-variants`,
    method: "GET",
  })) as AxiosResponse<any[]>;

  return res.data;
};

export const createAdminCampaign = async (data: AdminCampaignRequest) => {
  const res = (await request({
    url: `${PREFIX_API_CAMPAIGNS_ADMIN}`,
    method: "POST",
    data: data,
  })) as AxiosResponse<any>;

  return res.data;
};

export const updateAdminCampaign = async (data: AdminCampaignRequest) => {
  const res = (await request({
    url: `${PREFIX_API_CAMPAIGNS_ADMIN}/${data.id}`,
    method: "PUT",
    data: data,
  })) as AxiosResponse<any>;

  return res.data;
};
