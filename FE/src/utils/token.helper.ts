import type { DecodedToken, UserInformation } from '@/types/auth.type'
import { jwtDecode } from 'jwt-decode'

export const getUserInformation = (token: string): UserInformation => {
  const decoded = jwtDecode<DecodedToken>(token)

  return {
    userId: decoded.userId,
    fullName: decoded.fullName,
    email: decoded.email,
    role: decoded.role,
    pictureUrl:decoded.pictureUrl
  }
}

export const getUserRole = (token: string): string => {
  const decoded = jwtDecode<DecodedToken>(token)
  return decoded.role
}

export const getExpireTime = (token: string): number => {
  const decoded = jwtDecode<DecodedToken>(token)
  return decoded.exp
}
