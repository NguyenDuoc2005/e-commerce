import { ROLES } from "./roles";

export const { VITE_BASE_URL_SERVER } = import.meta.env || {};

export const { VITE_BASE_URL_CLIENT } = import.meta.env || {};

export const DOMAIN_BACKEND = `${VITE_BASE_URL_SERVER}` as string;

export const DOMAIN_FRONTEND = `${VITE_BASE_URL_CLIENT}` as string;

export const URL_FRONTEND = `${DOMAIN_FRONTEND}/redirect`;

// API URL
export const API_URL = `${VITE_BASE_URL_SERVER}/api/v1` as string;

export const SCREEN_ROLE_ADMIN = `&screen=${ROLES.ADMIN}`;
export const SCREEN_ROLE_USERS = `&screen=${ROLES.USERS}`;
export const REGISTER = `&register=true`;
export const NOT_REGISTER = `&register=false`;

// đăng nhập với google
export const URL_OAUTH2_GOOGLE_ADMIN = () =>
  `${DOMAIN_BACKEND}/oauth2/authorize/google?redirect_uri=${URL_FRONTEND}${SCREEN_ROLE_ADMIN}${NOT_REGISTER}` as string;
export const URL_OAUTH2_GOOGLE_USERS = () =>
  `${DOMAIN_BACKEND}/oauth2/authorize/google?redirect_uri=${URL_FRONTEND}${SCREEN_ROLE_USERS}${NOT_REGISTER}` as string;

// đăng nhập với github
export const URL_OAUTH2_GITHUB_ADMIN = () =>
  `${DOMAIN_BACKEND}/oauth2/authorize/github?redirect_uri=${URL_FRONTEND}${SCREEN_ROLE_ADMIN}${NOT_REGISTER}` as string;
export const URL_OAUTH2_GITHUB_USERS = () =>
  `${DOMAIN_BACKEND}/oauth2/authorize/github?redirect_uri=${URL_FRONTEND}${SCREEN_ROLE_USERS}${NOT_REGISTER}` as string;

export const PREFIX_API_AUTH = `${API_URL}/auth` as string;

export const PREFIX_API_REFRESH = (PREFIX_API_AUTH + `/refresh`) as string;

export const PREFIX_API_LOGIN = (PREFIX_API_AUTH + `/login`) as string;

export const PREFIX_API_LOGIN_ADMIN = (PREFIX_API_AUTH + `/login-admin`) as string;

export const PREFIX_API_REGISTER = (PREFIX_API_AUTH + `/register`) as string;

export const PREFIX_API_CHANGE_PASSWORD = (PREFIX_API_AUTH + `/change-password`) as string;

// API PREFIX ADMIN
export const PREFIX_API_ADMIN = `${API_URL}/admin` as string;

// API PREFIX Permitall
export const PREFIX_API_PERMITALL = `${API_URL}/permitall` as string;

// // API PREFIX CUSTOMER
// export const PREFIX_API_CUSTOMER = `${API_URL}/customer` as string;

export const PREFIX_API_THONG_KE_ADMIN = `${PREFIX_API_ADMIN}/thong-ke` as string;
export const PREFIX_API_MAU_SAC_ADMIN = `${PREFIX_API_ADMIN}/mau-sac` as string;
export const PREFIX_API_SIZE_ADMIN = `${PREFIX_API_ADMIN}/size` as string;
export const PREFIX_API_THUONG_HIEU_ADMIN = `${PREFIX_API_ADMIN}/thuong-hieu` as string;
export const PREFIX_API_XUAT_XU_ADMIN = `${PREFIX_API_ADMIN}/xuat-xu` as string;
export const PREFIX_API_KHACH_HANG_ADMIN = `${PREFIX_API_ADMIN}/khach-hang` as string;
export const PREFIX_API_NHAN_VIEN_ADMIN = `${PREFIX_API_ADMIN}/nhan-vien` as string;
export const PREFIX_API_SAN_PHAM_ADMIN = `${PREFIX_API_ADMIN}/san-pham` as string;
export const PREFIX_API_CHAT_LIEU_ADMIN = `${PREFIX_API_ADMIN}/chat-lieu` as string;
export const PREFIX_API_LOAI_DE_ADMIN = `${PREFIX_API_ADMIN}/loai-de` as string;
export const PREFIX_API_LOAI_GIAY_ADMIN = `${PREFIX_API_ADMIN}/danh-muc` as string;
export const PREFIX_API_SAN_PHAM_CHI_TIET_ADMIN = `${PREFIX_API_ADMIN}/san-pham-chi-tiet` as string;
export const PREFIX_API_BAN_HANG_ADMIN = `${PREFIX_API_ADMIN}/ban-hang` as string;
export const PREFIX_API_DOT_GIAM_GIA_ADMIN = `${PREFIX_API_ADMIN}/dot-giam-gia` as string;
export const PREFIX_API_HOA_DON_ADMIN = `${PREFIX_API_ADMIN}/hoa-don` as string;
export const PREFIX_API_VOUCHER_ADMIN = `${PREFIX_API_ADMIN}/voucher` as string;
export const PREFIX_API_HOA_DON_DETAIL_ADMIN = `${PREFIX_API_ADMIN}/hoa-don/:id` as string;
// Permitall API PREFIX
export const PREFIX_API_SANPHAM_PERMITALL = `${PREFIX_API_PERMITALL}/san-pham` as string;
export const PREFIX_API_SANPHAMCHITIET_PERMITALL =
  `${PREFIX_API_PERMITALL}/san-pham-chi-tiet` as string;
export const PREFIX_API_THUONGHIEU_PERMITALL = `${PREFIX_API_PERMITALL}/thuong-hieu` as string;
export const PREFIX_API_CART_PERMITALL = `${PREFIX_API_PERMITALL}/cart` as string;

export const PREFIX_API_PROFILE_PERMITALL = `${PREFIX_API_PERMITALL}/profile` as string;
export const PREFIX_API_PROFILE_ORDER_HISTORY = `${PREFIX_API_PERMITALL}/don-mua` as string;
export const PREFIX_API_PROFILE_ORDER_HISTORY_DETAIL = `${PREFIX_API_PERMITALL}/don-mua/:id` as string;

