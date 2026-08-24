import type { AxiosResponse } from 'axios'
import request from '@/services/request'
const base='/api/v1/admin/product-variant-axes'
export interface AxisInsight { name:string; usageCount:number; productCount:number; suggestions:Array<{id:string;name:string;verified:boolean;status:'ACTIVE'|'INACTIVE'}> }
export const getAxisInsights=async(q?:string)=>(await request.get(`${base}/insights`,{params:{q}}) as AxiosResponse<AxisInsight[]>).data||[]
export const createAxisSuggestion=async(name:string)=>(await request.post(`${base}/suggestions`,{name})).data
export const verifyAxisSuggestion=async(id:string)=>request.put(`${base}/suggestions/${id}/verify`)
