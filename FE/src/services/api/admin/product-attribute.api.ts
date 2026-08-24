import type { AxiosResponse } from 'axios'
import request from '@/services/request'
import { PREFIX_API_PRODUCT_ATTRIBUTES_ADMIN } from '@/constants/url'
export type AttributeDataType = 'TEXT' | 'NUMBER' | 'SELECT_ONE' | 'SELECT_MULTI'
export type AttributeStatus = 'ACTIVE' | 'INACTIVE'
export interface AdminProductAttribute { id:string; code:string; name:string; dataType:AttributeDataType; defaultUnit?:string; verified:boolean; creatorSellerId?:string; status:AttributeStatus; resolvedDefinitionId:string; productCount:number; categoryIds:string[] }
export interface AdminProductAttributeOption { id:string; value:string; status:AttributeStatus; verified:boolean; resolvedOptionId:string }
const data = <T>(r:AxiosResponse<T>) => r.data
export const getAdminProductAttributes = async (params?: {q?:string;status?:AttributeStatus;verified?:boolean;categoryId?:string;creatorSellerId?:string}) => data(await request.get(PREFIX_API_PRODUCT_ATTRIBUTES_ADMIN,{params}) as AxiosResponse<AdminProductAttribute[]>) || []
export const standardizeProductAttribute = async (id:string, body:{name:string;defaultUnit?:string;categoryIds:string[];reason?:string}) => data(await request.put(`${PREFIX_API_PRODUCT_ATTRIBUTES_ADMIN}/${id}/standardize`,body) as AxiosResponse<AdminProductAttribute>)
export const mergeProductAttribute = async (id:string,targetId:string,reason?:string) => data(await request.post(`${PREFIX_API_PRODUCT_ATTRIBUTES_ADMIN}/${id}/merge`,{targetId,reason}) as AxiosResponse<AdminProductAttribute>)
export const hideProductAttribute = async (id:string,reason?:string) => request.put(`${PREFIX_API_PRODUCT_ATTRIBUTES_ADMIN}/${id}/hide`,{reason})
export const verifyProductAttribute = async (id:string) => data(await request.put(`${PREFIX_API_PRODUCT_ATTRIBUTES_ADMIN}/${id}/verify`) as AxiosResponse<AdminProductAttribute>)
export const getProductAttributeOptions = async (id:string) => data(await request.get(`${PREFIX_API_PRODUCT_ATTRIBUTES_ADMIN}/${id}/options`) as AxiosResponse<AdminProductAttributeOption[]>) || []
