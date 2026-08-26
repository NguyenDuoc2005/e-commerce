export const ROUTES_CONSTANTS = {
  // AUTHENTICATION: {
  //   path: "/auth",
  //   name: "authentication",
  //   children: {
  //     LOGIN: {
  //       path: "login",
  //       name: "login",
  //     },
  //   },
  // },

  // Menu: {
  //   path: '/menu',
  //   name: 'menu'
  // },
  USERS: {
    path: '',
    name: 'users',
    children: {
      TRANGCHU: {
        path: 'trang-chu',
        name: 'trang-chu'
      },
      SANPHAM: {
        path: 'san-pham',
        name: 'san-pham'
      },
      SANPHAMCHITIET: {
        path: 'san-pham-chi-tiet/:idsp',
        name: 'san-pham-chi-tiet'
      },
      GIOHANG: {
        path: 'gio-hang',
        name: 'gio-hang'
      },
      LIENHE: {
        path: 'lien-he',
        name: 'lien-he'
      },
      GIOITHIEU: {
        path: 'gioi-thieu',
        name: 'gioi-thieu'
      },
      TRACUU: {
        path: 'tra-cuu',
        name: 'tra-cuu'
      },
      DONMUA: {
        path: 'don-mua',
        name: 'don-mua'
      },
      DONMUA_DETAIL: {
        path: 'don-mua-detail/:maHoaDon/:id',
        name: 'don-mua-detail'
      },
      DISPUTES: {
        path: 'khieu-nai',
        name: 'buyer-disputes'
      },
      CHAT: {
        path: 'tin-nhan',
        name: 'buyer-chat'
      },
      FLASH_SALE: {
        path: 'flash-sale',
        name: 'flash-sale'
      },
      THONGTINCANHAN: {
        path: 'thong-tin-ca-nhan',
        name: 'thong-tin-ca-nhan'
      },
      THANHTOAN: {
        path: 'thanh-toan',
        name: 'thanh-toan'
      },
      THANHTOANTHANHCONG: {
        path: 'thanh-toan-thanh-cong',
        name: 'thanh-toan-thanh-cong'
      },
      LOGIN: {
        path: 'login',
        name: 'Login'
      },
      REGISTER: {
        path: 'register',
        name: 'register'
      },
      DANG_KY_BAN_HANG: {
        path: 'dang-ky-ban-hang',
        name: 'dang-ky-ban-hang'
      },
      SHOP_DETAIL: {
        path: 'shop/:sellerSlug',
        name: 'shop-detail'
      },
    }
  },

  ADMIN: {
    path: '/admin',
    name: 'admin',
    children: {
      THONG_KE: {
        path: 'thong-ke',
        name: 'thong-ke-admin'
      },
      NHAN_VIEN: {
        path: 'nhan-vien',
        name: 'nhan-vien-admin'
      },
      XUAT_XU: {
        path: 'xuat-xu',
        name: 'xuat-xu-admin'
      },
      KHACH_HANG: {
        path: 'khach-hang',
        name: 'khach-hang-admin'
      },
      THEM_NHAN_VIEN: {
        path: 'them-nhan-vien',
        name: 'them-nhan-vien-admin'
      },
      THEM_KHACH_HANG: {
        path: 'them-khach-hang',
        name: 'them-khach-hang-admin'
      },
      VOUCHER: {
        path: 'voucher',
        name: 'voucher-admin'
      },
      FLASH_SALES: {
        path: 'flash-sales',
        name: 'flash-sales-admin'
      },
      SELLER_APPROVAL: {
        path: 'seller-approval',
        name: 'seller-approval-admin'
      },
      BANNERS: {
        path: 'banners',
        name: 'platform-banners-admin'
      },
      PAYOUT: {
        path: 'payout',
        name: 'payout-admin'
      },
      DISPUTES: {
        path: 'disputes',
        name: 'disputes-admin'
      },
      REPORTS: {
        path: 'reports',
        name: 'reports-admin'
      },
      PRODUCT_ATTRIBUTES: {
        path: 'product-attributes',
        name: 'product-attributes-admin'
      },
      CATEGORIES: {
        path: 'categories',
        name: 'categories-admin'
      },
      DOT_GIAM_GIA: {
        path: 'dot-giam-gia',
        name: 'dot-giam-gia-admin'
      },
      ADD_DOT_GIAM_GIA: {
        path: 'add-dot-giam-gia',
        name: 'add-dot-giam-gia-admin'
      },
      UPDATE_DOT_GIAM_GIA: {
        path: 'update-dot-giam-gia/:id',
        name: 'update-dot-giam-gia-admin'
      },
    }
  },

  SELLER: {
    path: '/seller',
    name: 'seller',
    children: {
      DASHBOARD: {
        path: 'dashboard',
        name: 'seller-dashboard'
      },
      ORDERS: {
        path: 'orders',
        name: 'seller-orders'
      },
      PRODUCTS: {
        path: 'products',
        name: 'seller-products'
      },
      VOUCHERS: {
        path: 'vouchers',
        name: 'seller-vouchers'
      },
      FLASH_SALES: {
        path: 'flash-sales',
        name: 'seller-flash-sales'
      },
      PAYOUT: {
        path: 'payout',
        name: 'seller-payout'
      },
      REVIEWS: {
        path: 'reviews',
        name: 'seller-reviews'
      },
      DISPUTES: {
        path: 'disputes',
        name: 'seller-disputes'
      },
      CHAT: {
        path: 'chat',
        name: 'seller-chat'
      },
      PROFILE: {
        path: 'profile',
        name: 'seller-profile'
      },
    }
  },

  REDIRECT: {
    path: '/redirect',
    name: 'redirect'
  },

  FORBIDDEN: {
    path: '/error/403',
    name: 'Forbidden'
  },

  LOGIN: {
    path: '/admin/login',
    name: 'login-admin'
  },

  UNAUTHORIZED: {
    path: '/error/401',
    name: 'Unauthorized'
  },
  NOT_FOUND: {
    path: '/:pathMatch(.*)*',
    name: 'NotFound'
  },

}
