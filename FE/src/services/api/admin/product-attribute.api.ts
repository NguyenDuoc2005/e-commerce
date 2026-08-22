import type { AxiosResponse } from 'axios'
import request from '@/services/request'
import { PREFIX_API_PRODUCT_ATTRIBUTES_ADMIN } from '@/constants/url'

export type AttributeDataType = 'TEXT' | 'NUMBER' | 'SINGLE_SELECT' | 'MULTI_SELECT'
export type AttributeStatus = 'PENDING' | 'STANDARDIZED' | 'MERGED' | 'HIDDEN'

export interface AdminProductAttribute {
  id: string
  code: string
  name: string
  dataType: AttributeDataType
  normalizationStatus: AttributeStatus
  creatorSellerId?: string
  mergedIntoAttributeId?: string
  usageCount: number
  options: Array<{ id: string; value: string }>
  categories: Array<{ id: string; name: string; filterable: boolean }>
}

export const getAdminProductAttributes = async (params?: { q?: string; status?: AttributeStatus }) => {
  const response = await request.get(PREFIX_API_PRODUCT_ATTRIBUTES_ADMIN, { params }) as AxiosResponse<AdminProductAttribute[]>
  return response.data || []
}

export const standardizeProductAttribute = async (
  id: string,
  data: { name: string; categoryIds: string[]; filterable: boolean; reason?: string }
) => {
  const response = await request.put(`${PREFIX_API_PRODUCT_ATTRIBUTES_ADMIN}/${id}/standardize`, data) as AxiosResponse<AdminProductAttribute>
  return response.data
}

export const mergeProductAttribute = async (id: string, targetAttributeId: string, reason?: string) => {
  const response = await request.post(`${PREFIX_API_PRODUCT_ATTRIBUTES_ADMIN}/${id}/merge`, { targetAttributeId, reason }) as AxiosResponse<AdminProductAttribute>
  return response.data
}

export const hideProductAttribute = async (id: string, reason?: string) => {
  await request.put(`${PREFIX_API_PRODUCT_ATTRIBUTES_ADMIN}/${id}/hide`, { reason })
}
