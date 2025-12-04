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
    }
  },

  ADMIN: {
    path: '/admin',
    name: 'admin',
    children: {
      MAUSAC: {
        path: 'mau-sac',
        name: 'mau-sac-admin'
      },
      THONG_KE: {
        path: 'thong-ke',
        name: 'thong-ke-admin'
      },
      NHAN_VIEN: {
        path: 'nhan-vien',
        name: 'nhan-vien-admin'
      },
      SIZE: {
        path: 'size',
        name: 'size-admin'
      },
      THUONG_HIEU: {
        path: 'thuong-hieu',
        name: 'thuong-hieu-admin'
      },
      XUAT_XU: {
        path: 'xuat-xu',
        name: 'xuat-xu-admin'
      },
      KHACH_HANG: {
        path: 'khach-hang',
        name: 'khach-hang-admin'
      },
      SAN_PHAM: {
        path: 'san-pham',
        name: 'san-pham-admin'
      },
      LOAI_DE: {
        path: 'loai-de',
        name: 'loai-de-admin'
      },
      LOAI_GIAY: {
        path: 'loai-giay',
        name: 'loai-giay-admin'
      },
      CHAT_LIEU: {
        path: 'chat-lieu',
        name: 'chat-lieu-admin'
      },
      SAN_PHAM_CHI_TIET: {
        path: 'san-pham-chi-tiet',
        name: 'san-pham-chi-tiet-admin'
      },
      THEM_SAN_PHAM_CHI_TIET: {
        path: 'them-san-pham-chi-tiet',
        name: 'them-san-pham-chi-tiet-admin'
      },
      THEM_NHAN_VIEN: {
        path: 'them-nhan-vien',
        name: 'them-nhan-vien-admin'
      },
      THEM_KHACH_HANG: {
        path: 'them-khach-hang',
        name: 'them-khach-hang-admin'
      },
      THEM_PHIEU_GIAM_GIA: {
        path: 'them-phieu-giam-gia',
        name: 'them-phieu-giam-gia-admin'
      },
      BAN_HANG: {
        path: 'ban-hang',
        name: 'ban-hang-admin'
      },
      VOUCHER: {
        path: 'voucher',
        name: 'voucher-admin'
      },
      HOA_DON: {
        path: 'hoa-don',
        name: 'hoa-don-admin'
      },
      HOA_DON_DETAIL: {
        path: 'hoa-don-detail/:maHoaDon/:id',
        name: 'hoa-don-detail-admin'
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

  ROLE_SWITCH: {
    path: '/role-switch',
    name: 'role-switch'
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