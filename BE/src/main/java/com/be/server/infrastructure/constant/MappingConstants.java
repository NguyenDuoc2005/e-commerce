package com.be.server.infrastructure.constant;

public class MappingConstants {

    /* API BASE ROLE */
    public static final String ADMIN = "/admin";
    public static final String STAFF = "/staff";

    /* API VERSION PREFIX */
    public static final String API_VERSION_PREFIX = "/api/v1";

    /* API COMMON */
    public static final String API_COMMON = API_VERSION_PREFIX + "/common";

    /* AUTHENTICATION */
    public static final String API_AUTH_PREFIX = API_VERSION_PREFIX + "/auth";

    /* Common */
    public static final String API_PERMITALL_PREFIX = API_VERSION_PREFIX + "/permitall";

    /* API FOR ROLE ADMIN */
    public static final String API_ADMIN_PREFIX = API_VERSION_PREFIX + ADMIN;
    public static final String API_ADMIN_MAU_SAC = API_ADMIN_PREFIX + "/mau-sac";
    public static final String API_ADMIN_SIZE = API_ADMIN_PREFIX + "/size";
    public static final String API_ADMIN_THUONG_HIEU = API_ADMIN_PREFIX + "/thuong-hieu";
    public static final String API_ADMIN_XUAT_XU = API_ADMIN_PREFIX + "/xuat-xu";
    public static final String API_ADMIN_KHACH_HANG = API_ADMIN_PREFIX + "/khach-hang";
    public static final String API_ADMIN_NHAN_VIEN= API_ADMIN_PREFIX + "/nhan-vien";
    public static final String API_ADMIN_SAN_PHAM= API_ADMIN_PREFIX + "/san-pham";
    public static final String API_ADMIN_SAN_PHAM_CHI_TIET= API_ADMIN_PREFIX + "/san-pham-chi-tiet";
    public static final String API_ADMIN_DOT_GIAM_GIA = API_ADMIN_PREFIX + "/dot-giam-gia";
    public static final String API_ADMIN_CHAT_LIEU= API_ADMIN_PREFIX + "/chat-lieu";
    public static final String API_ADMIN_DANH_MUC= API_ADMIN_PREFIX + "/danh-muc";
    public static final String API_ADMIN_LOAI_DE= API_ADMIN_PREFIX + "/loai-de";
    public static final String API_ADMIN_BAN_HANG= API_ADMIN_PREFIX + "/ban-hang";
    public static final String API_ADMIN_HOA_DON= API_ADMIN_PREFIX + "/hoa-don";
    public static final String API_ADMIN_VOUCHER= API_ADMIN_PREFIX + "/voucher";
    public static final String API_ADMIN_THONG_KE= API_ADMIN_PREFIX + "/thong-ke";
    public static final String API_PERMITALL_SANPHAM= API_PERMITALL_PREFIX + "/san-pham";
    public static final String API_PERMITALL_SANPHAM_CHITIET= API_PERMITALL_PREFIX + "/san-pham-chi-tiet";
    public static final String API_PERMITALL_THUONGHIEU= API_PERMITALL_PREFIX + "/thuong-hieu";
    public static final String API_PERMITALL_PROFILE= API_PERMITALL_PREFIX + "/profile";
    public static final String API_PERMITALL_CART= API_PERMITALL_PREFIX + "/cart";
    public static final String API_PERMITALL_ORDER_HISTORY= API_PERMITALL_PREFIX + "/don-mua";


}
