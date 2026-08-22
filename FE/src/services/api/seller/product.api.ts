import type { AxiosResponse } from 'axios'
import request from '@/services/request'
import { PREFIX_API_SELLER_PRODUCTS, PREFIX_API_SELLER_PRODUCT_VARIANTS } from '@/constants/url'
import type { DefaultResponse, PaginationResponse } from '@/types/api.common'

export interface SellerProduct {
  id: string
  code: string
  name: string
  description?: string
  tenBrand?: string
  idBrand?: string
  tenXuatXu?: string
  idXuatXu?: string
  tenSoleType?: string
  idSoleType?: string
  tenCategory?: string
  idCategory?: string
  tenMaterial?: string
  idMaterial?: string
  tongSP?: number
  status: string
  attributes?: DynamicAttribute[]
}

export interface SellerProductVariant {
  id: string
  name: string
  quantity: number
  salePrice: number
  kichThuoc?: string
  tenMau?: string
  imageUrl?: string
  status: string
}

export interface CatalogOption {
  id: string
  name: string
}

export interface ProductPayload {
  id?: string
  name: string
  description?: string
  idBrand?: string
  idXuatXu?: string
  idSoleType?: string
  idCategory?: string
  idMaterial?: string
  attributes?: DynamicAttributePayload[]
}

export type AttributeDataType = 'TEXT' | 'NUMBER' | 'SINGLE_SELECT' | 'MULTI_SELECT'

export interface DynamicAttributeOption {
  id: string
  value: string
}

export interface DynamicAttribute {
  attributeId: string
  name: string
  dataType: AttributeDataType
  normalizationStatus: 'PENDING' | 'STANDARDIZED' | 'MERGED' | 'HIDDEN'
  ownedBySeller: boolean
  defaultSuggestion: boolean
  filterable: boolean
  required: boolean
  options: DynamicAttributeOption[]
  textValue?: string
  numberValue?: number
  unit?: string
  selectedOptionIds: string[]
  displayOrder: number
}

export interface DynamicAttributePayload {
  attributeId?: string
  name: string
  dataType: AttributeDataType
  textValue?: string
  numberValue?: number
  unit?: string
  optionValues?: string[]
  selectedOptionIds?: string[]
  selectedOptionValues?: string[]
  displayOrder: number
}

export interface VariantPayload {
  id?: string
  idSP: string
  idMau?: string
  idSize?: string
  quantity: number
  salePrice: number
  imageUrl?: File
}

export const getSellerProducts = async (params: { page: number; size: number; q?: string }) => {
  const res = (await request.get(PREFIX_API_SELLER_PRODUCTS, { params })) as AxiosResponse<DefaultResponse<PaginationResponse<SellerProduct[]>>>
  return res.data
}

export const getSellerProduct = async (id: string) => {
  const res = (await request.get(`${PREFIX_API_SELLER_PRODUCTS}/${id}`)) as AxiosResponse<DefaultResponse<SellerProduct>>
  return res.data
}

export const saveSellerProduct = async (payload: ProductPayload) => {
  const formData = new FormData()
  Object.entries(payload).forEach(([key, value]) => {
    if (value === undefined || value === null) return
    formData.append(key, key === 'attributes' ? JSON.stringify(value) : String(value))
  })
  const res = (await request.post(PREFIX_API_SELLER_PRODUCTS, formData)) as AxiosResponse<DefaultResponse<SellerProduct>>
  return res.data
}

export const changeSellerProductStatus = async (id: string) => {
  const res = (await request.put(`${PREFIX_API_SELLER_PRODUCTS}/${id}/change-status`)) as AxiosResponse<DefaultResponse<null>>
  return res.data
}

export const getSellerProductOptions = async () => {
  const response = await request.get(`${PREFIX_API_SELLER_PRODUCTS}/list-danh-muc`)
  return (response.data?.data || []) as CatalogOption[]
}

export const getSellerCategoryAttributes = async (categoryId: string, q?: string) => {
  const res = (await request.get(`${PREFIX_API_SELLER_PRODUCTS}/categories/${categoryId}/attributes`, {
    params: q ? { q } : undefined
  })) as AxiosResponse<DynamicAttribute[]>
  return res.data || []
}

export const getSellerVariants = async (params: { page: number; size: number; q?: string; idSP?: string }) => {
  const res = (await request.get(PREFIX_API_SELLER_PRODUCT_VARIANTS, { params })) as AxiosResponse<DefaultResponse<PaginationResponse<SellerProductVariant[]>>>
  return res.data
}

export const getSellerVariant = async (id: string) => {
  const res = (await request.get(`${PREFIX_API_SELLER_PRODUCT_VARIANTS}/${id}`)) as AxiosResponse<DefaultResponse<any>>
  return res.data
}

export const getSellerVariantOptions = async () => {
  const responses = await Promise.all([
    request.get(`${PREFIX_API_SELLER_PRODUCT_VARIANTS}/list-sp`),
    request.get(`${PREFIX_API_SELLER_PRODUCT_VARIANTS}/list-mau`),
    request.get(`${PREFIX_API_SELLER_PRODUCT_VARIANTS}/list-size`)
  ])
  return responses.map((response) => (response.data?.data || []) as CatalogOption[])
}

export const saveSellerVariant = async (payload: VariantPayload) => {
  const formData = new FormData()
  Object.entries(payload).forEach(([key, value]) => {
    if (value !== undefined && value !== null) formData.append(key, value as string | Blob)
  })
  const url = payload.id ? `${PREFIX_API_SELLER_PRODUCT_VARIANTS}/update` : PREFIX_API_SELLER_PRODUCT_VARIANTS
  const res = (await request.post(url, formData)) as AxiosResponse<DefaultResponse<SellerProductVariant>>
  return res.data
}

export const changeSellerVariantStatus = async (id: string) => {
  const res = (await request.put(`${PREFIX_API_SELLER_PRODUCT_VARIANTS}/${id}/change-status`)) as AxiosResponse<DefaultResponse<null>>
  return res.data
}
