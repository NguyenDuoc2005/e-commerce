import { ROUTES_CONSTANTS } from "@/constants/path";
import { ROLES } from "@/constants/roles";
import { createRouter, createWebHistory } from "vue-router";
import type { RouteRecordRaw } from "vue-router";

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
    path: ROUTES_CONSTANTS.ADMIN.path,
    redirect: `${ROUTES_CONSTANTS.ADMIN.path}/${ROUTES_CONSTANTS.ADMIN.children.MAUSAC.path}`,
    component: () => import("@/layout/Admin.vue"),
    children: [
      {
        path: ROUTES_CONSTANTS.ADMIN.children.MAUSAC.path,
        name: ROUTES_CONSTANTS.ADMIN.children.MAUSAC.name,
        component: () => import("@/pages/admin/mausac/MauSac.vue"),
        // meta: {
        //   requiresRole: ROLES.ADMIN,
        //   requiresAuth: true
        // }
      },
    ]

  },
  {
    path: ROUTES_CONSTANTS.ADMIN.path,
    redirect: `${ROUTES_CONSTANTS.ADMIN.path}/${ROUTES_CONSTANTS.ADMIN.children.HOA_DON.path}`,
    component: () => import("@/layout/Admin.vue"),
    children: [
      {
        path: ROUTES_CONSTANTS.ADMIN.children.HOA_DON.path,
        name: ROUTES_CONSTANTS.ADMIN.children.HOA_DON.name,
        component: () => import("@/pages/admin/hoadon/HoaDon.vue"),
        meta: {
          requiresRole: ROLES.ADMIN,
          requiresAuth: true,
        },
      },
    ],
  },
  {
    path: ROUTES_CONSTANTS.ADMIN.path,
    redirect: `${ROUTES_CONSTANTS.ADMIN.path}/${ROUTES_CONSTANTS.ADMIN.children.HOA_DON_DETAIL.path}`,
    component: () => import("@/layout/Admin.vue"),
    children: [
      {
        path: ROUTES_CONSTANTS.ADMIN.children.HOA_DON_DETAIL.path,
        name: ROUTES_CONSTANTS.ADMIN.children.HOA_DON_DETAIL.name,
        component: () => import("@/pages/admin/hoadon/HoaDonModal.vue"),
        meta: {
          requiresRole: ROLES.ADMIN,
          requiresAuth: true,
        },
      },
    ],
  },
  {
    path: ROUTES_CONSTANTS.ADMIN.path,
    redirect: `${ROUTES_CONSTANTS.ADMIN.path}/${ROUTES_CONSTANTS.ADMIN.children.THONG_KE.path}`,
    component: () => import("@/layout/Admin.vue"),
    children: [
      {
        path: ROUTES_CONSTANTS.ADMIN.children.THONG_KE.path,
        name: ROUTES_CONSTANTS.ADMIN.children.THONG_KE.name,
        component: () => import("@/pages/admin/thongke/ThongKe.vue"),
        // meta: {
        //   requiresRole: ROLES.ADMIN,
        //   requiresAuth: true
        // }
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
    redirect: `${ROUTES_CONSTANTS.ADMIN.path}/${ROUTES_CONSTANTS.ADMIN.children.SIZE.path}`,
    component: () => import("@/layout/Admin.vue"),
    children: [
      {
        path: ROUTES_CONSTANTS.ADMIN.children.SIZE.path,
        name: ROUTES_CONSTANTS.ADMIN.children.SIZE.name,
        component: () => import("@/pages/admin/size/Size.vue"),
        // meta: {
        //   requiresRole: ROLES.ADMIN,
        //   requiresAuth: true
        // }
      },
    ],
  },
  {
    path: ROUTES_CONSTANTS.ADMIN.path,
    redirect: `${ROUTES_CONSTANTS.ADMIN.path}/${ROUTES_CONSTANTS.ADMIN.children.THUONG_HIEU.path}`,
    component: () => import("@/layout/Admin.vue"),
    children: [
      {
        path: ROUTES_CONSTANTS.ADMIN.children.THUONG_HIEU.path,
        name: ROUTES_CONSTANTS.ADMIN.children.THUONG_HIEU.name,
        component: () => import("@/pages/admin/thuonghieu/ThuongHieu.vue"),
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
    redirect: `${ROUTES_CONSTANTS.ADMIN.path}/${ROUTES_CONSTANTS.ADMIN.children.SAN_PHAM.path}`,
    component: () => import("@/layout/Admin.vue"),
    children: [
      {
        path: ROUTES_CONSTANTS.ADMIN.children.SAN_PHAM.path,
        name: ROUTES_CONSTANTS.ADMIN.children.SAN_PHAM.name,
        component: () => import("@/pages/admin/sanpham/SanPham.vue"),
        // meta: {
        //   requiresRole: ROLES.ADMIN,
        //   requiresAuth: true
        // }
      },
    ],
  },

  {
    path: ROUTES_CONSTANTS.ADMIN.path,
    redirect: `${ROUTES_CONSTANTS.ADMIN.path}/${ROUTES_CONSTANTS.ADMIN.children.SAN_PHAM_CHI_TIET.path}`,
    component: () => import("@/layout/Admin.vue"),
    children: [
      {
        path: ROUTES_CONSTANTS.ADMIN.children.SAN_PHAM_CHI_TIET.path,
        name: ROUTES_CONSTANTS.ADMIN.children.SAN_PHAM_CHI_TIET.name,
        component: () => import("@/pages/admin/sanphamchitiet/SanPhamChiTiet.vue"),
        // meta: {
        //   requiresRole: ROLES.ADMIN,
        //   requiresAuth: true
        // }
      },
    ],
  },
  {
    path: ROUTES_CONSTANTS.ADMIN.path,
    redirect: `${ROUTES_CONSTANTS.ADMIN.path}/${ROUTES_CONSTANTS.ADMIN.children.CHAT_LIEU.path}`,
    component: () => import("@/layout/Admin.vue"),
    children: [
      {
        path: ROUTES_CONSTANTS.ADMIN.children.CHAT_LIEU.path,
        name: ROUTES_CONSTANTS.ADMIN.children.CHAT_LIEU.name,
        component: () => import("@/pages/admin/chatlieu/ChatLieu.vue"),
        // meta: {
        //   requiresRole: ROLES.ADMIN,
        //   requiresAuth: true
        // }
      },
    ],
  },
  {
    path: ROUTES_CONSTANTS.ADMIN.path,
    redirect: `${ROUTES_CONSTANTS.ADMIN.path}/${ROUTES_CONSTANTS.ADMIN.children.LOAI_DE.path}`,
    component: () => import("@/layout/Admin.vue"),
    children: [
      {
        path: ROUTES_CONSTANTS.ADMIN.children.LOAI_DE.path,
        name: ROUTES_CONSTANTS.ADMIN.children.LOAI_DE.name,
        component: () => import("@/pages/admin/loaide/LoaiDe.vue"),
        // meta: {
        //   requiresRole: ROLES.ADMIN,
        //   requiresAuth: true
        // }
      },
    ],
  },
  {
    path: ROUTES_CONSTANTS.ADMIN.path,
    redirect: `${ROUTES_CONSTANTS.ADMIN.path}/${ROUTES_CONSTANTS.ADMIN.children.LOAI_GIAY.path}`,
    component: () => import("@/layout/Admin.vue"),
    children: [
      {
        path: ROUTES_CONSTANTS.ADMIN.children.LOAI_GIAY.path,
        name: ROUTES_CONSTANTS.ADMIN.children.LOAI_GIAY.name,
        component: () => import("@/pages/admin/loaigiay/LoaiGiay.vue"),
        // meta: {
        //   requiresRole: ROLES.ADMIN,
        //   requiresAuth: true
        // }
      },
    ],
  },
  {
    path: ROUTES_CONSTANTS.ADMIN.path,
    redirect: `${ROUTES_CONSTANTS.ADMIN.path}/${ROUTES_CONSTANTS.ADMIN.children.THEM_SAN_PHAM_CHI_TIET.path}`,
    component: () => import("@/layout/Admin.vue"),
    children: [
      {
        path: ROUTES_CONSTANTS.ADMIN.children.THEM_SAN_PHAM_CHI_TIET.path,
        name: ROUTES_CONSTANTS.ADMIN.children.THEM_SAN_PHAM_CHI_TIET.name,
        component: () =>
          import("@/pages/admin/sanphamchitiet/createsanpham/CreateSanPhamChiTiet.vue"),
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
    redirect: `${ROUTES_CONSTANTS.ADMIN.path}/${ROUTES_CONSTANTS.ADMIN.children.BAN_HANG.path}`,
    component: () => import("@/layout/Admin.vue"),
    children: [
      {
        path: ROUTES_CONSTANTS.ADMIN.children.BAN_HANG.path,
        name: ROUTES_CONSTANTS.ADMIN.children.BAN_HANG.name,
        component: () => import("@/pages/admin/banhang/BanHang.vue"),
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
];

export const router = createRouter({
  history: createWebHistory(),
  routes,
});
