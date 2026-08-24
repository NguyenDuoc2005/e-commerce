import type { AxiosResponse } from 'axios'
import request from '@/services/request'
import { PREFIX_API_PRODUCT_VARIANT_AXES_ADMIN } from '@/constants/url'
const base = PREFIX_API_PRODUCT_VARIANT_AXES_ADMIN
export type AxisSuggestionStatus = 'ACTIVE' | 'INACTIVE'
export interface AxisInsight { normalizedName:string; displayName:string; usageCount:number; topValues:string[] }
export interface AxisSuggestion { id:string; name:string; verified:boolean; status:AxisSuggestionStatus; resolvedSuggestionId:string }
export const getAxisInsights=async(q?:string)=>(await request.get(`${base}/insights`,{params:{q}}) as AxiosResponse<AxisInsight[]>).data||[]
export const getAxisSuggestions=async(q?:string)=>(await request.get(`${base}/suggestions`,{params:{q}}) as AxiosResponse<AxisSuggestion[]>).data||[]
export const createAxisSuggestion=async(name:string)=>(await request.post(`${base}/suggestions`,{name}) as AxiosResponse<AxisSuggestion>).data
export const verifyAxisSuggestion=async(id:string)=>request.put(`${base}/suggestions/${id}/verify`)
export const mergeAxisSuggestion=async(id:string,targetId:string)=>request.post(`${base}/suggestions/${id}/merge`,{targetId})
export const hideAxisSuggestion=async(id:string)=>request.put(`${base}/suggestions/${id}/hide`)
