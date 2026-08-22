import type { AxiosResponse } from 'axios'
import request from '@/services/request'
import { PREFIX_API_SELLER_ORDERS } from '@/constants/url'

export interface SellerOrder {
  id: string
  order_id: string
  order_code: string
  shop_name: string
  total_amount: number
  total_after_discount: number
  order_status: number
  created_date: number
  receiver_name?: string
  receiver_phone?: string
  shipping_address?: string
  items?: SellerOrderItem[]
}

export interface SellerOrderItem {
  id: string
  product_variant_id: string
  quantity: number
  sale_price: number
}

export interface SellerDashboardData {
  todayRevenue: number
  weekRevenue: number
  monthRevenue: number
  pendingOrders: number
  shippingOrders: number
  completedOrders: number
  revenueSeries: Array<{ date: string; revenue: number }>
}

export const getSellerOrders = async (params?: { status?: number | null; q?: string }) => {
  const res = (await request({
    url: PREFIX_API_SELLER_ORDERS,
    method: 'GET',
    params
  })) as AxiosResponse<SellerOrder[]>

  return res.data
}

export const getSellerOrderDetail = async (id: string) => {
  const res = (await request({
    url: `${PREFIX_API_SELLER_ORDERS}/${id}`,
    method: 'GET'
  })) as AxiosResponse<SellerOrder>

  return res.data
}

export const changeSellerOrderStatus = async (id: string, action: 'confirm' | 'ready-to-ship' | 'shipping' | 'complete' | 'cancel') => {
  const res = (await request({
    url: `${PREFIX_API_SELLER_ORDERS}/${id}/${action}`,
    method: 'POST'
  })) as AxiosResponse<SellerOrder>

  return res.data
}

export const getSellerDashboard = async () => {
  const res = (await request({
    url: `${PREFIX_API_SELLER_ORDERS}/dashboard`,
    method: 'GET'
  })) as AxiosResponse<SellerDashboardData>
  return res.data
}
