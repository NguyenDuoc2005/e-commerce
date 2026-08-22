import type { AxiosResponse } from 'axios'
import request from '@/services/request'
import { PREFIX_API_BANNER_ADMIN, PREFIX_API_BANNER_PERMITALL } from '@/constants/url'

export interface PlatformBanner {
  id: string
  title: string
  imageUrl: string
  targetUrl?: string
  position: string
  active: boolean
  sortOrder: number
  startAt?: string
  endAt?: string
  createdAt?: string
  updatedAt?: string
}

export type PlatformBannerPayload = Omit<PlatformBanner, 'id' | 'createdAt' | 'updatedAt'>

interface BannerListResponse { data: PlatformBanner[] }
interface BannerResponse { data: PlatformBanner }

export const getPublicBanners = async (position = 'HOME_HERO') => {
  const res = (await request({
    url: PREFIX_API_BANNER_PERMITALL,
    method: 'GET',
    params: { position }
  })) as AxiosResponse<BannerListResponse>
  return res.data
}

export const getAdminBanners = async () => {
  const res = (await request({ url: PREFIX_API_BANNER_ADMIN, method: 'GET' })) as AxiosResponse<BannerListResponse>
  return res.data
}

export const createBanner = async (data: PlatformBannerPayload) => {
  const res = (await request({ url: PREFIX_API_BANNER_ADMIN, method: 'POST', data })) as AxiosResponse<BannerResponse>
  return res.data
}

export const updateBanner = async (id: string, data: PlatformBannerPayload) => {
  const res = (await request({ url: `${PREFIX_API_BANNER_ADMIN}/${id}`, method: 'PUT', data })) as AxiosResponse<BannerResponse>
  return res.data
}

export const deleteBanner = async (id: string) => {
  const res = await request({ url: `${PREFIX_API_BANNER_ADMIN}/${id}`, method: 'DELETE' })
  return res.data
}
