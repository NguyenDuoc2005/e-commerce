import type { AxiosResponse } from 'axios'
import request from '@/services/request'
import { PREFIX_API_SELLER_PRODUCTS } from '@/constants/url'

export type AttributeDataType = 'TEXT' | 'NUMBER' | 'SELECT_ONE' | 'SELECT_MULTI'
export type EntityStatus = 'ACTIVE' | 'INACTIVE'

export interface CategoryNode {
  id: string
  code: string
  name: string
  slug: string
  children: CategoryNode[]
}

export interface AttributeOption {
  id: string
  value: string
  verified: boolean
  resolvedOptionId: string
}

export interface AttributeSuggestion {
  definitionId: string
  name: string
  dataType: AttributeDataType
  defaultUnit?: string
  verified: boolean
  required: boolean
  filterable: boolean
  displayOrder: number
  options: AttributeOption[]
}

export interface ProductAttributeInput {
  definitionId?: string
  name?: string
  dataType: AttributeDataType
  valueText?: string
  valueNumber?: number
  unit?: string
  selectedOptionIds: string[]
  selectedOptionValues: string[]
  displayOrder: number
}

export interface AxisValueInput {
  id?: string
  clientKey: string
  value: string
  displayOrder: number
}

export interface AxisInput {
  id?: string
  clientKey: string
  name: string
  nameSuggestionId?: string
  displayOrder: number
  values: AxisValueInput[]
}

export interface VariantInput {
  id?: string
  sku: string
  salePrice: number
  quantity: number
  imageUrl?: string
  defaultVariant: boolean
  status: EntityStatus
  selectionValueKeys: string[]
}

export interface ProductAggregatePayload {
  categoryId: string
  code?: string
  name: string
  description?: string
  productImages: Array<{ url: string; displayOrder: number; status: EntityStatus }>
  attributes: ProductAttributeInput[]
  variantAxes: AxisInput[]
  variants: VariantInput[]
}

export interface ProductSelection {
  axisId: string
  axisName: string
  valueId: string
  value: string
}

export interface ProductVariant extends Omit<VariantInput, 'selectionValueKeys' | 'defaultVariant'> {
  combinationKey: string
  isDefault: boolean
  selections: ProductSelection[]
}

export interface ProductAggregateDetail {
  id: string
  code: string
  sellerId: string
  name: string
  description?: string
  status: EntityStatus
  category: Omit<CategoryNode, 'children'>
  productImages: Array<{ id: string; url: string; displayOrder: number; status: EntityStatus }>
  attributes: Array<ProductAttributeInput & { selectedOptions: AttributeOption[] }>
  variantAxes: Array<AxisInput & { values: AxisValueInput[] }>
  variants: ProductVariant[]
}

export interface ProductSummary {
  id: string
  sellerId: string
  name: string
  status: EntityStatus
  category: Omit<CategoryNode, 'children'>
  minPrice?: number
  maxPrice?: number
  totalQuantity: number
  activeVariantCount: number
  thumbnailUrl?: string
  ratingAverage?: number
  ratingCount: number
  attributePreview: Array<{ name: string; valueText?: string; valueNumber?: number; unit?: string }>
  axisPreview: Array<{ name: string; values: Array<{ value: string }> }>
}

export interface SpringPage<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export const getSellerProducts = async (params: { page: number; size: number; q?: string }) => {
  const response = (await request.get(PREFIX_API_SELLER_PRODUCTS, { params })) as AxiosResponse<SpringPage<ProductSummary>>
  return response.data
}

export const getSellerProduct = async (id: string) => {
  const response = (await request.get(`${PREFIX_API_SELLER_PRODUCTS}/${id}`)) as AxiosResponse<ProductAggregateDetail>
  return response.data
}

export const createSellerProduct = async (payload: ProductAggregatePayload) => {
  const response = (await request.post(PREFIX_API_SELLER_PRODUCTS, payload)) as AxiosResponse<ProductAggregateDetail>
  return response.data
}

export const updateSellerProduct = async (id: string, payload: ProductAggregatePayload) => {
  const response = (await request.put(`${PREFIX_API_SELLER_PRODUCTS}/${id}`, payload)) as AxiosResponse<ProductAggregateDetail>
  return response.data
}

export const changeSellerProductStatus = async (id: string, status: EntityStatus) => {
  const response = (await request.put(`${PREFIX_API_SELLER_PRODUCTS}/${id}/status`, { status })) as AxiosResponse<ProductAggregateDetail>
  return response.data
}

export const getSellerCategoryTree = async () => {
  const response = (await request.get(`${PREFIX_API_SELLER_PRODUCTS}/categories/tree`)) as AxiosResponse<CategoryNode[]>
  return response.data
}

export const getSellerCategoryAttributes = async (categoryId: string, q = '') => {
  const response = (await request.get(`${PREFIX_API_SELLER_PRODUCTS}/categories/${categoryId}/attribute-suggestions`, {
    params: { q }
  })) as AxiosResponse<AttributeSuggestion[]>
  return response.data
}

export const getAxisNameSuggestions = async (q = '') => {
  const response = (await request.get(`${PREFIX_API_SELLER_PRODUCTS}/variant-axis-name-suggestions`, {
    params: { q }
  })) as AxiosResponse<Array<{ id: string; name: string; verified: boolean; resolvedSuggestionId: string }>>
  return response.data
}
