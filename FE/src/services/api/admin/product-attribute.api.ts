import type { AxiosResponse } from 'axios'
import request from '@/services/request'
import { PREFIX_API_PRODUCT_ATTRIBUTES_ADMIN } from '@/constants/url'
export type AttributeDataType = 'TEXT' | 'NUMBER' | 'SELECT_ONE' | 'SELECT_MULTI'
export type AttributeStatus = 'ACTIVE' | 'INACTIVE'
export interface AdminProductAttribute { id:string; code:string; name:string; dataType:AttributeDataType; defaultUnit?:string; verified:boolean; creatorSellerId?:string; status:AttributeStatus; resolvedDefinitionId:string; productCount:number; categoryIds:string[]; createdDate?:number }
export interface AdminProductAttributeOption { id:string; value:string; status:AttributeStatus; verified:boolean; resolvedOptionId:string }
export interface ProductAttributeModerationAudit { id:string; action:'VERIFY'|'STANDARDIZE'|'MERGE'|'HIDE'|string; actorUserId?:string; sourceDefinitionId:string; sourceDefinitionName:string; targetDefinitionId?:string; targetDefinitionName?:string; reason?:string; affectedProductCount:number; createdDate:number }
const data = <T>(r:AxiosResponse<T>) => r.data
export const getAdminProductAttributes = async (params?: {q?:string;status?:AttributeStatus;verified?:boolean;categoryId?:string;creatorSellerId?:string}) => data(await request.get(PREFIX_API_PRODUCT_ATTRIBUTES_ADMIN,{params}) as AxiosResponse<AdminProductAttribute[]>) || []
export const standardizeProductAttribute = async (id:string, body:{name:string;defaultUnit?:string;categoryIds:string[];reason?:string}) => data(await request.put(`${PREFIX_API_PRODUCT_ATTRIBUTES_ADMIN}/${id}/standardize`,body) as AxiosResponse<AdminProductAttribute>)
export const mergeProductAttribute = async (id:string,targetId:string,reason?:string) => data(await request.post(`${PREFIX_API_PRODUCT_ATTRIBUTES_ADMIN}/${id}/merge`,{targetId,reason}) as AxiosResponse<AdminProductAttribute>)
export const hideProductAttribute = async (id:string,reason?:string) => request.put(`${PREFIX_API_PRODUCT_ATTRIBUTES_ADMIN}/${id}/hide`,{reason})
export const verifyProductAttribute = async (id:string) => data(await request.put(`${PREFIX_API_PRODUCT_ATTRIBUTES_ADMIN}/${id}/verify`) as AxiosResponse<AdminProductAttribute>)
export const getProductAttributeOptions = async (id:string) => data(await request.get(`${PREFIX_API_PRODUCT_ATTRIBUTES_ADMIN}/${id}/options`) as AxiosResponse<AdminProductAttributeOption[]>) || []
export const verifyProductAttributeOption = async (optionId:string) => data(await request.put(`${PREFIX_API_PRODUCT_ATTRIBUTES_ADMIN}/options/${optionId}/verify`) as AxiosResponse<AdminProductAttributeOption>)
export const mergeProductAttributeOption = async (optionId:string,targetId:string,reason?:string) => data(await request.post(`${PREFIX_API_PRODUCT_ATTRIBUTES_ADMIN}/options/${optionId}/merge`,{targetId,reason}) as AxiosResponse<AdminProductAttributeOption>)
export const getProductAttributeModerationAudits = async () => data(await request.get(`${PREFIX_API_PRODUCT_ATTRIBUTES_ADMIN}/moderation-audits`) as AxiosResponse<ProductAttributeModerationAudit[]>) || []
export const reindexProductAttributes = async () => request.post(`${PREFIX_API_PRODUCT_ATTRIBUTES_ADMIN}/reindex`)
