import type { AxiosResponse } from 'axios'
import request from '@/services/request'
import { API_URL, PREFIX_API_BUYER_REVIEWS, PREFIX_API_PERMITALL } from '@/constants/url'

export interface Review {
  id: string
  customerId: string
  sellerId: string
  productId: string
  productDetailId: string
  orderSellerId: string
  productRating: number
  shopRating: number
  comment?: string
  imageUrls: string[]
  sellerReply?: string
  sellerRepliedAt?: string
  status: string
  createdAt: string
}

export interface CreateReviewRequest {
  orderSellerId: string
  productDetailId: string
  productRating: number
  shopRating: number
  comment?: string
  imageUrls?: string[]
}

export const createReview = async (data: CreateReviewRequest) => {
  const response = (await request({ url: PREFIX_API_BUYER_REVIEWS, method: 'POST', data })) as AxiosResponse<{ data: Review }>
  return response.data
}

export const getPublicReviews = async (params: { productId?: string; sellerId?: string }) => {
  const response = (await request({ url: `${PREFIX_API_PERMITALL}/reviews`, method: 'GET', params })) as AxiosResponse<{ data: Review[] }>
  return response.data
}

export const getMyReviews = async () => {
  const response = (await request({ url: `${PREFIX_API_BUYER_REVIEWS}/mine`, method: 'GET' })) as AxiosResponse<{ data: Review[] }>
  return response.data
}

export const getSellerReviews = async () => {
  const response = (await request({ url: `${API_URL}/seller/reviews`, method: 'GET' })) as AxiosResponse<{ data: Review[] }>
  return response.data
}

export const replyReview = async (id: string, reply: string) => {
  const response = (await request({ url: `${API_URL}/seller/reviews/${id}/reply`, method: 'PUT', data: { reply } })) as AxiosResponse<{ data: Review }>
  return response.data
}
