import type { AxiosResponse } from 'axios'
import request from '@/services/request'
import { PREFIX_API_ADMIN, PREFIX_API_PERMITALL } from '@/constants/url'

export type CategoryStatus = 'ACTIVE' | 'INACTIVE'

export interface AdminCategoryNode {
  id: string
  code: string
  name: string
  slug: string
  parentId?: string | null
  displayOrder?: number
  status?: CategoryStatus
  children?: AdminCategoryNode[] | string | null
}

export interface CategoryMutation {
  name: string
  code?: string
  slug?: string
  parentId?: string | null
  displayOrder: number
}

export interface CategoryAttributeSuggestion {
  definitionId: string
  name: string
  dataType: string
  defaultUnit?: string
  verified: boolean
  required: boolean
  filterable: boolean
  displayOrder: number
}

const base = `${PREFIX_API_ADMIN}/categories`

export const getAdminCategoryTree = async () =>
  ((await request.get(`${base}/tree`) as AxiosResponse<AdminCategoryNode[]>).data ?? [])

export const createAdminCategory = async (body: CategoryMutation) =>
  (await request.post(base, body) as AxiosResponse<AdminCategoryNode>).data

export const updateAdminCategory = async (id: string, body: CategoryMutation) =>
  (await request.put(`${base}/${id}`, body) as AxiosResponse<AdminCategoryNode>).data

export const updateAdminCategoryStatus = async (id: string, status: CategoryStatus) =>
  (await request.put(`${base}/${id}/status`, { status }) as AxiosResponse<AdminCategoryNode>).data

export const getAdminCategorySuggestions = async (categoryId: string) =>
  ((await request.get(`${PREFIX_API_PERMITALL}/categories/${categoryId}/attribute-suggestions`) as AxiosResponse<CategoryAttributeSuggestion[]>).data ?? [])

export const configureAdminCategorySuggestions = async (
  categoryId: string,
  body: Array<Pick<CategoryAttributeSuggestion, 'definitionId' | 'required' | 'filterable' | 'displayOrder'>>
) => request.put(`${base}/${categoryId}/attribute-suggestions`, body)
