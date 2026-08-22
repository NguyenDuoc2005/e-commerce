package com.ecommerce.order.model.response;

import java.time.LocalDateTime;

public interface OrderItemResponseDetail {
    String getMaOrder();
    String getTenOrder();
    String getMaOrderItem();
    String getTenProduct();
    String getAnhProduct();
    String getBrand();
    String getOrigin();
    String getColor();
    String getSize();
    Integer getQuantity();
    Double getSalePrice();
    Double getThanhTienSP();
    Double getThanhTien();
    String getTenCustomer();
    String getSdtKH();
    String getEmail();
    String getAddress();
    String getTenCustomer2();
    String getSdtKH2();
    String getEmail2();
    String getDiaChi2();
    String getLoaiOrder();
    String getTrangThaiOrder();
    LocalDateTime getThoiGian();
    Long getNgayTao();
    Double getPhiVanCdistrict();
    String getMaVoucher();
    String getTenVoucher();
    Double getGiaTriVoucher();
    Double getTongTienSauGiam();
    Double getTongTien();
    String getPhuongThucThanhToan();
    Double getDuNo();
    Double getHoanPhi();
}
