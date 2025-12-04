import { ROUTES_CONSTANTS } from '@/constants/path'
import {
  ACCESS_TOKEN_STORAGE_KEY,
  REFRESH_TOKEN_STORAGE_KEY,
  USER_INFO_STORAGE_KEY
} from '@/constants/storageKey'
import { API_URL, PREFIX_API_AUTH, PREFIX_API_REFRESH } from '@/constants/url'
import type { DefaultResponse } from '@/utils/types/api.common'
import { localStorageAction } from '@/utils/storage'
import axios from 'axios'
import type { AxiosResponse } from 'axios';
import { getUserInformation } from '@/utils/token.helper'


const request = axios.create({
  baseURL: `${API_URL}`
})

request.interceptors.request.use((config) => {
  const accessToken = localStorageAction.get(ACCESS_TOKEN_STORAGE_KEY)
  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`
  }
  return config
})

// Danh sách tất cả path login bạn dùng trên FE
const LOGIN_PATHS = [
  ROUTES_CONSTANTS.USERS.children.LOGIN.path,
  // Thêm các path login khác nếu có
  ROUTES_CONSTANTS.ADMIN.children.LOGIN?.path,     // Nếu có
  ROUTES_CONSTANTS.USERS.children.LOGIN?.path, // Nếu có
  '/login',                               
  '/admin/login',
  '/dang-nhap'
].filter(Boolean); // Loại undefined nếu không có

request.interceptors.response.use(
  (response) => {
    return response;
  },
  async (error) => {
    const originalRequest = error.config;

    // Hàm kiểm tra có phải trang login không
    const isLoginPage = LOGIN_PATHS.includes(window.location.pathname);

    if (
      error.response &&
      error.response.status === 401 &&
      !originalRequest._retry &&
      !isLoginPage
    ) {
      originalRequest._retry = true;

      const refreshToken = localStorageAction.get(REFRESH_TOKEN_STORAGE_KEY);
      if (refreshToken) {
        try {
          const response = (await axios.post(`${PREFIX_API_AUTH}/refresh`, {
            refreshToken
          })) as AxiosResponse<DefaultResponse<{ accessToken: string; refreshToken: string }>>;
          const newAccessToken = response.data.data.accessToken;
          const newRefreshToken = response.data.data.refreshToken;
          localStorageAction.set(ACCESS_TOKEN_STORAGE_KEY, newAccessToken);
          localStorageAction.set(REFRESH_TOKEN_STORAGE_KEY, newRefreshToken);
          localStorageAction.set(USER_INFO_STORAGE_KEY, getUserInformation(newAccessToken));
          originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
          return request(originalRequest);
        } catch (refreshError) {
          console.log('🚀 ~ refreshError:', refreshError);
        }
      }

      // Nếu không refresh được hoặc không có refreshToken, logout & redirect
      localStorageAction.remove(ACCESS_TOKEN_STORAGE_KEY);
      localStorageAction.remove(REFRESH_TOKEN_STORAGE_KEY);
      localStorageAction.remove(USER_INFO_STORAGE_KEY);
      window.location.href = ROUTES_CONSTANTS.UNAUTHORIZED.path;
    } else if (
      error.response &&
      error.response.status === 403 &&
      !isLoginPage
    ) {
      window.location.href = ROUTES_CONSTANTS.FORBIDDEN.path;
      console.log("lỗi ", error.response);
    }

    return Promise.reject(error);
  }
);


export default request
