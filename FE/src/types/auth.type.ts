export interface DecodedToken {
  userId: string
  fullName: string
  email: string
  pictureUrl:string
  role: string
  exp: number
  iat: number
  iss: string
}
export interface UserInformation {
  userId: string
  fullName: string
  pictureUrl:string
  email: string
  role: string
}
