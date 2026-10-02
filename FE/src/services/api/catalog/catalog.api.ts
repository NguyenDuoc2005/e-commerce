import type { AxiosResponse } from 'axios'
import request from '@/services/request'
import { PREFIX_API_PERMITALL } from '@/constants/url'

export type AttributeFilter = { values?: string[]; min?: number; max?: number }
export interface CatalogAttributeOption { id: string; value: string; verified?: boolean; resolvedOptionId?: string }
export interface CatalogAttribute { definitionId: string; name: string; dataType: 'TEXT'|'NUMBER'|'SELECT_ONE'|'SELECT_MULTI'; valueText?: string; valueNumber?: number; unit?: string; selectedOptions?: CatalogAttributeOption[]; optionValues?: string[]; optionIds?: string[] }
export interface CatalogAxis { id: string; name: string; values: Array<{ id: string; value: string }> }
export interface CatalogVariant { id: string; sku: string; combinationKey: string; salePrice: number; quantity: number; imageUrl?: string; isDefault: boolean; status: string; selections: Array<{ axisId: string; axisName: string; valueId: string; value: string }> }
export interface CatalogSummary { id: string; sellerId?: string; name: string; status: string; category?: { id:string; name:string; slug?:string }; minPrice?: number; maxPrice?: number; totalQuantity:number; activeVariantCount:number; thumbnailUrl?:string; ratingAverage?:number; ratingCount?:number; attributePreview?: CatalogAttribute[]; axisPreview?: CatalogAxis[] }
export interface CatalogDetail { id:string; code:string; sellerId?:string; name:string; description?:string; status:string; ratingAverage?:number; ratingCount?:number; category?:{id:string;name:string;slug?:string}; productImages:Array<{id:string;url:string;displayOrder:number}>; attributes:CatalogAttribute[]; variantAxes:CatalogAxis[]; variants:CatalogVariant[] }

export type ProductSearchSort = 'relevance' | 'newest' | 'price_asc' | 'price_desc' | 'rating_desc'

export interface ProductSearchParams {
  q?: string
  categoryId?: string
  sellerId?: string
  color?: string
  sizeValue?: string
  minPrice?: number
  maxPrice?: number
  page: number
  size: number
  sort?: ProductSearchSort
  cursor?: string
}

export interface ProductSearchItem {
  id: string
  sellerId?: string
  name: string
  status: string
  category?: { id: string; name: string; slug?: string }
  minPrice?: number
  maxPrice?: number
  totalQuantity: number
  activeVariantCount: number
  thumbnailUrl?: string
  ratingAverage?: number
  ratingCount?: number
  highlights?: Record<string, string[]>
}

export interface SearchFacetBucket { value: string; count: number }
export interface SearchRangeFacetBucket { key: string; from?: number; to?: number; count: number }
export interface ProductSearchFacets {
  categories: SearchFacetBucket[]
  shops: SearchFacetBucket[]
  priceRanges: SearchRangeFacetBucket[]
  colors: SearchFacetBucket[]
  sizes: SearchFacetBucket[]
}

export interface ProductSearchResponse {
  items: ProductSearchItem[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  nextCursor?: string
  facets: ProductSearchFacets
}

export interface ProductAutocompleteSuggestion { id: string; name: string; categoryName?: string }

const base = PREFIX_API_PERMITALL
export const getCatalogProducts=async(params:Record<string,unknown>)=>(await request.get(`${base}/products`,{params}) as AxiosResponse<{content:CatalogSummary[];totalElements:number;totalPages:number}>).data
export const searchCatalogProducts = async (params: ProductSearchParams, signal?: AbortSignal) => (
  await request.get(`${base}/products/search`, { params, signal }) as AxiosResponse<ProductSearchResponse>
).data
export const autocompleteCatalogProducts = async (q: string, signal?: AbortSignal) => (
  await request.get(`${base}/products/search/autocomplete`, { params: { q, size: 8 }, signal }) as AxiosResponse<{ suggestions: ProductAutocompleteSuggestion[] }>
).data
export const closeCatalogSearchPit = async (cursor: string) => (
  await request.delete(`${base}/products/search/pit`, { params: { cursor } }) as AxiosResponse<{ closed: boolean }>
).data
export const getCatalogProduct=async(id:string)=>(await request.get(`${base}/products/${id}`) as AxiosResponse<CatalogDetail>).data
export const getCategoryTree=async()=>(await request.get(`${base}/categories/tree`) as AxiosResponse<any[]>).data||[]
export const getCategoryFilters=async(categoryId:string)=>(await request.get(`${base}/categories/${categoryId}/attribute-suggestions`) as AxiosResponse<any[]>).data||[]
