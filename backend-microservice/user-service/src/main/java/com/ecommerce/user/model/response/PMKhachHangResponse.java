package com.ecommerce.user.model.response;

import java.math.BigDecimal;

public interface PMKhachHangResponse {

    String getId();

    String getMa();

    String getTen();

    String getSdt();

    String getTenKH();

    BigDecimal getPhiVanChuyen();

    String getDiaChi();

    BigDecimal getTongTienSauGiam();

    BigDecimal getTongTien();

    String getGhiChu();

    Integer getPhuongThucThanhToan();

    Integer getLoaiHoaDon();

    Integer getTrangThaiHoaDon();

    Long getNgayTao();
}
