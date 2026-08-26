import type { AxiosResponse } from "axios";
import request from "@/services/request";
import { PREFIX_API_BUYER_CART } from "@/constants/url";
import type { DefaultResponse, PaginationResponse } from "@/types/api.common";

export interface requestCartDetail {
  idCustomer?: string;
  idKhachHang?: string;

  idSPCT: string;

  price: string;

  quantity: string;
}

export interface requestCart {
  idUser: string;
}

export const createCartDetail = async (data: requestCartDetail) => {
  const res = (await request({
    url: PREFIX_API_BUYER_CART,
    method: "POST",
    data,
  })) as AxiosResponse<DefaultResponse<null>>;
  return res.data;
};

export const getAllCart = async (data: requestCart) => {
  const res = (await request({
    url: PREFIX_API_BUYER_CART,
    method: "GET",
    params: data,
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<null>>>>;
  return res.data;
};

export const deleteCartDetail = async (id: string) => {
  const res = (await request({
    url: `${PREFIX_API_BUYER_CART}/${id}`,
    method: "PUT",
  })) as AxiosResponse<DefaultResponse<PaginationResponse<Array<null>>>>;
  return res.data;
};
