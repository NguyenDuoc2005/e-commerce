package com.ecommerce.promotion.model.response;

public interface VoucherResponse {
    String getId();
    String getMa();
    String getTen();
    Double getPhanTramGiam();
    Integer getSoLuongPhieu();
    String getNgayBatDau();
    String getNgayKetThuc();
    Double getDieuKien();
    Double getGiaGiam();
    Boolean getLoaiGiam();
    Boolean getKieuGiam();
    String getIdKH();
    String getStatus();
}
