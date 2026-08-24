import { ROUTES_CONSTANTS } from "@/constants/path";
import { ROLES } from "@/constants/roles";
import { createRouter, createWebHistory } from "vue-router";
import type { RouteRecordRaw } from "vue-router";
import { USER_INFO_STORAGE_KEY } from '@/constants/storageKey'
import { localStorageAction } from '@/utils/storage'
import type { UserInformation } from '@/types/auth.type'

export const routes: RouteRecordRaw[] = [
  {
    path: ROUTES_CONSTANTS.REDIRECT.path,
    name: ROUTES_CONSTANTS.REDIRECT.name,
    component: () => import('@/routes/guard/Redirect.vue')
  },
  {
    path: ROUTES_CONSTANTS.USERS.path,
    redirect: `${ROUTES_CONSTANTS.USERS.path}/${ROUTES_CONSTANTS.USERS.children.TRANGCHU.path}`,
    component: () => import("@/layout/Users.vue"),
    children: [
      {
        path: ROUTES_CONSTANTS.USERS.children.TRANGCHU.path,
        name: ROUTES_CONSTANTS.USERS.children.TRANGCHU.name,
        component: () => import("@/pages/users/home/HomeView.vue"),
      },
      {
        path: ROUTES_CONSTANTS.USERS.children.LOGIN.path,
        name: ROUTES_CONSTANTS.USERS.children.LOGIN.name,
        component: () => import("@/pages/auth/LoginPage.vue"),
      },
      {
        path: ROUTES_CONSTANTS.USERS.children.REGISTER.path,
        name: ROUTES_CONSTANTS.USERS.children.REGISTER.name,

        component: () => import("@/pages/auth/RegisterPage.vue"),

      },
      {
        path: ROUTES_CONSTANTS.USERS.children.DANG_KY_BAN_HANG.path,
        name: ROUTES_CONSTANTS.USERS.children.DANG_KY_BAN_HANG.name,
        component: () => import("@/pages/users/seller/SellerRegistration.vue"),
      },
      {
        path: ROUTES_CONSTANTS.USERS.children.DONMUA.path,
        name: ROUTES_CONSTANTS.USERS.children.DONMUA.name,

        component: () => import("@/pages/users/orderhistory/OrderHistory.vue"),

      },
      {
        path: ROUTES_CONSTANTS.USERS.children.DONMUA_DETAIL.path,
        name: ROUTES_CONSTANTS.USERS.children.DONMUA_DETAIL.name,

        component: () => import("@/pages/users/orderhistory/OrderDetail.vue"),

      },
      {
        path: ROUTES_CONSTANTS.USERS.children.LIENHE.path,
        name: ROUTES_CONSTANTS.USERS.children.LIENHE.name,

        component: () => import("@/pages/users/home/ContactPage.vue"),

      },
      {
        path: ROUTES_CONSTANTS.USERS.children.GIOITHIEU.path,
        name: ROUTES_CONSTANTS.USERS.children.GIOITHIEU.name,

        component: () => import("@/pages/users/home/GioiThieu.vue"),

      },
      {
        path: ROUTES_CONSTANTS.USERS.children.TRACUU.path,
        name: ROUTES_CONSTANTS.USERS.children.TRACUU.name,

        component: () => import("@/pages/users/home/TraCuu.vue"),

      },
      {
        path: ROUTES_CONSTANTS.USERS.children.SANPHAM.path,
        name: ROUTES_CONSTANTS.USERS.children.SANPHAM.name,
        component: () => import("@/pages/users/products/ProductsView.vue"),
      },
      {
        path: ROUTES_CONSTANTS.USERS.children.SANPHAMCHITIET.path,
        name: ROUTES_CONSTANTS.USERS.children.SANPHAMCHITIET.name,
        component: () => import("@/pages/users/products/ProductDetail.vue"),
      },
      {
        path: ROUTES_CONSTANTS.USERS.children.SHOP_DETAIL.path,
        name: ROUTES_CONSTANTS.USERS.children.SHOP_DETAIL.name,
        component: () => import("@/pages/users/seller/ShopDetail.vue"),
      },
      {
        path: ROUTES_CONSTANTS.USERS.children.THANHTOAN.path,
        name: ROUTES_CONSTANTS.USERS.children.THANHTOAN.name,
        component: () => import("@/pages/users/checkout/CheckoutView.vue"),
      },
      {
        path: ROUTES_CONSTANTS.USERS.children.THANHTOANTHANHCONG.path,
        name: ROUTES_CONSTANTS.USERS.children.THANHTOANTHANHCONG.name,
        component: () => import("@/pages/users/checkout/CheckoutSuccess.vue"),
      },
      {
        path: ROUTES_CONSTANTS.USERS.children.GIOHANG.path,
        name: ROUTES_CONSTANTS.USERS.children.GIOHANG.name,
        component: () => import("@/pages/users/cart/CartView.vue"),
      },


      {
        path: ROUTES_CONSTANTS.USERS.children.THONGTINCANHAN.path,
        name: ROUTES_CONSTANTS.USERS.children.THONGTINCANHAN.name,
        component: () => import('@/pages/users/profile/ProfileView.vue')
      },

    ]

  },
  // Not Found route
  {
    path: ROUTES_CONSTANTS.NOT_FOUND.path,
    name: ROUTES_CONSTANTS.NOT_FOUND.name,
    component: () => import("@/pages/404/NotFound.vue"),
  },
  // 403 route
  {
    path: ROUTES_CONSTANTS.FORBIDDEN.path,
    name: ROUTES_CONSTANTS.FORBIDDEN.name,
    component: () => import("@/pages/403/Forbidden.vue"),
  },
  // 401 route
  {
    path: ROUTES_CONSTANTS.UNAUTHORIZED.path,
    name: ROUTES_CONSTANTS.UNAUTHORIZED.name,
    component: () => import("@/pages/401/Unauthorized.vue"),
  },

  {
    path: ROUTES_CONSTANTS.LOGIN.path,
    name: ROUTES_CONSTANTS.LOGIN.name,
    component: () => import('@/pages/auth/LoginAdmin.vue'),

  },

  {
    path: ROUTES_CONSTANTS.SELLER.path,
    redirect: `${ROUTES_CONSTANTS.SELLER.path}/${ROUTES_CONSTANTS.SELLER.children.DASHBOARD.path}`,
    component: () => import("@/layout/Admin.vue"),
    children: [
      {
        path: ROUTES_CONSTANTS.SELLER.children.DASHBOARD.path,
        name: ROUTES_CONSTANTS.SELLER.children.DASHBOARD.name,
        component: () => import("@/pages/seller/dashboard/SellerDashboard.vue"),
        meta: { requiresRole: 'SELLER', requiresAuth: true },
      },
      {
        path: ROUTES_CONSTANTS.SELLER.children.ORDERS.path,
        name: ROUTES_CONSTANTS.SELLER.children.ORDERS.name,
        component: () => import("@/pages/seller/orders/SellerOrders.vue"),
        meta: {
          requiresRole: 'SELLER',
          requiresAuth: true,
        },
      },
      {
        path: ROUTES_CONSTANTS.SELLER.children.PRODUCTS.path,
        name: ROUTES_CONSTANTS.SELLER.children.PRODUCTS.name,
        component: () => import("@/pages/seller/products/SellerProducts.vue"),
        meta: { requiresRole: 'SELLER', requiresAuth: true },
      },
      {
        path: ROUTES_CONSTANTS.SELLER.children.VOUCHERS.path,
        name: ROUTES_CONSTANTS.SELLER.children.VOUCHERS.name,
        component: () => import("@/pages/seller/vouchers/SellerVouchers.vue"),
        meta: {
          requiresRole: 'SELLER',
          requiresAuth: true,
        },
      },
      {
        path: ROUTES_CONSTANTS.SELLER.children.PAYOUT.path,
        name: ROUTES_CONSTANTS.SELLER.children.PAYOUT.name,
        component: () => import("@/pages/seller/payout/SellerPayout.vue"),
        meta: {
          requiresRole: 'SELLER',
          requiresAuth: true,
        },
      },
      {
        path: ROUTES_CONSTANTS.SELLER.children.REVIEWS.path,
        name: ROUTES_CONSTANTS.SELLER.children.REVIEWS.name,
        component: () => import("@/pages/seller/reviews/SellerReviews.vue"),
        meta: { requiresRole: 'SELLER', requiresAuth: true },
      },
    ],
  },

  {
    path: ROUTES_CONSTANTS.ADMIN.path,
    redirect: `${ROUTES_CONSTANTS.ADMIN.path}/${ROUTES_CONSTANTS.ADMIN.children.PRODUCT_ATTRIBUTES.path}`,
    component: () => import("@/layout/Admin.vue"),
    children: [
      {
        path: ROUTES_CONSTANTS.ADMIN.children.MAUSAC.path,
        name: ROUTES_CONSTANTS.ADMIN.children.MAUSAC.name,
        redirect: `${ROUTES_CONSTANTS.ADMIN.path}/${ROUTES_CONSTANTS.ADMIN.children.PRODUCT_ATTRIBUTES.path}`,
        // meta: {
        //   requiresRole: ROLES.ADMIN,
        //   requiresAuth: true
        // }
      },
    ]

  },
  {
    path: ROUTES_CONSTANTS.ADMIN.path,
    redirect: `${ROUTES_CONSTANTS.ADMIN.path}/${ROUTES_CONSTANTS.ADMIN.children.THONG_KE.path}`,
    component: () => import("@/layout/Admin.vue"),
    children: [
      {
        path: ROUTES_CONSTANTS.ADMIN.children.THONG_KE.path,
        name: ROUTES_CONSTANTS.ADMIN.children.THONG_KE.name,
        component: () => import("@/pages/admin/thongke/MarketplaceStatistics.vue"),
        meta: { requiresRole: ROLES.ADMIN, requiresAuth: true }
      },
    ],
  },
  {
    path: ROUTES_CONSTANTS.ADMIN.path,
    redirect: `${ROUTES_CONSTANTS.ADMIN.path}/${ROUTES_CONSTANTS.ADMIN.children.DOT_GIAM_GIA.path}`,
    component: () => import("@/layout/Admin.vue"),
    children: [
      {
        path: ROUTES_CONSTANTS.ADMIN.children.DOT_GIAM_GIA.path,
        name: ROUTES_CONSTANTS.ADMIN.children.DOT_GIAM_GIA.name,
        component: () => import("@/pages/admin/dotgiamgia/DotGiamGia.vue"),
        // meta: {
        //   requiresRole: ROLES.ADMIN,
        //   requiresAuth: true
        // }
      },
    ],
  },

  {
    path: ROUTES_CONSTANTS.ADMIN.path,
    redirect: `${ROUTES_CONSTANTS.ADMIN.path}/${ROUTES_CONSTANTS.ADMIN.children.ADD_DOT_GIAM_GIA.path}`,
    component: () => import("@/layout/Admin.vue"),
    children: [
      {
        path: ROUTES_CONSTANTS.ADMIN.children.ADD_DOT_GIAM_GIA.path,
        name: ROUTES_CONSTANTS.ADMIN.children.ADD_DOT_GIAM_GIA.name,
        component: () => import("@/pages/admin/dotgiamgia/DotGiamGiaModal.vue"),
        // meta: {
        //   requiresRole: ROLES.ADMIN,
        //   requiresAuth: true
        // }
      },
    ],
  },

  {
    path: ROUTES_CONSTANTS.ADMIN.path,
    redirect: `${ROUTES_CONSTANTS.ADMIN.path}/${ROUTES_CONSTANTS.ADMIN.children.UPDATE_DOT_GIAM_GIA.path}`,
    component: () => import("@/layout/Admin.vue"),
    children: [
      {
        path: ROUTES_CONSTANTS.ADMIN.children.UPDATE_DOT_GIAM_GIA.path,
        name: ROUTES_CONSTANTS.ADMIN.children.UPDATE_DOT_GIAM_GIA.name,
        component: () => import("@/pages/admin/dotgiamgia/DotGiamGiaUpdate.vue"),
        // meta: {
        //   requiresRole: ROLES.ADMIN,
        //   requiresAuth: true
        // }
      },
    ],
  },
  {
    path: ROUTES_CONSTANTS.ADMIN.path,
    redirect: `${ROUTES_CONSTANTS.ADMIN.path}/${ROUTES_CONSTANTS.ADMIN.children.PRODUCT_ATTRIBUTES.path}`,
    component: () => import("@/layout/Admin.vue"),
    children: [
      {
        path: ROUTES_CONSTANTS.ADMIN.children.SIZE.path,
        name: ROUTES_CONSTANTS.ADMIN.children.SIZE.name,
        redirect: `${ROUTES_CONSTANTS.ADMIN.path}/${ROUTES_CONSTANTS.ADMIN.children.PRODUCT_ATTRIBUTES.path}`,
        // meta: {
        //   requiresRole: ROLES.ADMIN,
        //   requiresAuth: true
        // }
      },
    ],
  },
  {
    path: ROUTES_CONSTANTS.ADMIN.path,
    redirect: `${ROUTES_CONSTANTS.ADMIN.path}/${ROUTES_CONSTANTS.ADMIN.children.PRODUCT_ATTRIBUTES.path}`,
    component: () => import("@/layout/Admin.vue"),
    children: [
      {
        path: ROUTES_CONSTANTS.ADMIN.children.THUONG_HIEU.path,
        name: ROUTES_CONSTANTS.ADMIN.children.THUONG_HIEU.name,
        redirect: `${ROUTES_CONSTANTS.ADMIN.path}/${ROUTES_CONSTANTS.ADMIN.children.PRODUCT_ATTRIBUTES.path}`,
        // meta: {
        //   requiresRole: ROLES.ADMIN,
        //   requiresAuth: true
        // }

      },
    ],

  },
  {
    path: ROUTES_CONSTANTS.ADMIN.path,
    redirect: `${ROUTES_CONSTANTS.ADMIN.path}/${ROUTES_CONSTANTS.ADMIN.children.KHACH_HANG.path}`,
    component: () => import("@/layout/Admin.vue"),
    children: [
      {
        path: ROUTES_CONSTANTS.ADMIN.children.KHACH_HANG.path,
        name: ROUTES_CONSTANTS.ADMIN.children.KHACH_HANG.name,
        component: () => import("@/pages/admin/khachhang/KhachHang.vue"),
        // meta: {
        //   requiresRole: ROLES.ADMIN,
        //   requiresAuth: true
        // }
      },
    ],
  },
  {
    path: ROUTES_CONSTANTS.ADMIN.path,
    redirect: `${ROUTES_CONSTANTS.ADMIN.path}/${ROUTES_CONSTANTS.ADMIN.children.NHAN_VIEN.path}`,
    component: () => import("@/layout/Admin.vue"),
    children: [
      {
        path: ROUTES_CONSTANTS.ADMIN.children.NHAN_VIEN.path,
        name: ROUTES_CONSTANTS.ADMIN.children.NHAN_VIEN.name,
        component: () => import("@/pages/admin/nhanvien/NhanVien.vue"),
        // meta: {
        //   requiresRole: ROLES.ADMIN,
        //   requiresAuth: true
        // }
      },
    ],
  },
  {
    path: ROUTES_CONSTANTS.ADMIN.path,
    redirect: `${ROUTES_CONSTANTS.ADMIN.path}/${ROUTES_CONSTANTS.ADMIN.children.PRODUCT_ATTRIBUTES.path}`,
    component: () => import("@/layout/Admin.vue"),
    children: [
      {
        path: ROUTES_CONSTANTS.ADMIN.children.CHAT_LIEU.path,
        name: ROUTES_CONSTANTS.ADMIN.children.CHAT_LIEU.name,
        redirect: `${ROUTES_CONSTANTS.ADMIN.path}/${ROUTES_CONSTANTS.ADMIN.children.PRODUCT_ATTRIBUTES.path}`,
        // meta: {
        //   requiresRole: ROLES.ADMIN,
        //   requiresAuth: true
        // }
      },
    ],
  },
  {
    path: ROUTES_CONSTANTS.ADMIN.path,
    redirect: `${ROUTES_CONSTANTS.ADMIN.path}/${ROUTES_CONSTANTS.ADMIN.children.PRODUCT_ATTRIBUTES.path}`,
    component: () => import("@/layout/Admin.vue"),
    children: [
      {
        path: ROUTES_CONSTANTS.ADMIN.children.LOAI_DE.path,
        name: ROUTES_CONSTANTS.ADMIN.children.LOAI_DE.name,
        redirect: `${ROUTES_CONSTANTS.ADMIN.path}/${ROUTES_CONSTANTS.ADMIN.children.PRODUCT_ATTRIBUTES.path}`,
        // meta: {
        //   requiresRole: ROLES.ADMIN,
        //   requiresAuth: true
        // }
      },
    ],
  },
  {
    path: ROUTES_CONSTANTS.ADMIN.path,
    redirect: `${ROUTES_CONSTANTS.ADMIN.path}/${ROUTES_CONSTANTS.ADMIN.children.PRODUCT_ATTRIBUTES.path}`,
    component: () => import("@/layout/Admin.vue"),
    children: [
      {
        path: ROUTES_CONSTANTS.ADMIN.children.LOAI_GIAY.path,
        name: ROUTES_CONSTANTS.ADMIN.children.LOAI_GIAY.name,
        redirect: `${ROUTES_CONSTANTS.ADMIN.path}/${ROUTES_CONSTANTS.ADMIN.children.PRODUCT_ATTRIBUTES.path}`,
        // meta: {
        //   requiresRole: ROLES.ADMIN,
        //   requiresAuth: true
        // }
      },
    ],
  },
  {
    path: ROUTES_CONSTANTS.ADMIN.path,
    redirect: `${ROUTES_CONSTANTS.ADMIN.path}/${ROUTES_CONSTANTS.ADMIN.children.THEM_NHAN_VIEN.path}`,
    component: () => import("@/layout/Admin.vue"),
    children: [
      {
        path: ROUTES_CONSTANTS.ADMIN.children.THEM_NHAN_VIEN.path,
        name: ROUTES_CONSTANTS.ADMIN.children.THEM_NHAN_VIEN.name,
        component: () => import("@/pages/admin/nhanvien/NhanVienModal.vue"),
        // meta: {
        //   requiresRole: ROLES.ADMIN,
        //   requiresAuth: true
        // }
      },
    ],
  },
  {
    path: ROUTES_CONSTANTS.ADMIN.path,
    redirect: `${ROUTES_CONSTANTS.ADMIN.path}/${ROUTES_CONSTANTS.ADMIN.children.THEM_KHACH_HANG.path}`,
    component: () => import("@/layout/Admin.vue"),
    children: [
      {
        path: ROUTES_CONSTANTS.ADMIN.children.THEM_KHACH_HANG.path,
        name: ROUTES_CONSTANTS.ADMIN.children.THEM_KHACH_HANG.name,
        component: () => import("@/pages/admin/khachhang/KhachHangModal.vue"),
        // meta: {
        //   requiresRole: ROLES.ADMIN,
        //   requiresAuth: true
        // }
      },
    ],
  },
  {
    path: ROUTES_CONSTANTS.ADMIN.path,
    redirect: `${ROUTES_CONSTANTS.ADMIN.path}/${ROUTES_CONSTANTS.ADMIN.children.THEM_PHIEU_GIAM_GIA.path}`,
    component: () => import("@/layout/Admin.vue"),
    children: [
      {
        path: ROUTES_CONSTANTS.ADMIN.children.THEM_PHIEU_GIAM_GIA.path,
        name: ROUTES_CONSTANTS.ADMIN.children.THEM_PHIEU_GIAM_GIA.name,
        component: () => import("@/pages/admin/voucher/VoucherModal.vue"),
        // meta: {
        //   requiresRole: ROLES.ADMIN,
        //   requiresAuth: true
        // }
      },
    ],
  },
  {
    path: ROUTES_CONSTANTS.ADMIN.path,
    redirect: `${ROUTES_CONSTANTS.ADMIN.path}/${ROUTES_CONSTANTS.ADMIN.children.VOUCHER.path}`,
    component: () => import("@/layout/Admin.vue"),
    children: [
      {
        path: ROUTES_CONSTANTS.ADMIN.children.VOUCHER.path,
        name: ROUTES_CONSTANTS.ADMIN.children.VOUCHER.name,
        component: () => import("@/pages/admin/voucher/Voucher.vue"),
        // meta: {
        //   requiresRole: ROLES.ADMIN,
        //   requiresAuth: true
        // }
      },
    ],
  },
  {
    path: ROUTES_CONSTANTS.ADMIN.path,
    redirect: `${ROUTES_CONSTANTS.ADMIN.path}/${ROUTES_CONSTANTS.ADMIN.children.SELLER_APPROVAL.path}`,
    component: () => import("@/layout/Admin.vue"),
    children: [
      {
        path: ROUTES_CONSTANTS.ADMIN.children.SELLER_APPROVAL.path,
        name: ROUTES_CONSTANTS.ADMIN.children.SELLER_APPROVAL.name,
        component: () => import("@/pages/admin/seller/SellerApproval.vue"),
        meta: {
          requiresRole: ROLES.ADMIN,
          requiresAuth: true,
        },
      },
    ],
  },
  {
    path: ROUTES_CONSTANTS.ADMIN.path,
    component: () => import("@/layout/Admin.vue"),
    children: [
      {
        path: ROUTES_CONSTANTS.ADMIN.children.BANNERS.path,
        name: ROUTES_CONSTANTS.ADMIN.children.BANNERS.name,
        component: () => import("@/pages/admin/banner/PlatformBanners.vue"),
        meta: { requiresRole: ROLES.ADMIN, requiresAuth: true },
      },
    ],
  },
  {
    path: ROUTES_CONSTANTS.ADMIN.path,
    component: () => import("@/layout/Admin.vue"),
    children: [
      {
        path: ROUTES_CONSTANTS.ADMIN.children.PAYOUT.path,
        name: ROUTES_CONSTANTS.ADMIN.children.PAYOUT.name,
        component: () => import("@/pages/admin/payout/AdminPayout.vue"),
        meta: { requiresRole: ROLES.ADMIN, requiresAuth: true },
      },
    ],
  },
  {
    path: ROUTES_CONSTANTS.ADMIN.path,
    component: () => import("@/layout/Admin.vue"),
    children: [
      {
        path: ROUTES_CONSTANTS.ADMIN.children.PRODUCT_ATTRIBUTES.path,
        name: ROUTES_CONSTANTS.ADMIN.children.PRODUCT_ATTRIBUTES.name,
        component: () => import("@/pages/admin/product-attributes/ProductAttributes.vue"),
        meta: { requiresRole: ROLES.ADMIN, requiresAuth: true },
      },
    ],
  },
];

export const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to) => {
  const user = localStorageAction.get(USER_INFO_STORAGE_KEY) as UserInformation | null
  if (to.meta.requiresAuth && !user) {
    return { name: ROUTES_CONSTANTS.USERS.children.LOGIN.name, query: { redirect: to.fullPath } }
  }
  const requiredRole = to.meta.requiresRole as string | undefined
  const roles = user?.roles?.length ? user.roles : user?.role ? [user.role] : []
  if (requiredRole && !roles.includes(requiredRole)) {
    return { name: ROUTES_CONSTANTS.FORBIDDEN.name }
  }
  return true
})
