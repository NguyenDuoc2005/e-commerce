import type { AxiosResponse } from 'axios'
import request from '@/services/request'
import { API_URL } from '@/constants/url'

export type ChatRole = 'buyer' | 'seller'
export type ChatSenderType = 'BUYER' | 'SELLER'

export interface ChatConversation {
  id: string
  customerId: string
  sellerId: string
  buyerName: string
  shopName: string
  shopLogoUrl?: string
  lastMessage?: string
  lastMessageAt?: string
  unreadCount: number
  createdAt: string
}

export interface ChatMessage {
  id: string
  conversationId: string
  senderType: ChatSenderType
  senderId: string
  content: string
  createdAt: string
  readAt?: string
}

const baseUrl = (role: ChatRole) => `${API_URL}/${role}/chat`

const call = async <T>(url: string, method: 'GET' | 'POST' = 'GET', data?: unknown) => {
  const response = await request({ url, method, data }) as AxiosResponse<T>
  return response.data
}

export const startBuyerConversation = (sellerId: string) =>
  call<ChatConversation>(`${baseUrl('buyer')}/conversations`, 'POST', { sellerId })

export const getChatConversations = (role: ChatRole) =>
  call<ChatConversation[]>(`${baseUrl(role)}/conversations`)

export const getChatMessages = (role: ChatRole, conversationId: string) =>
  call<ChatMessage[]>(`${baseUrl(role)}/conversations/${conversationId}/messages`)

export const sendChatMessage = (role: ChatRole, conversationId: string, content: string) =>
  call<ChatMessage>(`${baseUrl(role)}/conversations/${conversationId}/messages`, 'POST', { content })

export const markChatRead = (role: ChatRole, conversationId: string) =>
  call<ChatConversation>(`${baseUrl(role)}/conversations/${conversationId}/read`, 'POST')
