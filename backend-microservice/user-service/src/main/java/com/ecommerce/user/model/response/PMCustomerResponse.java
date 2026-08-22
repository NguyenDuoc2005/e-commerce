package com.ecommerce.user.model.response;

import java.math.BigDecimal;

public interface PMCustomerResponse {

    String getId();

    String getCode();

    String getName();

    String getPhoneNumber();

    String getTenKH();

    BigDecimal getPhiVanCdistrict();

    String getAddress();

    BigDecimal getTongTienSauGiam();

    BigDecimal getTongTien();

    String getGhiChu();

    Integer getPhuongThucThanhToan();

    Integer getLoaiOrder();

    Integer getTrangThaiOrder();

    Long getNgayTao();
}
